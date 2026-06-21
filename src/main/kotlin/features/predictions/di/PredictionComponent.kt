package com.adel.features.predictions.di

import com.adel.features.matches.data.MatchRepository
import com.adel.features.predictions.data.PredictionRepository
import com.adel.features.predictions.data.PredictionRepositoryImpl
import com.adel.features.predictions.service.PredictionService

class PredictionComponent(
    matchRepository: MatchRepository,
) {
    val repository: PredictionRepository by lazy { PredictionRepositoryImpl() }
    val service: PredictionService by lazy { PredictionService(repository, matchRepository) }
}
