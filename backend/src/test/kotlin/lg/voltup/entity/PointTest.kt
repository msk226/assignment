package lg.voltup.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PointTest {

    @Test
    fun `create 메서드로 포인트를 생성할 수 있다`() {
        val userId = 1L
        val amount = 100
        val expiresAt = LocalDateTime.now().plusDays(30)

        val point = Point.create(userId, amount, expiresAt)

        assertAll(
            { assertNotNull(point) },
            { assertEquals(userId, point.userId) },
            { assertEquals(amount, point.amount) },
            { assertEquals(0, point.usedAmount) },
            { assertEquals(expiresAt, point.expiresAt) }
        )
    }

    @Test
    fun `createWithDefaultExpiry 메서드로 기본 만료일이 적용된 포인트를 생성할 수 있다`() {
        val userId = 1L
        val amount = 100
        val beforeCreate = LocalDateTime.now()

        val point = Point.createWithDefaultExpiry(userId, amount)

        val expectedExpiresAt = beforeCreate.plusDays(30)
        assertAll(
            { assertNotNull(point) },
            { assertEquals(userId, point.userId) },
            { assertEquals(amount, point.amount) },
            { assertTrue(point.expiresAt.isAfter(expectedExpiresAt.minusSeconds(1))) },
            { assertTrue(point.expiresAt.isBefore(expectedExpiresAt.plusSeconds(1))) }
        )
    }

    @Test
    fun `포인트 금액이 0일 경우 예외가 발생한다`() {
        val exception = assertThrows<IllegalArgumentException> {
            Point.create(1L, 0, LocalDateTime.now().plusDays(30))
        }

        assertEquals("포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `포인트 금액이 음수일 경우 예외가 발생한다`() {
        val exception = assertThrows<IllegalArgumentException> {
            Point.create(1L, -100, LocalDateTime.now().plusDays(30))
        }

        assertEquals("포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `포인트 생성 시 usedAmount는 0으로 초기화된다`() {
        val point = Point.createWithDefaultExpiry(1L, 100)

        assertEquals(0, point.usedAmount)
    }
}
