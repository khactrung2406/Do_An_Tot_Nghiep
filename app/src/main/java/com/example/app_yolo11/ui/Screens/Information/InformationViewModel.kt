package com.example.app_yolo11.ui.Screens.Information

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_yolo11.model.SeaSnails
import com.example.app_yolo11.repositories.SeaSnailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class InformationViewModel @Inject constructor(
    private val repository: SeaSnailRepository
) : ViewModel() {

    private val _snail = MutableStateFlow<SeaSnails?>(null)
    val snail: StateFlow<SeaSnails?> = _snail.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastIdLoaded: String? = null


    fun loadSnailById(id: String) {

        if (lastIdLoaded == id && _snail.value != null) return

        _loading.value = true
        _error.value = null
        lastIdLoaded = id

        viewModelScope.launch {
            repository.debugCheckAllData()
            try {
                val result = repository.getSnailById(id)
                _snail.value = result

            } catch (e: Exception) {
                _error.value = "Lỗi tải dữ liệu: ${e.localizedMessage}"
                Log.e("InformationViewModel", "Load error", e)

            } finally {
                _loading.value = false
            }
        }
    }


    fun refresh() {
        lastIdLoaded?.let { loadSnailById(it) }
    }

    fun clearError() {
        _error.value = null
    }
}
