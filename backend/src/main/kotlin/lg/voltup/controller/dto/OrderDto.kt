package lg.voltup.controller.dto

import lg.voltup.entity.enums.OrderStatus
import java.time.LocalDateTime

data class OrderCreateRequest(
    val productId: Long
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val productName: String,
    val pointsUsed: Int,
    val status: OrderStatus,
    val createdAt: LocalDateTime
)
