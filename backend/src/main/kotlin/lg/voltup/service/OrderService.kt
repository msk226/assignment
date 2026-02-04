package lg.voltup.service

import lg.voltup.controller.dto.OrderCreateRequest
import lg.voltup.controller.dto.OrderResponse
import lg.voltup.entity.Order
import lg.voltup.entity.Point
import lg.voltup.entity.Product
import lg.voltup.exception.InsufficientPointsException
import lg.voltup.exception.OrderNotFoundException
import lg.voltup.exception.ProductNotFoundException
import lg.voltup.repository.OrderRepository
import lg.voltup.repository.PointRepository
import lg.voltup.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val pointRepository: PointRepository
) {

    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAllByOrderByCreatedAtDesc().map { it.toResponse() }
    }
    
    @Transactional
    fun createOrder(userId: Long, request: OrderCreateRequest): OrderResponse {
        // 1. 상품 조회 (비관적 락 적용)
        val product = findProductByIdWithLock(request.productId)
        product.validatePurchasable()

        // 2. 포인트 검증 및 차감 (비관적 락 적용, 원자적 연산)
        deductPointsWithValidation(userId, product.price)

        // 3. 재고 차감
        product.purchase()

        // 4. 주문 생성
        val order = Order.create(
            userId = userId,
            productId = product.id,
            productName = product.name,
            pointsUsed = product.price
        )

        return orderRepository.save(order).toResponse()
    }

    @Transactional(readOnly = true)
    fun getUserOrders(userId: Long): List<OrderResponse> {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.toResponse() }
    }

    @Transactional
    fun cancelOrder(orderId: Long): OrderResponse {
        val order = findOrderById(orderId)
        order.cancel()

        refundPoints(order.userId, order.pointsUsed)
        restoreProductStock(order.productId)

        return order.toResponse()
    }

    private fun findOrderById(orderId: Long): Order {
        return orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException("주문을 찾을 수 없습니다.") }
    }

    private fun refundPoints(userId: Long, amount: Int) {
        pointRepository.save(Point.createWithDefaultExpiry(userId, amount))
    }

    private fun restoreProductStock(productId: Long) {
        productRepository.findById(productId).ifPresent { it.restoreStock() }
    }

    private fun findProductByIdWithLock(productId: Long): Product {
        return productRepository.findByIdWithLock(productId)
            ?: throw ProductNotFoundException("상품을 찾을 수 없습니다.")
    }

    private fun deductPointsWithValidation(userId: Long, requiredAmount: Int) {
        val validPoints =
            pointRepository.findValidPointsByUserIdWithLock(userId, LocalDateTime.now())
        val availableBalance = validPoints.sumOf { it.availableAmount }

        if (availableBalance < requiredAmount) {
            throw InsufficientPointsException(
                "포인트가 부족합니다. (보유: ${availableBalance}p, 필요: ${requiredAmount}p)"
            )
        }

        var remaining = requiredAmount
        for (point in validPoints) {
            if (remaining <= 0) break
            remaining -= point.use(remaining)
        }
    }

    private fun Order.toResponse() = OrderResponse(
        id = id,
        userId = userId,
        productId = productId,
        productName = productName,
        pointsUsed = pointsUsed,
        status = status,
        createdAt = createdAt
    )
}
