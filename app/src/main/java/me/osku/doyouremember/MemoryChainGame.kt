package me.osku.doyouremember

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.osku.doyouremember.ui.theme.*
import java.util.*

// 难度定义
enum class MemoryChainDifficulty(
    val displayName: String,
    val rows: Int,
    val cols: Int,
    val colorCount: Int
) {
    EASY("簡單", 4, 4, 1),
    NORMAL("普通", 4, 4, 2),
    MEDIUM("中等", 6, 4, 2),
    HARD("困難", 6, 6, 3),
    EXTREME("極難", 8, 6, 4)
}

// 卡牌数据类
data class MemoryChainCard(
    val id: Int,
    val number: Int,
    val color: Color,
    val isFlipped: Boolean = false,
    val isFound: Boolean = false,
    val isMissed: Boolean = false
)

// 游戏状态
enum class GamePhase {
    MEMORIZATION,  // 记忆阶段
    CHALLENGE,     // 挑战阶段
    FINISHED       // 游戏结束
}

// 游戏状态数据
data class MemoryChainGameState(
    val cards: List<MemoryChainCard> = emptyList(),
    val phase: GamePhase = GamePhase.MEMORIZATION,
    val currentTarget: MemoryChainCard? = null,
    val remainingFlips: Int = 10,
    val score: Int = 0,
    val memoryTimeLeft: Float = 0f,
    val availableTargets: List<MemoryChainCard> = emptyList(),
    val presentedButNotFlipped: List<MemoryChainCard> = emptyList(),
    val isInputLocked: Boolean = false
)

@Composable
fun MemoryChainEntry(onBack: () -> Unit) {
    var selectedDifficulty by remember { mutableStateOf<MemoryChainDifficulty?>(null) }

    if (selectedDifficulty == null) {
        MemoryChainDifficultySelection(
            onDifficultySelected = { selectedDifficulty = it },
            onBack = onBack
        )
    } else {
        MemoryChainGame(
            difficulty = selectedDifficulty!!,
            onBack = onBack,
            onRestart = { selectedDifficulty = null }
        )
    }
}

@Composable
fun MemoryChainDifficultySelection(
    onDifficultySelected: (MemoryChainDifficulty) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainYellow)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "記憶連鎖 - 選擇難度",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MainRed,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        MemoryChainDifficulty.entries.forEach { difficulty ->
            Button(
                onClick = { onDifficultySelected(difficulty) },
                colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        difficulty.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${difficulty.rows}x${difficulty.cols} 格",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(8.dp)
        ) {
            Text("返回主選單")
        }
    }
}

