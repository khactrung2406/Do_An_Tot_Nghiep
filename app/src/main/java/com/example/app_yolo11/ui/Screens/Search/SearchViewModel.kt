package com.example.app_yolo11.ui.Screens.Search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.SeaSnails
import com.example.app_yolo11.repositories.HistoryRepository // [MỚI] Import HistoryRepo
import com.example.app_yolo11.repositories.SeaSnailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    val repository: SeaSnailRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SeaSnails>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun searchSnails() {
        searchJob?.cancel()
        val queryText = _searchQuery.value.trim()

        if (queryText.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        _isLoading.value = true
        searchJob = viewModelScope.launch {
            try {
                val results = repository.searchSnailsByName(queryText)
                _searchResults.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // [MỚI] Hàm lưu vào lịch sử khi người dùng click vào kết quả
    fun addToHistory(snail: SeaSnails) {
        viewModelScope.launch {
            try {
                historyRepository.addToHistory(snail)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}