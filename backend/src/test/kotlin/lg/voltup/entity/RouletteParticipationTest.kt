package lg.voltup.entity

import lg.voltup.entity.enums.ParticipationStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.*

class RouletteParticipationTest {

    @Test
    fun `create 메서드로 룰렛 참여 기록을 생성할 수 있다`() {
        val userId = 1L
        val date = LocalDate.now()
        val points = 100

        val participation = RouletteParticipation.create(userId, date, points)

        assertAll(
            { assertNotNull(participation) },
            { assertEquals(userId, participation.userId) },
            { assertEquals(date, participation.date) },
            { assertEquals(points, participation.points) },
            { assertNotNull(participation.createdAt) }
        )
    }

    @Test
    fun `룰렛 참여 생성 시 id는 0으로 초기화된다`() {
        val participation = RouletteParticipation.create(1L, LocalDate.now(), 100)

        assertEquals(0L, participation.id)
    }

    @Test
    fun `포인트가 0인 룰렛 참여를 생성할 수 있다`() {
        val participation = RouletteParticipation.create(1L, LocalDate.now(), 0)

        assertEquals(0, participation.points)
    }

    @Test
    fun `룰렛 참여 생성 시 상태는 PARTICIPATED로 초기화된다`() {
        val participation = RouletteParticipation.create(1L, LocalDate.now(), 100)

        assertEquals(ParticipationStatus.PARTICIPATED, participation.status)
        assertFalse(participation.isCancelled)
    }

    @Test
    fun `룰렛 참여를 취소하면 상태가 CANCELLED로 변경된다`() {
        val participation = RouletteParticipation.create(1L, LocalDate.now(), 100)

        participation.cancel()

        assertEquals(ParticipationStatus.CANCELLED, participation.status)
        assertTrue(participation.isCancelled)
        assertNotNull(participation.cancelledAt)
    }

    @Test
    fun `이미 취소된 참여를 다시 취소하면 예외가 발생한다`() {
        val participation = RouletteParticipation.create(1L, LocalDate.now(), 100)
        participation.cancel()

        val exception = assertThrows<IllegalStateException> {
            participation.cancel()
        }

        assertEquals("이미 취소된 참여입니다.", exception.message)
    }

    @Test
    fun `취소되지 않은 참여의 cancelledAt은 null이다`() {
        val participation = RouletteParticipation.create(1L, LocalDate.now(), 100)

        assertNull(participation.cancelledAt)
    }
}