@Composable
fun MemoryChainGame(
    difficulty: MemoryChainDifficulty,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    var gameState by remember { mutableStateOf(initializeGameState(difficulty)) }

    // 记忆阶段计时器
    LaunchedEffect(gameState.phase) {
        if (gameState.phase == GamePhase.MEMORIZATION) {
            val totalTime = (difficulty.rows * difficulty.cols * 0.1f)
            var timeLeft = totalTime

            while (timeLeft > 0 && gameState.phase == GamePhase.MEMORIZATION) {
                gameState = gameState.copy(memoryTimeLeft = timeLeft)
                delay(100)
                timeLeft -= 0.1f
            }

            if (gameState.phase == GamePhase.MEMORIZATION) {
                // 翻转所有卡牌到背面，开始挑战阶段
                gameState = gameState.copy(
                    cards = gameState.cards.map { it.copy(isFlipped = false) },
                    phase = GamePhase.CHALLENGE,
                    availableTargets = gameState.cards.shuffled()
                )
                // 生成第一个提示
                gameState = generateNextTarget(gameState)
            }
        }
    }

    // 处理错误选择后的延迟盖卡逻辑
    LaunchedEffect(gameState.isInputLocked) {
        if (gameState.isInputLocked && gameState.phase == GamePhase.CHALLENGE) {
            // 等待1.5秒让用户看到错误的选择
            delay(1500)

            // 将所有非正确答案的翻开卡片重新盖起来
            val updatedCards = gameState.cards.map { card ->
                if (card.isFlipped && !card.isFound) {
                    card.copy(isFlipped = false)
                } else {
                    card
                }
            }

            // 更新游戏状态并生成下一个目标
            val newState = gameState.copy(cards = updatedCards)
            if (newState.remainingFlips > 0) {
                gameState = generateNextTarget(newState)
            } else {
                gameState = finishGame(newState)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainYellow)
            .padding(16.dp)
    ) {
        // 顶部信息栏
        GameInfoBar(gameState = gameState)

        Spacer(modifier = Modifier.height(16.dp))

        // 游戏网格
        MemoryChainGrid(
            gameState = gameState,
            difficulty = difficulty,
            onCardClick = { cardIndex ->
                if (!gameState.isInputLocked && gameState.phase == GamePhase.CHALLENGE) {
                    gameState = handleCardClick(gameState, cardIndex)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 底部按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = MainGreen)
            ) {
                Text("重新開始")
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("返回")
            }
        }

        // 游戏结束界面
        if (gameState.phase == GamePhase.FINISHED) {
            GameEndDialog(
                gameState = gameState,
                onRestart = onRestart,
                onBack = onBack
            )
        }
    }
}

@Composable
fun GameInfoBar(gameState: MemoryChainGameState) {
    Column {
        when (gameState.phase) {
            GamePhase.MEMORIZATION -> {
                Text(
                    "記憶階段 - 剩餘時間: ${String.format(Locale.getDefault(), "%.1f", gameState.memoryTimeLeft)}秒",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainRed
                )
            }
            GamePhase.CHALLENGE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "得分: ${gameState.score}/10",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "剩餘翻牌: ${gameState.remainingFlips}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 目标提示
                gameState.currentTarget?.let { target ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            "尋找目標: ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(target.color, RoundedCornerShape(8.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                target.number.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            GamePhase.FINISHED -> {
                Text(
                    "遊戲結束！最終得分: ${gameState.score}/10",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainRed
                )
            }
        }
    }
}

@Composable
fun MemoryChainGrid(
    gameState: MemoryChainGameState,
    difficulty: MemoryChainDifficulty,
    onCardClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(difficulty.cols),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(gameState.cards) { index, card ->
            MemoryChainCardView(
                card = card,
                gamePhase = gameState.phase,
                onClick = { onCardClick(index) }
            )
        }
    }
}

