package lg.voltup.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}
