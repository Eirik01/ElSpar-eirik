package com.team12.ElSpar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.team12.ElSpar.ElSparApplication
import com.team12.ElSpar.domain.GetPowerPriceUseCase
import com.team12.ElSpar.domain.GetProjectedPowerPriceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ElSparViewModel(
    private val getPowerPriceUseCase: GetPowerPriceUseCase,
    private val getProjectedPowerPriceUseCase: GetProjectedPowerPriceUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ElSparUiState> =
        MutableStateFlow(ElSparUiState.Loading)
    val uiState: StateFlow<ElSparUiState> = _uiState.asStateFlow()

    init {
        getPowerPrice()
    }

    fun getPowerPrice() {
        viewModelScope.launch {
            _uiState.value = try {
                ElSparUiState.Success(
                    priceList = getPowerPriceUseCase()
                )
            } catch (e: IOException) {
                ElSparUiState.Error
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY] as ElSparApplication)
                val getCurrentPowerPriceUseCase = application.container.getPowerPriceUseCase
                val getProjectedPowerPriceUseCase = application.container.getProjectedPowerPriceUseCase
                ElSparViewModel(
                    getPowerPriceUseCase = getCurrentPowerPriceUseCase,
                    getProjectedPowerPriceUseCase = getProjectedPowerPriceUseCase,
                )
            }
        }
    }
}