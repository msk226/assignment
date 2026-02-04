package lg.voltup.entity

import jakarta.persistence.*
import java.time.LocalDateTime

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

    val earnedAt: LocalDateTime = LocalDateTime.now(),

    val expiresAt: LocalDateTime
) {
    companion object {
        private const val DEFAULT_EXPIRY_DAYS = 30L

        fun create(userId: Long, amount: Int, expiresAt: LocalDateTime): Point {
            require(amount > 0) { "포인트는 0보다 커야 합니다." }
            return Point(
                userId = userId,
                amount = amount,
                expiresAt = expiresAt
            )
        }

        fun createWithDefaultExpiry(userId: Long, amount: Int): Point {
            return create(userId, amount, LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS))
        }
    }
}
