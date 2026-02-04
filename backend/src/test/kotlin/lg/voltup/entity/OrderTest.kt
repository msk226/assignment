package lg.voltup.entity

import lg.voltup.entity.enums.OrderStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderTest {

    @Test
    fun `create 메서드로 주문을 생성할 수 있다`() {
        val userId = 1L
        val productId = 1L
        val productName = "테스트상품"
        val pointsUsed = 10000

        val order = Order.create(userId, productId, productName, pointsUsed)

        assertAll(
            { assertNotNull(order) },
            { assertEquals(userId, order.userId) },
            { assertEquals(productId, order.productId) },
            { assertEquals(productName, order.productName) },
            { assertEquals(pointsUsed, order.pointsUsed) },
            { assertEquals(OrderStatus.COMPLETED, order.status) },
            { assertNotNull(order.createdAt) }
        )
    }

    @Test
    fun `사용 포인트가 0일 경우 예외가 발생한다`() {
        val exception = assertThrows<IllegalArgumentException> {
            Order.create(
                userId = 1L,
                productId = 1L,
                productName = "테스트상품",
                pointsUsed = 0
            )
        }

        assertEquals("사용 포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `사용 포인트가 음수일 경우 예외가 발생한다`() {
        val exception = assertThrows<IllegalArgumentException> {
            Order.create(
                userId = 1L,
                productId = 1L,
                productName = "테스트상품",
                pointsUsed = -100
            )
        }

        assertEquals("사용 포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `주문 생성 시 상태는 COMPLETED로 초기화된다`() {
        val order = Order.create(
            userId = 1L,
            productId = 1L,
            productName = "테스트상품",
            pointsUsed = 10000
        )

        assertEquals(OrderStatus.COMPLETED, order.status)
    }

    @Test
    fun `주문 상태를 변경할 수 있다`() {
        val order = Order.create(
            userId = 1L,
            productId = 1L,
            productName = "테스트상품",
            pointsUsed = 10000
        )

        order.status = OrderStatus.CANCELLED

        assertEquals(OrderStatus.CANCELLED, order.status)
    }
}
