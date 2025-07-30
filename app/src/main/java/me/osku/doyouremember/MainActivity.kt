package me.osku.doyouremember

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.osku.doyouremember.ui.theme.DoYouRememberTheme
import me.osku.doyouremember.ui.theme.MainBlue
import me.osku.doyouremember.ui.theme.MainGreen
import me.osku.doyouremember.ui.theme.MainRed
import me.osku.doyouremember.ui.theme.MainYellow
import androidx.compose.foundation.shape.RoundedCornerShape

enum class MainScreen {
    MENU, GAME, SETTINGS, EXIT, SEQMEMORY, MEMORY_CHAIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefsManager = remember { PreferencesManager(this@MainActivity) }
            var screen by remember { mutableStateOf(MainScreen.MENU) }
            var seqCardType by remember { mutableStateOf(prefsManager.seqCardType) }
            var briefReveal by remember { mutableStateOf(prefsManager.briefReveal) }

            Surface(modifier = Modifier.fillMaxSize()) {
                when (screen) {
                    MainScreen.MENU -> MainMenu(
                        onStart = { screen = MainScreen.GAME },
                        onSettings = { screen = MainScreen.SETTINGS },
                        onExit = { screen = MainScreen.EXIT },
                        onSeqMemory = { screen = MainScreen.SEQMEMORY },
                        onMemoryChain = { screen = MainScreen.MEMORY_CHAIN }
                    )
                    MainScreen.GAME -> ClassicGameEntry(
                        onBack = { screen = MainScreen.MENU },
                        briefReveal = briefReveal
                    )
                    MainScreen.SEQMEMORY -> SequenceMemoryEntry(
                        onBack = { screen = MainScreen.MENU },
                        cardType = seqCardType
                    )
                    MainScreen.MEMORY_CHAIN -> MemoryChainEntry(
                        onBack = { screen = MainScreen.MENU }
                    )
                    MainScreen.SETTINGS -> SettingsScreen(
                        onBack = { screen = MainScreen.MENU },
                        seqCardType = seqCardType,
                        onSeqCardTypeChange = {
                            seqCardType = it
                            prefsManager.seqCardType = it
                        },
                        briefReveal = briefReveal,
                        onBriefRevealChange = {
                            briefReveal = it
                            prefsManager.briefReveal = it
                        }
                    )
                    MainScreen.EXIT -> ExitApp()
                }
            }
        }
    }
}

@Composable
fun MainMenu(onStart: () -> Unit, onSettings: () -> Unit, onExit: () -> Unit, onSeqMemory: () -> Unit, onMemoryChain: () -> Unit) {
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
        ) { Text("經典記憶翻卡") }
        Button(
            onClick = onSeqMemory,
            colors = ButtonDefaults.buttonColors(containerColor = MainGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(8.dp)
        ) { Text("序列記憶") }
        Button(
            onClick = onMemoryChain,
            colors = ButtonDefaults.buttonColors(containerColor = MainRed),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(8.dp)
        ) { Text("記憶連鎖") }
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
fun ExitApp() {
    // Android無法直接結束App，這裡可顯示提示或finish()
    Text("感謝遊玩！請手動關閉App。")
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DoYouRememberTheme {
        MainMenu({}, {}, {}, {}, {})
    }
}