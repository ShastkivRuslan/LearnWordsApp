package com.example.learnwordstrainer.ui.activities

import BubbleSettingsScreen
import BubbleSettingsViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.learnwordstrainer.ui.theme.LearnWordsTrainerTheme

class BubbleSettingsActivity : ComponentActivity() {

    private val viewModel: BubbleSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LearnWordsTrainerTheme {
                BubbleSettingsScreen(
                    uiState = uiState,
                    onBackPressed = { finish() },
                    onBubbleEnabledChange = viewModel::setBubbleEnabled,
                    onBubbleSizeChange = viewModel::setBubbleSize,
                    onTransparencyChange = viewModel::setBubbleTransparency,
                    onVibrationEnabledChange = viewModel::setVibrationEnabled,
                    onAboutBubbleClick = {},
                    onAutoHideClick = {})
            }
        }
    }
}