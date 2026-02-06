package lg.voltup.controller.dto

import jakarta.validation.constraints.Positive
import lg.voltup.entity.enums.OrderStatus
import java.time.LocalDateTime

data class OrderCreateRequest(
    @field:Positive(message = "상품 ID는 양수여야 합니다.")
    val productId: Long,
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val productName: String,
    val pointsUsed: Int,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
)
