package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.game.ui.ArchiveScreen
import com.example.game.ui.ArmoryScreen
import com.example.game.ui.GameScreen
import com.example.game.ui.TitleScreen
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.ScreenState
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoidBlack

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VoidBlack)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        SpawnApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SpawnApp(viewModel: GameViewModel) {
    val screenState by viewModel.screenState.collectAsState()

    // Handle back button presses gracefully
    if (screenState != ScreenState.TITLE) {
        BackHandler {
            viewModel.setScreenState(ScreenState.TITLE)
        }
    }

    when (screenState) {
        ScreenState.TITLE -> {
            TitleScreen(viewModel = viewModel)
        }
        ScreenState.PLAYING,
        ScreenState.STAGE_CLEAR,
        ScreenState.GAME_OVER,
        ScreenState.VICTORY_SCREEN -> {
            GameScreen(viewModel = viewModel)
        }
        ScreenState.ARMORY -> {
            ArmoryScreen(viewModel = viewModel)
        }
        ScreenState.ARCHIVE -> {
            ArchiveScreen(viewModel = viewModel)
        }
    }
}