@Composable
fun MemoryChainCardView(
    card: MemoryChainCard,
    gamePhase: GamePhase,
    onClick: () -> Unit
) {
    val isVisible = when (gamePhase) {
        GamePhase.MEMORIZATION -> true
        GamePhase.CHALLENGE -> card.isFlipped || card.isFound
        GamePhase.FINISHED -> true
    }

    val backgroundColor = if (isVisible) card.color else Color.Gray
    val borderColor = when {
        card.isMissed -> Color.Red
        card.isFound -> Color.Green
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = gamePhase == GamePhase.CHALLENGE && !card.isFound) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            Text(
                text = card.number.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // 标记未找到的卡牌
        if (gamePhase == GamePhase.FINISHED && card.isMissed) {
            Text(
                "✗",
                fontSize = 24.sp,
                color = Color.Red,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        // 标记已找到的卡牌
        if (card.isFound) {
            Text(
                "✓",
                fontSize = 24.sp,
                color = Color.Green,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun GameEndDialog(
    gameState: MemoryChainGameState,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                "遊戲結束！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "最終得分: ${gameState.score}/10",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "成功率: ${(gameState.score * 10)}%",
                    fontSize = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = MainGreen)
            ) {
                Text("再玩一次")
            }
        },
        dismissButton = {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("返回主選單")
            }
        }
    )
}

// 辅助函数
fun initializeGameState(difficulty: MemoryChainDifficulty): MemoryChainGameState {
    val totalCards = difficulty.rows * difficulty.cols
    val numbersPerColor = totalCards / difficulty.colorCount

    val colors = listOf(MainRed, MainBlue, MainGreen, Color.Magenta, Color.Cyan).take(difficulty.colorCount)

    val cards = mutableListOf<MemoryChainCard>()
    var cardId = 0

    colors.forEach { color ->
        repeat(numbersPerColor) { numberIndex ->
            cards.add(
                MemoryChainCard(
                    id = cardId++,
                    number = numberIndex + 1,
                    color = color,
                    isFlipped = true // 初始显示正面
                )
            )
        }
    }

    // 如果有剩余卡牌，随机分配颜色
    while (cards.size < totalCards) {
        val randomColor = colors.random()
        cards.add(
            MemoryChainCard(
                id = cardId++,
                number = (cards.size % numbersPerColor) + 1,
                color = randomColor,
                isFlipped = true
            )
        )
    }

    return MemoryChainGameState(
        cards = cards.shuffled(),
        phase = GamePhase.MEMORIZATION,
        memoryTimeLeft = totalCards * 0.2f
    )
}

fun generateNextTarget(gameState: MemoryChainGameState): MemoryChainGameState {
    if (gameState.availableTargets.isEmpty() || gameState.remainingFlips <= 0) {
        return gameState.copy(phase = GamePhase.FINISHED)
    }

    val nextTarget = gameState.availableTargets.first()
    return gameState.copy(
        currentTarget = nextTarget,
        availableTargets = gameState.availableTargets.drop(1),
        isInputLocked = false  // 重置输入锁定状态
    )
}

fun handleCardClick(gameState: MemoryChainGameState, cardIndex: Int): MemoryChainGameState {
    val clickedCard = gameState.cards[cardIndex]
    val currentTarget = gameState.currentTarget ?: return gameState

    // 翻转卡牌
    val updatedCards = gameState.cards.toMutableList()
    updatedCards[cardIndex] = clickedCard.copy(isFlipped = true)

    // 判定正确性
    val isCorrect = clickedCard.id == currentTarget.id

    return if (isCorrect) {
        // 正确匹配
        updatedCards[cardIndex] = clickedCard.copy(isFlipped = true, isFound = true)
        val newState = gameState.copy(
            cards = updatedCards,
            score = gameState.score + 1,
            remainingFlips = gameState.remainingFlips - 1
        )

        // 生成下一个目标或结束游戏
        if (newState.remainingFlips <= 0) {
            finishGame(newState)
        } else {
            generateNextTarget(newState)
        }
    } else {
        // 错误匹配 - 记录未找到的目标，并在延迟后盖起来
        val presentedButNotFlipped = gameState.presentedButNotFlipped.toMutableList()
        if (!presentedButNotFlipped.contains(currentTarget)) {
            presentedButNotFlipped.add(currentTarget)
        }

        val newState = gameState.copy(
            cards = updatedCards,
            remainingFlips = gameState.remainingFlips - 1,
            presentedButNotFlipped = presentedButNotFlipped,
            isInputLocked = true
        )

        // 生成下一个目标或结束游戏
        if (newState.remainingFlips <= 0) {
            finishGame(newState)
        } else {
            // 延迟后将错误的卡片盖起来，然后生成下一个目标
            newState
        }
    }
}

fun finishGame(gameState: MemoryChainGameState): MemoryChainGameState {
    // 标记所有未找到的目标卡牌
    val finalCards = gameState.cards.map { card ->
        if (gameState.presentedButNotFlipped.any { it.id == card.id }) {
            card.copy(isMissed = true)
        } else {
            card
        }
    }

    return gameState.copy(
        cards = finalCards,
        phase = GamePhase.FINISHED,
        isInputLocked = false
    )
}