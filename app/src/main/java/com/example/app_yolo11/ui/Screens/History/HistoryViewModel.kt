package com.example.app_yolo11.ui.Screens.History

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.repositories.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {
    val historyList = repository.getHistory()
        .map { list -> list.map { it.toSeaSnail() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}