package com.team12.ElSpar.data

import com.team12.ElSpar.api.HvaKosterStrommenApiService
import com.team12.ElSpar.api.PriceNotAvailableException
import com.team12.ElSpar.model.PriceArea
import java.time.LocalDate
import java.time.LocalDateTime

interface PowerRepository {
    suspend fun getPowerPricesByDate(
        date: LocalDate,
        area: PriceArea
    ): Map<LocalDateTime, Double>
}

class DefaultPowerRepository(
    private val hvaKosterStrommenApiService: HvaKosterStrommenApiService
) : PowerRepository {
    private val localRepo = mutableMapOf<Pair<LocalDate, PriceArea>, Map<LocalDateTime, Double>>()
    override suspend fun getPowerPricesByDate(
        date: LocalDate,
        area: PriceArea
    ): Map<LocalDateTime, Double> {
        val priceData = mutableMapOf<LocalDateTime, Double>()
        val key = Pair(date, area)
        if (localRepo[key] != null) return localRepo[key]!!
        try {
            hvaKosterStrommenApiService
                .getPowerPricesByDate(date, area)
                .forEach {
                    priceData[LocalDateTime.parse(it.time_start.dropLast(6))] = it.NOK_per_kWh * 125
                }
        } catch (e: PriceNotAvailableException) {
            throw e
        }
        localRepo[key] = priceData
        return priceData.toMap()
    }
}