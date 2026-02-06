package lg.voltup.entity

import jakarta.persistence.*
import lg.voltup.entity.enums.PointStatus
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "points")
class Point private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    val amount: Int,
    var usedAmount: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PointStatus = PointStatus.EARNED,
    val earnedAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime,
) {
    companion object {
        private const val DEFAULT_EXPIRY_DAYS = 30L

        fun create(
            userId: Long,
            amount: Int,
            expiresAt: LocalDateTime,
        ): Point {
            require(amount > 0) { "포인트는 0보다 커야 합니다." }
            return Point(
                userId = userId,
                amount = amount,
                status = PointStatus.EARNED,
                expiresAt = expiresAt,
            )
        }

        fun createWithDefaultExpiry(
            userId: Long,
            amount: Int,
        ): Point {
            return create(userId, amount, LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS))
        }
    }

    val effectiveStatus: PointStatus
        get() =
            when {
                status == PointStatus.CANCELED -> PointStatus.CANCELED
                isExpired -> PointStatus.EXPIRED
                else -> PointStatus.EARNED
            }

    fun cancel() {
        status = PointStatus.CANCELED
    }

    val availableAmount: Int
        get() = amount - usedAmount

    val isExpired: Boolean
        get() = LocalDateTime.now().isAfter(expiresAt)

    val isUsable: Boolean
        get() = !isExpired && availableAmount > 0

    val daysUntilExpiry: Long
        get() = if (isExpired) 0 else ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt)

    fun isExpiringWithin(days: Long): Boolean {
        if (isExpired) return false
        return daysUntilExpiry <= days
    }

    fun use(requestedAmount: Int): Int {
        require(requestedAmount > 0) { "사용 포인트는 0보다 커야 합니다." }
        if (!isUsable) return 0

        val actualUsage = minOf(requestedAmount, availableAmount)
        usedAmount += actualUsage
        return actualUsage
    }
}
