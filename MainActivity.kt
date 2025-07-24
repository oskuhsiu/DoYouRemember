// ...existing code...
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.osku.doyouremember.ui.theme.CardLightBlue
import me.osku.doyouremember.ui.theme.CardDarkBlue
import me.osku.doyouremember.ui.theme.CardLightYellow
import me.osku.doyouremember.ui.theme.CardDarkOrange
import me.osku.doyouremember.ui.theme.CardLightPink
import me.osku.doyouremember.ui.theme.CardDarkPurple
import me.osku.doyouremember.ui.theme.CardLightGreen
import me.osku.doyouremember.ui.theme.CardDarkGreen
// ...existing code...
@Composable
fun ClassicMemoryGame(difficulty: Int, onBack: () -> Unit) {
    // 難度對應格數與卡牌資料
    val gridInfo = listOf(
        Pair(4 to 2, 4), // 簡單 4x2 4對
        Pair(4 to 4, 8), // 普通 4x4 8對
        Pair(6 to 4, 12), // 中等 6x4 12對
        Pair(6 to 6, 18), // 困難 6x6 18對
        Pair(8 to 6, 24) // 極難 8x6 24對
    )
    val (cols, rows) = gridInfo[difficulty].first
    val pairCount = gridInfo[difficulty].second
    // 產生卡牌資料（僅數字卡，配色依難度）
    val cardColors = listOf(
        Pair(CardLightBlue, CardDarkBlue),
        Pair(CardLightYellow, CardDarkOrange),
        Pair(CardLightPink, CardDarkPurple),
        Pair(CardLightGreen, CardDarkGreen)
    )
    val colorIdx = difficulty % cardColors.size
    val (bgColor, fgColor) = cardColors[colorIdx]
    // 卡牌資料
    val cards = remember(pairCount) {
        val nums = (1..pairCount).toList()
        (nums + nums).shuffled()
    }
    // 卡牌翻開狀態
    var flipped by remember { mutableStateOf(List(cards.size) { false }) }
    Column(
        modifier = Modifier.fillMaxSize().background(MainGreen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("[經典記憶翻卡 遊戲主畫面]", color = Color.White)
        Text("${cols}x${rows}格  共${pairCount * 2}張卡", color = Color.White, modifier = Modifier.padding(8.dp))
        // 卡牌格子
        for (row in 0 until rows) {
            Row(modifier = Modifier.padding(2.dp)) {
                for (col in 0 until cols) {
                    val idx = row * cols + col
                    if (idx < cards.size) {
                        Card(
                            modifier = Modifier
                                .size(60.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    // 翻牌邏輯（暫時僅切換狀態）
                                    flipped = flipped.toMutableList().also { it[idx] = !it[idx] }
                                },
                            colors = CardDefaults.cardColors(containerColor = if (flipped[idx]) bgColor else Color.LightGray),
                            border = BorderStroke(2.dp, Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (flipped[idx]) {
                                    Text(
                                        text = cards[idx].toString(),
                                        color = fgColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp,
                                        modifier = Modifier.size(54.dp)
                                    )
                                } else {
                                    // 卡背預留動畫（暫以靜態色塊）
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(Color.Gray, shape = CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("返回難度選擇") }
    }
}
// ...existing code...

