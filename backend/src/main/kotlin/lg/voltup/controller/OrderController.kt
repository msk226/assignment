package lg.voltup.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lg.voltup.controller.dto.OrderCreateRequest
import lg.voltup.controller.dto.OrderResponse
import lg.voltup.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "주문", description = "주문 관련 API (사용자)")
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @Operation(summary = "상품 주문", description = "포인트를 사용하여 상품을 주문합니다.")
    @PostMapping
    fun createOrder(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody request: OrderCreateRequest
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.createOrder(userId, request))
    }

    @Operation(summary = "내 주문 내역", description = "내 주문 내역을 조회합니다.")
    @GetMapping
    fun getOrders(@RequestHeader("X-User-Id") userId: Long): ResponseEntity<List<OrderResponse>> {
        return ResponseEntity.ok(orderService.getUserOrders(userId))
    }
}
