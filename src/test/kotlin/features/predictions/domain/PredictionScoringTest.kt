package com.adel.features.predictions.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class PredictionScoringTest {

    private fun points(ph: Int, pa: Int, ah: Int, aa: Int): Short =
        PredictionScoring.pointsFor(ph.toShort(), pa.toShort(), ah.toShort(), aa.toShort())

    @Test
    fun exactScore_homeWin() {
        assertEquals(5, points(2, 1, 2, 1).toInt())
    }

    @Test
    fun exactScore_draw() {
        assertEquals(5, points(0, 0, 0, 0).toInt())
    }

    @Test
    fun exactScore_awayWin() {
        assertEquals(5, points(1, 3, 1, 3).toInt())
    }

    @Test
    fun correctResult_homeWin_wrongScore() {
        assertEquals(3, points(2, 0, 3, 1).toInt())
    }

    @Test
    fun correctResult_awayWin_wrongScore() {
        assertEquals(3, points(0, 1, 1, 3).toInt())
    }

    @Test
    fun correctResult_draw_wrongScore() {
        assertEquals(3, points(1, 1, 2, 2).toInt())
    }

    @Test
    fun wrong_predictedHomeWin_actualAwayWin() {
        assertEquals(0, points(2, 1, 1, 2).toInt())
    }

    @Test
    fun wrong_predictedDraw_actualHomeWin() {
        assertEquals(0, points(1, 1, 2, 1).toInt())
    }

    @Test
    fun wrong_predictedHomeWin_actualDraw() {
        assertEquals(0, points(2, 1, 1, 1).toInt())
    }
}
