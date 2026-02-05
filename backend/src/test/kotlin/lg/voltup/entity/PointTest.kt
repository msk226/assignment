package lg.voltup.entity

import lg.voltup.entity.enums.PointStatus
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
            { assertEquals(expiresAt, point.expiresAt) },
            { assertEquals(PointStatus.EARNED, point.status) }
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

    @Test
    fun `포인트 생성 시 상태는 EARNED로 초기화된다`() {
        val point = Point.createWithDefaultExpiry(1L, 100)

        assertEquals(PointStatus.EARNED, point.status)
        assertEquals(PointStatus.EARNED, point.effectiveStatus)
    }

    @Test
    fun `포인트 취소 시 상태가 CANCELED로 변경된다`() {
        val point = Point.createWithDefaultExpiry(1L, 100)

        point.cancel()

        assertEquals(PointStatus.CANCELED, point.status)
        assertEquals(PointStatus.CANCELED, point.effectiveStatus)
    }

    @Test
    fun `만료된 포인트의 effectiveStatus는 EXPIRED이다`() {
        val point = Point.create(1L, 100, LocalDateTime.now().minusDays(1))

        assertEquals(PointStatus.EARNED, point.status)
        assertEquals(PointStatus.EXPIRED, point.effectiveStatus)
    }

    @Test
    fun `취소된 포인트의 effectiveStatus는 만료 여부와 관계없이 CANCELED이다`() {
        val point = Point.create(1L, 100, LocalDateTime.now().minusDays(1))
        point.cancel()

        assertEquals(PointStatus.CANCELED, point.status)
        assertEquals(PointStatus.CANCELED, point.effectiveStatus)
    }
}
