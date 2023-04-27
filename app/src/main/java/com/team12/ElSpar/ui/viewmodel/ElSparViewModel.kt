package com.team12.ElSpar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.team12.ElSpar.Settings.PriceArea
import com.team12.ElSpar.ElSparApplication
import com.team12.ElSpar.data.SettingsRepository
import com.team12.ElSpar.domain.GetPowerPriceUseCase
import kotlinx.coroutines.Dispatchers
import com.team12.ElSpar.model.PricePeriod
import com.team12.ElSpar.network.NoConnectionException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class ElSparViewModel(
    private val getPowerPriceUseCase: GetPowerPriceUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState: MutableStateFlow<ElSparUiState> = MutableStateFlow(ElSparUiState.Loading)
    val uiState: StateFlow<ElSparUiState> = _uiState.asStateFlow()

    val settings = settingsRepository.settingsFlow

    private var currentPricePeriod = PricePeriod.DAY
    private var currentEndDate = LocalDate.now()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            settings.collect { settings ->
                if (!settings.initialStartupCompleted) {
                    settingsRepository.initializeValues()
                    _uiState.value = ElSparUiState.SelectArea(currentPriceArea = settings.area)
                } else {
                    update()
                }
            }
        }
    }

    private fun update() {
        getPowerPrice()
        cache()
    }

    fun getPowerPrice() {
        viewModelScope.launch(Dispatchers.IO) {
            settings.collect { settings ->
                _uiState.value = try {
                    ElSparUiState.Success(
                        currentPricePeriod = currentPricePeriod,
                        currentEndDate = currentEndDate,
                        priceList = getPowerPriceUseCase(
                            endDate = currentEndDate,
                            period = currentPricePeriod,
                            area = settings.area
                        ),
                        currentPrice = getPowerPriceUseCase(
                            endDate = LocalDate.now(),
                            period = PricePeriod.DAY,
                            area = settings.area
                        )
                    )
                } catch (e: NoConnectionException) {
                    ElSparUiState.Error
                }
            }
        }
    }

    private fun cache(
        buffer: PricePeriod = PricePeriod.MONTH
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            settings.collect { settings ->
                getPowerPriceUseCase(
                    endDate = LocalDate.now(),
                    period = buffer,
                    area = settings.area
                )
            }
        }
    }

    fun updatePricePeriod(pricePeriod: PricePeriod) {
        currentPricePeriod = pricePeriod
        update()
    }

    fun updatePriceArea(priceArea: PriceArea) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updatePriceArea(priceArea)
            settingsRepository.initialStartupCompleted()
            update()
        }
    }

    /*fun updateCoordinatesForPriceArea(priceArea: PriceArea){
        currentCoordinates = when (priceArea){
            PriceArea.NO1 -> "60.79" to "11.08"
            PriceArea.NO2 -> "59.14" to "7.80"
            PriceArea.NO3 -> "63.03" to "9.65"
            PriceArea.NO4 -> "68.29" to "17.53"
            else -> "60.83" to "7.61"
        }
        //Log.d("priceCoords",currentCoordinates.toString())
    }*/

    fun dateForward() {
        currentEndDate = currentEndDate.plusDays(currentPricePeriod.days.toLong())
        update()
    }

    fun dateBack() {
        currentEndDate = currentEndDate.minusDays(currentPricePeriod.days.toLong())
        update()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                (this[APPLICATION_KEY] as ElSparApplication).container.run {
                    ElSparViewModel(
                        getPowerPriceUseCase = getPowerPriceUseCase,
                        settingsRepository = settingsRepository
                    )
                }
            }
        }
    }
}