package com.adel.features.predictions.di

import com.adel.features.matches.data.MatchRepository
import com.adel.features.predictions.data.PredictionRepository
import com.adel.features.predictions.data.PredictionRepositoryImpl
import com.adel.features.predictions.data.PredictionStandingsRepository
import com.adel.features.predictions.data.PredictionStandingsRepositoryImpl
import com.adel.features.predictions.service.PredictionService

class PredictionComponent(
    matchRepository: MatchRepository,
) {
    val repository: PredictionRepository by lazy { PredictionRepositoryImpl() }
    val standingsRepository: PredictionStandingsRepository by lazy { PredictionStandingsRepositoryImpl() }
    val service: PredictionService by lazy { PredictionService(repository, standingsRepository, matchRepository) }
}
