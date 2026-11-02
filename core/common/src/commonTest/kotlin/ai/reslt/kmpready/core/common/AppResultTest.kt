package ai.reslt.kmpready.core.common

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.result.flatMap
import ai.reslt.kmpready.core.common.result.getOrNull
import ai.reslt.kmpready.core.common.result.map
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppResultTest {

    @Test
    fun map_transformsSuccess() {
        val result: AppResult<Int> = AppResult.Success(2)
        assertEquals(AppResult.Success(4), result.map { it * 2 })
    }

    @Test
    fun map_keepsFailure() {
        val result: AppResult<Int> = AppResult.Failure(AppError.Network)
        assertEquals(AppResult.Failure(AppError.Network), result.map { it * 2 })
    }

    @Test
    fun flatMap_shortCircuitsOnFailure() {
        val result: AppResult<Int> = AppResult.Success(1)
        val chained = result.flatMap<Int, Int> { AppResult.Failure(AppError.Timeout) }
        assertNull(chained.getOrNull())
    }
}
