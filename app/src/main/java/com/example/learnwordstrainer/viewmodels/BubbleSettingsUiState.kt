package com.example.learnwordstrainer.viewmodels

data class BubbleSettingsUiState(
    val isBubbleEnabled: Boolean = true,
    val bubbleSize: Float = 40f,
    val bubbleTransparency: Float = 100f,
    val isVibrationEnabled: Boolean = true,
    val autoHideAppList: Set<String> = emptySet()
)