package lg.voltup.controller.dto

import java.time.LocalDateTime

data class RouletteSpinResponse(
    val points: Int,
    val remainingBudget: Int,
    val message: String,
)

data class RouletteStatusResponse(
    val hasParticipatedToday: Boolean,
    val todayPoints: Int?,
    val remainingBudget: Int,
    val totalBudget: Int,
)

data class RouletteParticipationResponse(
    val id: Long,
    val userId: Long,
    val nickname: String?,
    val points: Int,
    val status: String,
    val createdAt: LocalDateTime,
    val cancelledAt: LocalDateTime?,
)

data class RouletteHistoryResponse(
    val id: Long,
    val points: Int,
    val date: java.time.LocalDate,
    val isCancelled: Boolean,
    val isCancellable: Boolean,
    val createdAt: LocalDateTime,
    val cancelledAt: LocalDateTime?,
)

data class CancelParticipationResponse(
    val participationId: Long,
    val cancelledPoints: Int,
    val budgetRestored: Boolean,
    val message: String,
)
