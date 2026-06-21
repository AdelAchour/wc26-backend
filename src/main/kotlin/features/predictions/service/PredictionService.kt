package com.adel.features.predictions.service

import com.adel.common.pagination.PaginatedResult
import com.adel.features.matches.data.MatchRepository
import com.adel.features.matches.domain.Match
import com.adel.features.matches.domain.TeamCodes
import com.adel.features.predictions.data.PredictionRepository
import com.adel.features.predictions.data.PredictionStandingsRepository
import com.adel.features.predictions.domain.LeaderboardEntry
import com.adel.features.predictions.domain.MyRank
import com.adel.features.predictions.domain.Prediction
import com.adel.features.predictions.domain.PredictionScoring
import com.adel.features.predictions.domain.PredictionStats
import com.adel.features.users.data.UserRepository
import java.time.OffsetDateTime
import java.time.ZoneOffset

class PredictionService(
    private val repository: PredictionRepository,
    private val standingsRepository: PredictionStandingsRepository,
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
) {
    suspend fun getMyPredictions(userId: Long): List<Prediction> =
        repository.findByUser(userId)

    /** Profile prediction stats for a user. Null if the user doesn't exist. */
    suspend fun getUserStats(userId: Long): PredictionStats? {
        userRepository.findById(userId) ?: return null
        val standing = standingsRepository.findRankedStanding(userId)
        return PredictionStats(
            userId = userId,
            rank = standing?.rank,
            totalPoints = standing?.totalPoints ?: 0,
            exactCount = standing?.exactCount ?: 0,
            gradedCount = standing?.gradedCount ?: 0,
            predictionsCount = repository.countByUser(userId).toInt(),
        )
    }

    suspend fun getLeaderboard(limit: Int, offset: Long): PaginatedResult<LeaderboardEntry> =
        PaginatedResult(
            items = standingsRepository.topEntries(limit, offset),
            total = standingsRepository.count(),
            limit = limit,
            offset = offset,
        )

    suspend fun getMyRank(userId: Long): MyRank? = standingsRepository.findMyRank(userId)

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

    /**
     * Grade every prediction for a finished match. Computes points once per
     * distinct predicted scoreline and bulk-updates. Idempotent — re-running
     * after a corrected result simply recomputes. No-op if the match has no
     * final score yet.
     */
    suspend fun gradeMatch(match: Match) {
        val actualHome = match.homeScore ?: return
        val actualAway = match.awayScore ?: return

        val predictions = repository.findByMatch(match.id)

        predictions
            .map { it.homeScore to it.awayScore }
            .distinct()
            .forEach { (predHome, predAway) ->
                val points = PredictionScoring.pointsFor(predHome, predAway, actualHome, actualAway)
                repository.updatePointsForScoreline(match.id, predHome, predAway, points)
            }

        // Refresh leaderboard standings for everyone who predicted this match.
        standingsRepository.recomputeForUsers(predictions.map { it.userId })
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
