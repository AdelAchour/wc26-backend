package com.adel.features.predictions.service

import com.adel.features.matches.data.MatchRepository
import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.TeamCodes
import com.adel.features.predictions.data.PredictionRepository
import com.adel.features.predictions.domain.Prediction
import java.time.OffsetDateTime
import java.time.ZoneOffset

class PredictionService(
    private val repository: PredictionRepository,
    private val matchRepository: MatchRepository,
) {
    suspend fun getMyPredictions(userId: Long): List<Prediction> =
        repository.findByUser(userId)

    /**
     * Create or update the user's prediction for a match. The server is the
     * source of truth for the lock: predictions are editable only while the
     * match is still upcoming (now < kickoff). Knockout fixtures whose teams
     * aren't decided yet (placeholder names) can't be predicted.
     */
    suspend fun upsertPrediction(
        userId: Long,
        matchId: Long,
        homeScore: Short,
        awayScore: Short,
    ): UpsertPredictionResult {
        if (homeScore.toInt() !in VALID_SCORE_RANGE || awayScore.toInt() !in VALID_SCORE_RANGE) {
            return UpsertPredictionResult.InvalidScores
        }

        val match = matchRepository.findById(matchId)
            ?: return UpsertPredictionResult.MatchNotFound

        if (!areTeamsConfirmed(match)) return UpsertPredictionResult.TeamsNotConfirmed

        // Lock at kickoff — compared on the instant, so offsets don't matter.
        if (!OffsetDateTime.now(ZoneOffset.UTC).isBefore(match.kickoffAt)) {
            return UpsertPredictionResult.Locked
        }

        val prediction = repository.upsert(userId, matchId, homeScore, awayScore)
        return UpsertPredictionResult.Success(prediction)
    }

    /** A match is predictable only once both teams resolve to real team codes. */
    private fun areTeamsConfirmed(match: Match): Boolean =
        TeamCodes.fromTeamName(match.homeTeam) != null && TeamCodes.fromTeamName(match.awayTeam) != null

    companion object {
        val VALID_SCORE_RANGE = 0..99
    }
}

sealed interface UpsertPredictionResult {
    data class Success(val prediction: Prediction) : UpsertPredictionResult
    data object MatchNotFound : UpsertPredictionResult
    data object TeamsNotConfirmed : UpsertPredictionResult
    data object Locked : UpsertPredictionResult
    data object InvalidScores : UpsertPredictionResult
}
