package lg.voltup.controller.dto

import java.time.LocalDateTime

data class PointResponse(
    val id: Long,
    val amount: Int,
    val usedAmount: Int,
    val availableAmount: Int,
    val status: String,
    val earnedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val isExpired: Boolean,
    val daysUntilExpiry: Long
)

data class PointBalanceResponse(
    val totalBalance: Int,
    val expiringWithin7Days: Int
)

data class ExpiringPointsResponse(
    val points: List<PointResponse>,
    val totalExpiringAmount: Int
)
