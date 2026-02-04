package lg.voltup.service

import lg.voltup.controller.dto.OrderCreateRequest
import lg.voltup.entity.Point
import lg.voltup.entity.Product
import lg.voltup.entity.User
import lg.voltup.entity.enums.OrderStatus
import lg.voltup.exception.InsufficientPointsException
import lg.voltup.exception.InsufficientStockException
import lg.voltup.exception.OrderAlreadyCancelledException
import lg.voltup.exception.OrderNotFoundException
import lg.voltup.exception.ProductNotAvailableException
import lg.voltup.exception.ProductNotFoundException
import lg.voltup.repository.OrderRepository
import lg.voltup.repository.PointRepository
import lg.voltup.repository.ProductRepository
import lg.voltup.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class OrderServiceTest @Autowired constructor(
    private val orderService: OrderService,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val pointRepository: PointRepository,
    private val orderRepository: OrderRepository
) {

    private lateinit var testUser: User
    private lateinit var testProduct: Product

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(User.create("testuser"))
        testProduct = productRepository.save(
            Product.create(
                name = "테스트상품",
                description = "테스트 설명",
                price = 1000,
                stock = 10,
                imageUrl = null
            )
        )
        // 테스트 유저에게 포인트 지급
        pointRepository.save(
            Point.create(
                userId = testUser.id,
                amount = 5000,
                expiresAt = LocalDateTime.now().plusDays(30)
            )
        )
    }

    @Test
    @DisplayName("상품 주문 시 주문이 생성되고 포인트가 차감된다")
    fun createOrder_shouldCreateOrderAndDeductPoints() {
        val request = OrderCreateRequest(productId = testProduct.id)

        val result = orderService.createOrder(testUser.id, request)

        assertEquals(testProduct.id, result.productId)
        assertEquals(testProduct.name, result.productName)
        assertEquals(testProduct.price, result.pointsUsed)
        assertEquals(OrderStatus.COMPLETED, result.status)

        // 포인트 차감 확인
        val remainingBalance = pointRepository.getAvailableBalance(testUser.id, LocalDateTime.now())
        assertEquals(4000, remainingBalance)

        // 재고 차감 확인
        val updatedProduct = productRepository.findById(testProduct.id).get()
        assertEquals(9, updatedProduct.stock)
    }

    @Test
    @DisplayName("존재하지 않는 상품 주문 시 예외가 발생한다")
    fun createOrder_shouldThrowExceptionWhenProductNotFound() {
        val request = OrderCreateRequest(productId = 9999L)

        val exception = assertThrows<ProductNotFoundException> {
            orderService.createOrder(testUser.id, request)
        }

        assertEquals("상품을 찾을 수 없습니다.", exception.message)
    }

    @Test
    @DisplayName("포인트가 부족하면 예외가 발생한다")
    fun createOrder_shouldThrowExceptionWhenInsufficientPoints() {
        // 비싼 상품 생성
        val expensiveProduct = productRepository.save(
            Product.create(
                name = "비싼상품",
                description = null,
                price = 10000,
                stock = 10,
                imageUrl = null
            )
        )
        val request = OrderCreateRequest(productId = expensiveProduct.id)

        val exception = assertThrows<InsufficientPointsException> {
            orderService.createOrder(testUser.id, request)
        }

        assertTrue(exception.message!!.contains("포인트가 부족합니다"))
    }

    @Test
    @DisplayName("재고가 없으면 예외가 발생한다")
    fun createOrder_shouldThrowExceptionWhenOutOfStock() {
        // 재고 없는 상품 생성
        val outOfStockProduct = productRepository.save(
            Product.create(
                name = "품절상품",
                description = null,
                price = 100,
                stock = 0,
                imageUrl = null
            )
        )
        val request = OrderCreateRequest(productId = outOfStockProduct.id)

        val exception = assertThrows<InsufficientStockException> {
            orderService.createOrder(testUser.id, request)
        }

        assertEquals("재고가 부족합니다.", exception.message)
    }

    @Test
    @DisplayName("판매 중지된 상품은 주문할 수 없다")
    fun createOrder_shouldThrowExceptionWhenProductNotActive() {
        // 판매 중지 상품 생성
        val inactiveProduct = productRepository.save(
            Product.create(
                name = "판매중지상품",
                description = null,
                price = 100,
                stock = 10,
                imageUrl = null
            )
        )
        inactiveProduct.isActive = false
        productRepository.save(inactiveProduct)

        val request = OrderCreateRequest(productId = inactiveProduct.id)

        val exception = assertThrows<ProductNotAvailableException> {
            orderService.createOrder(testUser.id, request)
        }

        assertEquals("판매 중인 상품이 아닙니다.", exception.message)
    }

    @Test
    @DisplayName("만료일이 가까운 포인트부터 먼저 사용된다")
    fun createOrder_shouldUseExpiringPointsFirst() {
        // 기존 포인트 외에 만료일이 더 가까운 포인트 추가
        val soonExpiringPoint = pointRepository.save(
            Point.create(
                userId = testUser.id,
                amount = 500,
                expiresAt = LocalDateTime.now().plusDays(5)
            )
        )
        val laterExpiringPoint = pointRepository.save(
            Point.create(
                userId = testUser.id,
                amount = 500,
                expiresAt = LocalDateTime.now().plusDays(60)
            )
        )

        // 1000원 상품 주문
        val request = OrderCreateRequest(productId = testProduct.id)
        orderService.createOrder(testUser.id, request)

        // 만료일이 가까운 포인트부터 사용되었는지 확인
        val usedSoonExpiring = pointRepository.findById(soonExpiringPoint.id).get()
        assertEquals(500, usedSoonExpiring.usedAmount) // 전액 사용됨
    }

    @Test
    @DisplayName("주문 내역을 조회할 수 있다")
    fun getUserOrders_shouldReturnUserOrders() {
        val request = OrderCreateRequest(productId = testProduct.id)
        orderService.createOrder(testUser.id, request)

        val orders = orderService.getUserOrders(testUser.id)

        assertEquals(1, orders.size)
        assertEquals(testProduct.name, orders[0].productName)
    }

    @Test
    @DisplayName("여러 상품을 연속으로 주문할 수 있다")
    fun createOrder_shouldAllowMultipleOrders() {
        // 충분한 포인트 추가
        pointRepository.save(
            Point.create(
                userId = testUser.id,
                amount = 10000,
                expiresAt = LocalDateTime.now().plusDays(30)
            )
        )

        val request = OrderCreateRequest(productId = testProduct.id)

        // 3번 연속 주문
        repeat(3) {
            orderService.createOrder(testUser.id, request)
        }

        val orders = orderService.getUserOrders(testUser.id)
        assertEquals(3, orders.size)

        // 재고 확인
        val updatedProduct = productRepository.findById(testProduct.id).get()
        assertEquals(7, updatedProduct.stock)
    }

    // cancelOrder 테스트
    @Test
    @DisplayName("주문을 취소하면 포인트가 환불된다")
    fun cancelOrder_shouldRefundPoints() {
        val request = OrderCreateRequest(productId = testProduct.id)
        val order = orderService.createOrder(testUser.id, request)

        val balanceBeforeCancel = pointRepository.getAvailableBalance(testUser.id, LocalDateTime.now())

        orderService.cancelOrder(order.id)

        val balanceAfterCancel = pointRepository.getAvailableBalance(testUser.id, LocalDateTime.now())
        assertEquals(balanceBeforeCancel + testProduct.price, balanceAfterCancel)
    }

    @Test
    @DisplayName("주문을 취소하면 재고가 복구된다")
    fun cancelOrder_shouldRestoreStock() {
        val request = OrderCreateRequest(productId = testProduct.id)
        val order = orderService.createOrder(testUser.id, request)

        val stockBeforeCancel = productRepository.findById(testProduct.id).get().stock

        orderService.cancelOrder(order.id)

        val stockAfterCancel = productRepository.findById(testProduct.id).get().stock
        assertEquals(stockBeforeCancel + 1, stockAfterCancel)
    }

    @Test
    @DisplayName("주문 취소 후 상태가 CANCELLED로 변경된다")
    fun cancelOrder_shouldChangeStatusToCancelled() {
        val request = OrderCreateRequest(productId = testProduct.id)
        val order = orderService.createOrder(testUser.id, request)

        val cancelledOrder = orderService.cancelOrder(order.id)

        assertEquals(OrderStatus.CANCELLED, cancelledOrder.status)
    }

    @Test
    @DisplayName("존재하지 않는 주문을 취소하면 예외가 발생한다")
    fun cancelOrder_shouldThrowExceptionWhenOrderNotFound() {
        assertThrows<OrderNotFoundException> {
            orderService.cancelOrder(9999L)
        }
    }

    @Test
    @DisplayName("이미 취소된 주문을 다시 취소하면 예외가 발생한다")
    fun cancelOrder_shouldThrowExceptionWhenAlreadyCancelled() {
        val request = OrderCreateRequest(productId = testProduct.id)
        val order = orderService.createOrder(testUser.id, request)
        orderService.cancelOrder(order.id)

        assertThrows<OrderAlreadyCancelledException> {
            orderService.cancelOrder(order.id)
        }
    }
}
