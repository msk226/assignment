package lg.voltup.service

import lg.voltup.controller.dto.OrderCreateRequest
import lg.voltup.entity.Point
import lg.voltup.entity.Product
import lg.voltup.entity.User
import lg.voltup.repository.OrderRepository
import lg.voltup.repository.PointRepository
import lg.voltup.repository.ProductRepository
import lg.voltup.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class OrderServiceConcurrencyTest @Autowired constructor(
    private val orderService: OrderService,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val pointRepository: PointRepository,
    private val orderRepository: OrderRepository
) {

    private lateinit var testUsers: List<User>
    private lateinit var testProduct: Product

    @BeforeEach
    fun setUp() {
        // 10명의 테스트 유저 생성
        testUsers = (1..10).map { i ->
            val user = userRepository.save(User.create("testuser$i"))
            // 각 유저에게 10000 포인트 지급
            pointRepository.save(
                Point.create(
                    userId = user.id,
                    amount = 10000,
                    expiresAt = LocalDateTime.now().plusDays(30)
                )
            )
            user
        }

        // 재고 5개인 상품 생성
        testProduct = productRepository.save(
            Product.create(
                name = "한정판상품",
                description = "재고 5개",
                price = 1000,
                stock = 5,
                imageUrl = null
            )
        )
    }

    @AfterEach
    fun tearDown() {
        orderRepository.deleteAll()
        pointRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("동시에 10명이 재고 5개인 상품을 주문하면 정확히 5개만 주문된다")
    fun concurrentOrders_shouldNotOversell() {
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        testUsers.forEach { user ->
            executor.submit {
                try {
                    orderService.createOrder(user.id, OrderCreateRequest(testProduct.id))
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // 검증: 정확히 5개만 성공해야 함
        assertEquals(5, successCount.get(), "성공한 주문 수가 재고와 일치해야 합니다")
        assertEquals(5, failCount.get(), "실패한 주문 수가 초과 요청과 일치해야 합니다")

        // 재고가 0이어야 함
        val updatedProduct = productRepository.findById(testProduct.id).get()
        assertEquals(0, updatedProduct.stock, "재고가 정확히 0이어야 합니다")

        // 주문 수가 5개여야 함
        val orders = orderRepository.findAll()
        assertEquals(5, orders.size, "주문 수가 재고와 일치해야 합니다")
    }

    @Test
    @DisplayName("동시에 같은 유저가 여러 번 주문해도 포인트가 정확히 차감된다")
    fun concurrentOrders_shouldDeductPointsCorrectly() {
        val user = testUsers[0]

        // 재고 충분한 상품 생성 (100개)
        val abundantProduct = productRepository.save(
            Product.create(
                name = "대량상품",
                description = null,
                price = 100,
                stock = 100,
                imageUrl = null
            )
        )

        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)

        repeat(threadCount) {
            executor.submit {
                try {
                    orderService.createOrder(user.id, OrderCreateRequest(abundantProduct.id))
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 포인트 부족 또는 기타 오류
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // 검증: 포인트 잔액 + 사용된 포인트 = 원래 포인트
        val remainingBalance = pointRepository.getAvailableBalance(user.id, LocalDateTime.now())
        val ordersForUser = orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.id)
        val totalUsed = ordersForUser.sumOf { it.pointsUsed }

        assertEquals(10000, remainingBalance + totalUsed, "포인트 합계가 일치해야 합니다")
        assertTrue(remainingBalance >= 0, "포인트가 음수가 되면 안 됩니다")
    }

    @Test
    @DisplayName("재고가 1개일 때 동시에 5명이 주문하면 정확히 1명만 성공한다")
    fun concurrentOrders_withSingleStock() {
        // 재고 1개인 상품 생성
        val singleStockProduct = productRepository.save(
            Product.create(
                name = "단일재고상품",
                description = null,
                price = 100,
                stock = 1,
                imageUrl = null
            )
        )

        val threadCount = 5
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)

        testUsers.take(threadCount).forEach { user ->
            executor.submit {
                try {
                    orderService.createOrder(user.id, OrderCreateRequest(singleStockProduct.id))
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    // 재고 부족 예외
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // 검증: 정확히 1명만 성공
        assertEquals(1, successCount.get(), "정확히 1명만 주문에 성공해야 합니다")

        // 재고가 0이어야 함
        val updatedProduct = productRepository.findById(singleStockProduct.id).get()
        assertEquals(0, updatedProduct.stock)
    }
}
