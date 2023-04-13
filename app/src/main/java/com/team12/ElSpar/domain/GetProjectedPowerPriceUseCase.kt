package com.team12.ElSpar.domain

import com.team12.ElSpar.data.PowerRepository
import com.team12.ElSpar.data.WeatherRepository
import com.team12.ElSpar.model.PriceArea
import java.time.LocalDate
import java.time.LocalDateTime

class GetProjectedPowerPriceUseCase(
    powerRepository: PowerRepository,
    weatherRepository: WeatherRepository,
) {
    operator fun invoke(date: LocalDate, area: PriceArea): Map<LocalDateTime, Double> {
        val projectedPriceData = mutableMapOf<LocalDateTime, Double>()
        for (h in 0..23) {
            projectedPriceData[date.atStartOfDay().plusHours(h.toLong())] = 0.0
        }
        return projectedPriceData
    }

}