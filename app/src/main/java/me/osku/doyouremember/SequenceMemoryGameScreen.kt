package me.osku.doyouremember

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.osku.doyouremember.ui.theme.*

@Composable
fun SequenceMemoryEntry(onBack: () -> Unit, cardType: String) {
    val levels = listOf(
        SequenceLevel("簡單", 4, 4 to 2),
        SequenceLevel("簡單", 5, 4 to 2),
        SequenceLevel("簡單", 6, 4 to 2),
        SequenceLevel("普通", 7, 4 to 3),
        SequenceLevel("普通", 8, 4 to 3),
        SequenceLevel("普通", 9, 4 to 3),
        SequenceLevel("普通", 10, 4 to 3),
        SequenceLevel("中等", 11, 4 to 4),
        SequenceLevel("中等", 12, 4 to 4),
        SequenceLevel("中等", 13, 4 to 4),
        SequenceLevel("中等", 14, 4 to 4),
        SequenceLevel("中等", 15, 4 to 4),
        SequenceLevel("困難", 16, 5 to 4),
        SequenceLevel("困難", 17, 5 to 4),
        SequenceLevel("困難", 18, 5 to 4),
        SequenceLevel("困難", 19, 5 to 4),
        SequenceLevel("困難", 20, 5 to 4),
        SequenceLevel("困難", 21, 6 to 6),
        SequenceLevel("困難", 22, 6 to 6),
        SequenceLevel("困難", 23, 6 to 6),
        SequenceLevel("困難", 24, 6 to 6),
        SequenceLevel("困難", 25, 6 to 6),
        SequenceLevel("極難", 26, 8 to 6),
        SequenceLevel("極難", 27, 8 to 6),
        SequenceLevel("極難", 28, 8 to 6),
        SequenceLevel("極難", 29, 8 to 6),
        SequenceLevel("極難", 30, 8 to 6)
    )
    var levelIdx by remember { mutableStateOf(0) }
    var sequenceLen by remember { mutableStateOf(levels[levelIdx].seqLen) }
    val (cols, rows) = levels[levelIdx].grid
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val cardAreaWidth = screenWidth * 0.95f
    val cardAreaHeight = screenHeight * 0.7f
    val cardWidth = cardAreaWidth / cols
    val cardHeight = cardAreaHeight / rows.inc()
    val cardSize = minOf(cardWidth, cardHeight)
    val animalList = (1..20).map { "animal_$it" }
    val cardPool =
        if (cardType == "數字牌") (1..cols * rows).map { it.toString() } else animalList.take(cols * rows)
    val sequence = remember(sequenceLen, cardType) {
        cardPool.shuffled().take(sequenceLen)
    }
    var showSequence by remember { mutableStateOf(true) }
    var userInput by remember { mutableStateOf(listOf<String>()) }
    var errorCount by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    LaunchedEffect(sequence, showSequence) {
        if (showSequence) {
            for (item in sequence) {
                userInput = listOf()
                delay(800)
            }
            showSequence = false
        }
    }
    // UI重構：卡片區域置中，底部固定按鈕，過關流程修正
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen) // 柔和淺藍色背景，提升可視性與舒適度
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "[序列記憶] 難度：${levels[levelIdx].name} 目前長度：$sequenceLen",
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            if (showSequence) {
                Text("請記住以下序列：", color = MainYellow)
                // 正確：每排最多7個，超過自動換行，所有題目都用同一個 scale（根據最大一排數量決定）
                val maxPerRow = 7
                val rowsNeeded = (sequence.size + maxPerRow - 1) / maxPerRow
                val maxRowCount = sequence.size.coerceAtMost(maxPerRow)
                val scale = when {
                    maxRowCount <= 4 -> .9f
                    maxRowCount <= 5 -> 0.8f
                    maxRowCount <= 6 -> 0.7f
                    else -> 0.6f
                }
                for (rowIdx in 0 until rowsNeeded) {
                    Row {
                        val start = rowIdx * maxPerRow
                        val end = minOf(start + maxPerRow, sequence.size)
                        val rowItems = sequence.subList(start, end)
                        rowItems.forEach { item ->
                            if (cardType == "數字牌") {
                                Box(
                                    Modifier
                                        .size(cardSize * scale)
                                        .background(CardLightBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        item,
                                        color = CardDarkBlue,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontSize = (cardSize.value * scale * 0.75f).sp
                                    )
                                }
                            } else {
                                Box(
                                    Modifier
                                        .size(cardSize * scale)
                                        .background(CardLightPink, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val resId = getDrawableIdByName(item)
                                    if (resId != null) {
                                        androidx.compose.foundation.Image(
                                            painterResource(resId),
                                            contentDescription = null,
                                            modifier = Modifier.size(cardSize * scale * 0.75f)
                                        )
                                    } else {
                                        Text(
                                            "?",
                                            color = CardDarkPurple,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            fontSize = (cardSize.value * scale * 0.75f).sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (!finished) {
                Text("請依序點擊卡牌：", color = MainYellow)
                val allCards = cardPool.shuffled().take(cols * rows)
                Column {
                    for (row in 0 until rows) {
                        Row {
                            for (col in 0 until cols) {
                                val idx = row * cols + col
                                if (idx < allCards.size) {
                                    val card = allCards[idx]
                                    Card(
                                        modifier = Modifier
                                            .size(cardSize)
                                            .padding(2.dp)
                                            .clickable(enabled = userInput.size < sequenceLen && !finished) {
                                                if (userInput.size < sequenceLen) {
                                                    userInput = userInput + card
                                                    if (userInput.size == sequenceLen) {
                                                        if (userInput == sequence) {
                                                            finished = true
                                                        } else {
                                                            errorCount++
                                                            userInput = listOf()
                                                            showSequence = true
                                                        }
                                                    }
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        border = BorderStroke(2.dp, Color.White),
                                        elevation = CardDefaults.cardElevation(2.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (cardType == "數字牌") {
                                                Text(
                                                    card,
                                                    color = CardDarkBlue,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    fontSize = (cardSize.value * 0.6f).sp
                                                )
                                            } else {
                                                val resId = getDrawableIdByName(card)
                                                if (resId != null) {
                                                    androidx.compose.foundation.Image(
                                                        painterResource(resId),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(cardSize * 0.6f)
                                                    )
                                                } else {
                                                    Text(
                                                        "?",
                                                        color = CardDarkPurple,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        fontSize = (cardSize.value * 0.6f).sp
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
                Text("錯誤次數：$errorCount", color = Color.White)
            } else {
                // 過關後顯示「進入下一關」按鈕
                Button(onClick = {
                    // 進入下一關，提升難度與序列長度
                    val nextLevel = if (levelIdx < levels.lastIndex) levelIdx + 1 else levelIdx
                    val nextLen =
                        if (levelIdx < levels.lastIndex) levels[nextLevel].seqLen else sequenceLen + 1
                    levelIdx = nextLevel
                    sequenceLen = nextLen
                    userInput = listOf()
                    errorCount = 0
                    finished = false
                    showSequence = true
                }, modifier = Modifier.padding(top = 24.dp)) {
                    Text("進入下一關")
                }
            }
        }
        // 底部固定「回選單」按鈕
        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(48.dp)
        ) { Text("返回主選單") }
    }
}

data class SequenceLevel(val name: String, val seqLen: Int, val grid: Pair<Int, Int>)

@Composable
fun getDrawableIdByName(name: String): Int? {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resId != 0) resId else null
}
