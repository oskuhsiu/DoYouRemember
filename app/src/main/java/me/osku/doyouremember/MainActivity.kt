package me.osku.doyouremember

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.osku.doyouremember.ui.theme.CardDarkBlue
import me.osku.doyouremember.ui.theme.CardDarkGreen
import me.osku.doyouremember.ui.theme.CardDarkOrange
import me.osku.doyouremember.ui.theme.CardDarkPurple
import me.osku.doyouremember.ui.theme.CardLightBlue
import me.osku.doyouremember.ui.theme.CardLightGreen
import me.osku.doyouremember.ui.theme.CardLightPink
import me.osku.doyouremember.ui.theme.CardLightYellow
import me.osku.doyouremember.ui.theme.DoYouRememberTheme
import me.osku.doyouremember.ui.theme.MainBlue
import me.osku.doyouremember.ui.theme.MainGreen
import me.osku.doyouremember.ui.theme.MainRed
import me.osku.doyouremember.ui.theme.MainYellow

enum class MainScreen {
    MENU, GAME, SETTINGS, EXIT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoYouRememberTheme {
                var screen by remember { mutableStateOf(MainScreen.MENU) }
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        MainScreen.MENU -> MainMenu(
                            onStart = { screen = MainScreen.GAME },
                            onSettings = { screen = MainScreen.SETTINGS },
                            onExit = { screen = MainScreen.EXIT }
                        )

                        MainScreen.GAME -> ClassicGameEntry(onBack = { screen = MainScreen.MENU })
                        MainScreen.SETTINGS -> SettingsPlaceholder(onBack = {
                            screen = MainScreen.MENU
                        })

                        MainScreen.EXIT -> ExitApp()
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenu(onStart: () -> Unit, onSettings: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainYellow),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("翻卡記憶遊戲", color = MainRed, modifier = Modifier.padding(bottom = 32.dp))
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(8.dp)
        ) { Text("開始遊戲") }
        Button(
            onClick = onSettings,
            colors = ButtonDefaults.buttonColors(containerColor = MainRed),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(8.dp)
        ) { Text("設定") }
        Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(8.dp)
        ) { Text("退出遊戲") }
    }
}

@Composable
fun GamePlaceholder(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("[經典記憶翻卡 遊戲畫面待實作]")
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("返回主選單") }
    }
}

@Composable
fun SettingsPlaceholder(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("[設定畫面待實作]")
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("返回主選單") }
    }
}

@Composable
fun ExitApp() {
    // Android無法直接結束App，這裡可顯示提示或finish()
    Text("感謝遊玩！請手動關閉App。")
}

@Composable
fun ClassicGameEntry(onBack: () -> Unit) {
    val difficulties = listOf(
        DifficultyLevel("簡單", "4x2格（8張卡） 無限時", "適合幼童"),
        DifficultyLevel("普通", "4x4格（16張卡） 120秒", "適合學齡前兒童"),
        DifficultyLevel("中等", "6x4格（24張卡） 90秒 30次翻牌", "適合青少年"),
        DifficultyLevel("困難", "6x6格（36張卡） 75秒 40次翻牌", "適合成人"),
        DifficultyLevel("極難", "8x6格（48張卡） 60秒 50次翻牌", "適合年長者/高手")
    )
    var selected by remember { mutableStateOf(-1) }
    if (selected < 0) {
        // 難度選擇畫面
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
        // 直接全畫面顯示遊戲，不再疊在難度選擇上
        ClassicMemoryGame(difficulty = selected, onBack = { selected = -1 })
    }
}

data class DifficultyLevel(val name: String, val desc: String, val suit: String)

@Composable
fun ClassicMemoryGame(difficulty: Int, onBack: () -> Unit) {
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
    var pendingFlipBack by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var flipped by remember { mutableStateOf(List(cards.size) { false }) }
    var matched by remember { mutableStateOf(List(cards.size) { false }) }
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    var lock by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var flipCount by remember { mutableStateOf(0) }

    // 取得螢幕寬高，計算卡牌最大尺寸
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    // 預留上下資訊欄空間，卡牌區域高度約70%螢幕
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
                                        .clickable(enabled = !isFlipped && !lock) {
                                            if (lock) return@clickable
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
                                                    .graphicsLayer { rotationY = 180f } // 修正鏡像顯示
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DoYouRememberTheme {
        MainMenu({}, {}, {})
    }
}