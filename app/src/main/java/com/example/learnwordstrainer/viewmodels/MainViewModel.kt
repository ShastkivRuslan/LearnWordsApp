package com.example.learnwordstrainer.viewmodels

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.learnwordstrainer.repository.ThemeRepository
import com.example.learnwordstrainer.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val wordRepository =
        WordRepository(
            application
        )
    private val themeRepository = ThemeRepository(application)

    private val _themeMode = MutableStateFlow(themeRepository.themeMode)
    val themeMode = _themeMode.asStateFlow()

    private val _totalWordsCount = MutableLiveData(0)
    val totalWordsCount: LiveData<Int> = _totalWordsCount

    private val _learnedPercentage = MutableLiveData(0)
    val learnedPercentage: LiveData<Int> = _learnedPercentage

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        val total = wordRepository.getWordCount()
        val learned = wordRepository.getLearnedWordsCount()

        _totalWordsCount.value = total
        _learnedPercentage.value = if (total > 0) (learned * 100) / total else 0
    }

    fun updateTheme() {
        _themeMode.value = themeRepository.themeMode
        AppCompatDelegate.setDefaultNightMode(themeRepository.themeMode)
    }
}