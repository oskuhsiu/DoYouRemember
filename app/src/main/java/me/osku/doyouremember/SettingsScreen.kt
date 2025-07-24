package me.osku.doyouremember

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.osku.doyouremember.ui.theme.MainBlue
import me.osku.doyouremember.ui.theme.MainGreen
import me.osku.doyouremember.ui.theme.MainRed

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    seqCardType: String,
    onSeqCardTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBlue), // 設定頁背景改為白色
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("設定", color = MainRed, modifier = Modifier.padding(bottom = 24.dp))
//        Text("序列記憶牌面：", color = Color.White)
//        Row(modifier = Modifier.padding(8.dp)) {
//            listOf("數字牌", "動物牌").forEach { type ->
//                Button(
//                    onClick = { onSeqCardTypeChange(type) },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (seqCardType == type) MainGreen else Color.White
//                    ),
//                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
//                    modifier = Modifier.padding(horizontal = 8.dp)
//                ) {
//                    Text(
//                        type,
//                        color = if (seqCardType == type) Color.White else MainGreen // 未被選擇時文字顏色為綠色
//                    )
//                }
//            }
//        }
        Button(onClick = onBack, modifier = Modifier.padding(top = 32.dp)) { Text("返回主選單") }
    }
}
