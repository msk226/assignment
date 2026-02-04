package lg.voltup.entity

import jakarta.persistence.*
import lg.voltup.entity.enums.OrderStatus
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    val productName: String,

    val pointsUsed: Int,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.COMPLETED,

    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            userId: Long,
            productId: Long,
            productName: String,
            pointsUsed: Int
        ): Order {
            require(pointsUsed > 0) { "사용 포인트는 0보다 커야 합니다." }
            return Order(
                userId = userId,
                productId = productId,
                productName = productName,
                pointsUsed = pointsUsed
            )
        }
    }

    val isCancelled: Boolean
        get() = status == OrderStatus.CANCELLED

    val isCancellable: Boolean
        get() = !isCancelled
}
