package lg.voltup.service

import lg.voltup.controller.dto.OrderCreateRequest
import lg.voltup.entity.Point
import lg.voltup.entity.Product
import lg.voltup.entity.User
import lg.voltup.entity.enums.OrderStatus
import lg.voltup.exception.OrderAlreadyCancelledException
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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class OrderServiceConcurrencyTest
    @Autowired
    constructor(
        private val orderService: OrderService,
        private val userRepository: UserRepository,
        private val productRepository: ProductRepository,
        private val pointRepository: PointRepository,
        private val orderRepository: OrderRepository,
    ) {
        private lateinit var testUsers: List<User>
        private lateinit var testProduct: Product

        @BeforeEach
        fun setUp() {
            // 10명의 테스트 유저 생성
            testUsers =
                (1..10).map { i ->
                    val user = userRepository.save(User.create("testuser$i"))
                    // 각 유저에게 10000 포인트 지급
                    pointRepository.save(
                        Point.create(
                            userId = user.id,
                            amount = 10000,
                            expiresAt = LocalDateTime.now().plusDays(30),
                        ),
                    )
                    user
                }

            // 재고 5개인 상품 생성
            testProduct =
                productRepository.save(
                    Product.create(
                        name = "한정판상품",
                        description = "재고 5개",
                        price = 1000,
                        stock = 5,
                        imageUrl = null,
                    ),
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
            val abundantProduct =
                productRepository.save(
                    Product.create(
                        name = "대량상품",
                        description = null,
                        price = 100,
                        stock = 100,
                        imageUrl = null,
                    ),
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
            val singleStockProduct =
                productRepository.save(
                    Product.create(
                        name = "단일재고상품",
                        description = null,
                        price = 100,
                        stock = 1,
                        imageUrl = null,
                    ),
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

        @Test
        @DisplayName("동시에 같은 주문을 취소하면 1회만 취소된다")
        fun concurrentCancelSameOrder_shouldSucceedOnlyOnce() {
            val user = testUsers[0]

            // 1. 주문 생성
            val order = orderService.createOrder(user.id, OrderCreateRequest(testProduct.id))

            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(threadCount)
            val successCount = AtomicInteger(0)
            val alreadyCancelledCount = AtomicInteger(0)
            val unexpectedExceptions = CopyOnWriteArrayList<Exception>()

            // 2. 동시에 같은 주문 취소 시도
            repeat(threadCount) {
                executor.submit {
                    try {
                        orderService.cancelOrder(order.id)
                        successCount.incrementAndGet()
                    } catch (e: OrderAlreadyCancelledException) {
                        alreadyCancelledCount.incrementAndGet()
                    } catch (e: Exception) {
                        unexpectedExceptions.add(e)
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            executor.shutdown()

            assertTrue(
                unexpectedExceptions.isEmpty(),
                "예상하지 못한 예외가 발생했습니다: ${unexpectedExceptions.map { "${it.javaClass.simpleName}: ${it.message}" }}",
            )
            assertEquals(1, successCount.get(), "취소는 1번만 성공해야 합니다")
            assertEquals(threadCount - 1, alreadyCancelledCount.get(), "나머지는 이미 취소됨 예외가 발생해야 합니다")

            // 3. 주문이 취소 상태인지 확인
            val updatedOrder = orderRepository.findById(order.id).get()
            assertEquals(OrderStatus.CANCELLED, updatedOrder.status, "주문이 취소 상태여야 합니다")

            // 4. 재고가 정확히 1개만 복구되었는지 확인
            val updatedProduct = productRepository.findById(testProduct.id).get()
            assertEquals(5, updatedProduct.stock, "재고가 원래대로 복구되어야 합니다")

            // 5. 포인트가 정확히 1번만 환불되었는지 확인
            val balance = pointRepository.getAvailableBalance(user.id, LocalDateTime.now())
            assertEquals(10000, balance, "포인트가 원래대로 환불되어야 합니다")
        }

        @Test
        @DisplayName("주문 취소와 새 주문이 동시에 발생해도 재고가 정확하게 관리된다")
        fun concurrentCancelAndNewOrder_shouldManageStockCorrectly() {
            // 재고 1개인 상품 생성
            val limitedProduct =
                productRepository.save(
                    Product.create(
                        name = "한정상품",
                        description = null,
                        price = 100,
                        stock = 1,
                        imageUrl = null,
                    ),
                )

            // 1. 첫 번째 유저가 주문
            val firstUser = testUsers[0]
            val firstOrder = orderService.createOrder(firstUser.id, OrderCreateRequest(limitedProduct.id))

            // 이 시점에 재고는 0

            val executor = Executors.newFixedThreadPool(2)
            val startLatch = CountDownLatch(1)
            val endLatch = CountDownLatch(2)

            val cancelSuccess = AtomicInteger(0)
            val orderSuccess = AtomicInteger(0)

            // 2. 동시에 취소 + 새 주문 시도
            val secondUser = testUsers[1]

            executor.submit {
                startLatch.await()
                try {
                    orderService.cancelOrder(firstOrder.id)
                    cancelSuccess.incrementAndGet()
                } catch (e: Exception) {
                    // 취소 실패
                } finally {
                    endLatch.countDown()
                }
            }

            executor.submit {
                startLatch.await()
                try {
                    orderService.createOrder(secondUser.id, OrderCreateRequest(limitedProduct.id))
                    orderSuccess.incrementAndGet()
                } catch (e: Exception) {
                    // 주문 실패 (재고 부족)
                } finally {
                    endLatch.countDown()
                }
            }

            startLatch.countDown() // 동시에 시작
            endLatch.await()
            executor.shutdown()

            // 3. 결과 검증: 재고가 음수가 되면 안 됨
            val updatedProduct = productRepository.findById(limitedProduct.id).get()
            assertTrue(updatedProduct.stock >= 0, "재고가 음수가 되면 안 됩니다")

            // 취소가 성공하면 재고가 복구되고, 새 주문이 성공할 수도 있음
            // 취소가 먼저 되면 재고 1개 → 새 주문 성공 가능
            // 새 주문이 먼저 시도되면 재고 0개이므로 실패

            val totalOrders =
                orderRepository.findAll()
                    .filter { it.productId == limitedProduct.id && it.status != OrderStatus.CANCELLED }
                    .size

            // 재고 + 활성 주문 수 = 원래 재고 (1개)
            assertEquals(1, updatedProduct.stock + totalOrders, "재고와 활성 주문 수의 합이 원래 재고와 같아야 합니다")
        }
    }
