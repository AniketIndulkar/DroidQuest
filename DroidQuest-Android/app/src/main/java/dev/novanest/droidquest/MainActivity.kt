package dev.novanest.droidquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.novanest.droidquest.ui.DroidQuestApp
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.DroidQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DroidQuestApplication).container
        setContent {
            DroidQuestTheme {
                val vm: DroidQuestViewModel = viewModel(
                    factory = DroidQuestViewModel.Factory(container.contentRepository, container.progressRepository),
                )
                Box(Modifier.fillMaxSize().background(DQ.ScreenBg)) {
                    DroidQuestApp(vm)
                }
            }
        }
    }
}
