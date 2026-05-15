package com.adel.features.matches.di

import com.adel.features.matches.data.MatchRepository
import com.adel.features.matches.data.MatchRepositoryImpl
import com.adel.features.matches.service.MatchService

class MatchComponent {
    private val repository: MatchRepository by lazy { MatchRepositoryImpl() }
    val service: MatchService by lazy { MatchService(repository) }
}