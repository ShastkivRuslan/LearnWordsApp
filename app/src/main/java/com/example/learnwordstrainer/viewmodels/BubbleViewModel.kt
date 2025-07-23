package com.example.learnwordstrainer.viewmodels

import com.example.learnwordstrainer.model.BubblePosition
import com.example.learnwordstrainer.repository.BubbleRepository

/**
 * ViewModel for the floating bubble, managing its position and state
 */
class BubbleViewModel(private val repository: BubbleRepository?) {
    private var currentPosition: BubblePosition? = null

    /**
     * Load the saved position from the repository
     */
    fun loadSavedPosition(): BubblePosition? {
        currentPosition = repository?.loadPosition()
        return currentPosition
    }

    /**
     * Save the current position to the repository
     */
    fun saveBubblePosition(x: Int, y: Int) {
        repository?.savePosition(x, y)
        currentPosition = BubblePosition(x, y)
    }

    /**
     * Зберігає розмір бульбашки
     * @param sizeDp розмір у dp
     */
    fun saveBubbleSize(sizeDp: Int) {
        if (repository?.isValidBubbleSize(sizeDp) == true) {
            repository.setBubbleSize(sizeDp)
        }
    }

    /**
     * Отримує збережений розмір бульбашки
     * @return розмір у dp
     */
    fun getSavedBubbleSize(): Int {
        return repository?.getBubbleSize() ?: 40 // Default size if repository is null
    }

    /**
     * Перевіряє чи розмір бульбашки є валідним
     * @param size розмір для перевірки
     * @return true якщо розмір допустимий
     */
    fun isValidBubbleSize(size: Int): Boolean {
        return repository?.isValidBubbleSize(size) ?: false
    }

    /**
     * Отримує поточну позицію бульбашки
     */
    fun getCurrentPosition(): BubblePosition? {
        return currentPosition
    }
}