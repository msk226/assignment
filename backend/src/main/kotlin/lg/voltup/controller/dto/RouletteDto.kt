package lg.voltup.controller.dto

import java.time.LocalDateTime

data class RouletteSpinResponse(
    val points: Int,
    val remainingBudget: Int,
    val message: String
)

data class RouletteStatusResponse(
    val hasParticipatedToday: Boolean,
    val todayPoints: Int?,
    val remainingBudget: Int,
    val totalBudget: Int
)

data class RouletteParticipationResponse(
    val id: Long,
    val userId: Long,
    val nickname: String?,
    val points: Int,
    val createdAt: LocalDateTime
)
