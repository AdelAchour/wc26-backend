package com.adel.features.predictions.domain

/**
 * The prediction scoring model (MVP: 2-tier).
 *
 *   - Exact scoreline           → 5 points
 *   - Correct result (W/D/L)    → 3 points
 *   - Wrong                      → 0 points
 *
 * Pure function of predicted vs actual scoreline. Knockout matches are graded
 * on their end-of-play score (extra time included, penalty shootout excluded) —
 * that is purely a data-entry concern (the stored match score must be the
 * pre-shootout score), so no special handling is needed here.
 */
object PredictionScoring {

    const val EXACT_SCORE_POINTS: Short = 5
    const val CORRECT_RESULT_POINTS: Short = 3
    const val NO_POINTS: Short = 0

    fun pointsFor(
        predictedHome: Short,
        predictedAway: Short,
        actualHome: Short,
        actualAway: Short,
    ): Short {
        if (predictedHome == actualHome && predictedAway == actualAway) return EXACT_SCORE_POINTS
        return if (outcome(predictedHome, predictedAway) == outcome(actualHome, actualAway)) {
            CORRECT_RESULT_POINTS
        } else {
            NO_POINTS
        }
    }

    /** -1 away win, 0 draw, +1 home win. */
    private fun outcome(home: Short, away: Short): Int = home.compareTo(away).coerceIn(-1, 1)
}
