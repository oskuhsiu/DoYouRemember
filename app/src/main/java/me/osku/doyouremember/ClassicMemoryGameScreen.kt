package me.osku.doyouremember

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.osku.doyouremember.ui.theme.*

@Composable
fun ClassicGameEntry(onBack: () -> Unit, briefReveal: Boolean) {
    val difficulties = listOf(
        DifficultyLevel("簡單", "4x2格（8張卡） 無限時", "適合幼童"),
        DifficultyLevel("普通", "4x4格（16張卡） 120秒", "適合學齡前兒童"),
        DifficultyLevel("中等", "6x4格（24張卡） 90秒 30次翻牌", "適合青少年"),
        DifficultyLevel("困難", "6x6格（36張卡） 75秒 40次翻牌", "適合成人"),
        DifficultyLevel("極難", "8x6格（48張卡） 60秒 50次翻牌", "適合年長者/高手")
    )
    var selected by remember { mutableStateOf(-1) }
    if (selected < 0) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBlue),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("選擇難度", color = Color.White, modifier = Modifier.padding(bottom = 24.dp))
            difficulties.forEachIndexed { idx, diff ->
                Button(
                    onClick = { selected = idx },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selected == idx) MainYellow else Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(diff.name, color = MainRed)
                        Text(diff.desc, color = Color.DarkGray, fontSize = 10.sp)
                        Text(diff.suit, color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
            Button(
                onClick = onBack,
                modifier = Modifier.padding(top = 24.dp)
            ) { Text("返回主選單") }
        }
    } else {
        ClassicMemoryGame(difficulty = selected, onBack = { selected = -1 }, briefReveal = briefReveal)
    }
}

@Composable
fun ClassicMemoryGame(difficulty: Int, onBack: () -> Unit, briefReveal: Boolean) {
    val gridInfo = listOf(
        Pair(4 to 2, 4),
        Pair(4 to 4, 8),
        Pair(6 to 4, 12),
        Pair(6 to 6, 18),
        Pair(8 to 6, 24)
    )
    val configuration = LocalConfiguration.current
    val (cols, rows) = if (configuration.screenWidthDp > configuration.screenHeightDp) gridInfo[difficulty].first else Pair(
        gridInfo[difficulty].first.second,
        gridInfo[difficulty].first.first
    )
    val pairCount = gridInfo[difficulty].second
    val cardColors = listOf(
        Pair(CardLightBlue, CardDarkBlue),
        Pair(CardLightYellow, CardDarkOrange),
        Pair(CardLightPink, CardDarkPurple),
        Pair(CardLightGreen, CardDarkGreen)
    )
    val colorIdx = difficulty % cardColors.size
    val (bgColor, fgColor) = cardColors[colorIdx]
    val cards = remember(pairCount) {
        val nums = (1..pairCount).toList()
        (nums + nums).shuffled()
    }

    // 短暫開牌相關狀態
    var isInBriefReveal by remember { mutableStateOf(briefReveal) }
    var briefRevealTimeLeft by remember { mutableStateOf(if (briefReveal) cards.size * 0.05f else 0f) }

    var pendingFlipBack by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var flipped by remember { mutableStateOf(List(cards.size) { briefReveal }) } // 如果開啟短暫開牌，初始全部翻開
    var matched by remember { mutableStateOf(List(cards.size) { false }) }
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    var lock by remember { mutableStateOf(briefReveal) } // 短暫開牌時鎖定操作
    var score by remember { mutableStateOf(0) }
    var flipCount by remember { mutableStateOf(0) }

    // 短暫開牌倒數計時
    LaunchedEffect(isInBriefReveal) {
        if (isInBriefReveal && briefRevealTimeLeft > 0) {
            while (briefRevealTimeLeft > 0) {
                delay(50L) // 每50毫秒更新一次
                briefRevealTimeLeft -= 0.05f
            }
            // 時間到，關閉所有牌
            flipped = List(cards.size) { false }
            lock = false
            isInBriefReveal = false
        }
    }

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val cardAreaWidth = screenWidth * 0.95f
    val cardAreaHeight = screenHeight * 0.7f
    val cardWidth = cardAreaWidth / cols
    val cardHeight = cardAreaHeight / rows.inc()
    val cardSize = minOf(cardWidth, cardHeight)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("[經典記憶翻卡 遊戲主畫面]", color = Color.White)

        // 顯示倒數計時（如果在短暫開牌階段）
        if (isInBriefReveal) {
            Text(
                "記住牌面位置！剩餘 ${String.format("%.1f", briefRevealTimeLeft)} 秒",
                color = Color.Yellow,
                modifier = Modifier.padding(4.dp)
            )
        }

        Text(
            "${cols}x${rows}格  共${pairCount * 2}張卡  分數：$score  翻牌次數：$flipCount",
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardAreaHeight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        for (col in 0 until cols) {
                            val idx = row * cols + col
                            if (idx < cards.size) {
                                val isFlipped = flipped[idx] || matched[idx]
                                val rotation by animateFloatAsState(
                                    targetValue = if (isFlipped) 180f else 0f,
                                    animationSpec = tween(durationMillis = 400), label = "flip"
                                )
                                Card(
                                    modifier = Modifier
                                        .size(cardSize)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .graphicsLayer {
                                            rotationY = rotation
                                            cameraDistance = 8 * density
                                        }
                                        .clickable(enabled = !isFlipped && !lock && !isInBriefReveal) {
                                            if (lock || isInBriefReveal) return@clickable
                                            val newFlipped = flipped.toMutableList()
                                            newFlipped[idx] = true
                                            flipped = newFlipped
                                            flipCount++
                                            if (selectedIdx == null) {
                                                selectedIdx = idx
                                            } else {
                                                lock = true
                                                val prevIdx = selectedIdx!!
                                                if (cards[prevIdx] == cards[idx]) {
                                                    matched = matched.toMutableList().also {
                                                        it[prevIdx] = true; it[idx] = true
                                                    }
                                                    score += 10
                                                    selectedIdx = null
                                                    lock = false
                                                } else {
                                                    pendingFlipBack = prevIdx to idx
                                                    lock = true
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    border = BorderStroke(2.dp, Color.White),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (rotation > 90f) {
                                            Text(
                                                text = cards[idx].toString(),
                                                color = fgColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (cardSize.value * 0.7f).sp,
                                                modifier = Modifier
                                                    .size(cardSize * 0.9f)
                                                    .graphicsLayer { rotationY = 180f }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(cardSize * 0.9f)
                                                    .background(bgColor, shape = CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(pendingFlipBack) {
            pendingFlipBack?.let { (first, second) ->
                delay(700)
                flipped = flipped.toMutableList().also {
                    it[first] = false; it[second] = false
                }
                selectedIdx = null
                lock = false
                pendingFlipBack = null
            }
        }
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("返回難度選擇") }
    }
}

data class DifficultyLevel(val name: String, val desc: String, val suit: String)
