package lg.voltup.service

import lg.voltup.controller.dto.ExpiringPointsResponse
import lg.voltup.controller.dto.PointBalanceResponse
import lg.voltup.controller.dto.PointResponse
import lg.voltup.entity.Point
import lg.voltup.repository.PointRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PointService(
    private val pointRepository: PointRepository
) {

    fun getPoints(userId: Long): List<PointResponse> {
        return pointRepository.findAllByUserId(userId)
            .map { it.toResponse() }
            .sortedBy { it.expiresAt }
    }

    fun getBalance(userId: Long): PointBalanceResponse {
        val now = LocalDateTime.now()
        val totalBalance = pointRepository.getAvailableBalance(userId, now)
        val expiringPoints = pointRepository.findExpiringPoints(userId, now, now.plusDays(7))
        val expiringAmount = expiringPoints.sumOf { it.availableAmount }

        return PointBalanceResponse(
            totalBalance = totalBalance,
            expiringWithin7Days = expiringAmount
        )
    }

    fun getExpiringPoints(userId: Long): ExpiringPointsResponse {
        val now = LocalDateTime.now()
        val expiringPoints = pointRepository.findExpiringPoints(userId, now, now.plusDays(7))

        return ExpiringPointsResponse(
            points = expiringPoints.map { it.toResponse() },
            totalExpiringAmount = expiringPoints.sumOf { it.availableAmount }
        )
    }

    private fun Point.toResponse() = PointResponse(
        id = id,
        amount = amount,
        usedAmount = usedAmount,
        availableAmount = availableAmount,
        earnedAt = earnedAt,
        expiresAt = expiresAt,
        isExpired = isExpired,
        daysUntilExpiry = daysUntilExpiry
    )
}
