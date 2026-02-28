package com.example.app_yolo11.ui.Screens.History

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.repositories.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.regex.Pattern

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _historyList = repository.getHistory()
        .map { list -> list.map { it.toSeaSnail() } }

    val historyList = combine(_historyList, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val normalizedQuery = removeAccent(query)
            list.filter {
                removeAccent(it.name).contains(normalizedQuery, ignoreCase = true) ||
                        removeAccent(it.scientificName).contains(normalizedQuery, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun removeAccent(s: String): String {
        val temp = Normalizer.normalize(s, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D')
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}