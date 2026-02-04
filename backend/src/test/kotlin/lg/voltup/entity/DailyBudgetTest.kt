package lg.voltup.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DailyBudgetTest {

    @Test
    fun `create 메서드로 일일 예산을 생성할 수 있다`() {
        val date = LocalDate.now()
        val totalBudget = 200000

        val dailyBudget = DailyBudget.create(date, totalBudget)

        assertAll(
            { assertNotNull(dailyBudget) },
            { assertEquals(date, dailyBudget.date) },
            { assertEquals(totalBudget, dailyBudget.totalBudget) },
            { assertEquals(0, dailyBudget.usedBudget) }
        )
    }

    @Test
    fun `create 메서드에서 totalBudget을 지정하지 않으면 기본값 100000이 적용된다`() {
        val date = LocalDate.now()

        val dailyBudget = DailyBudget.create(date)

        assertEquals(100000, dailyBudget.totalBudget)
    }

    @Test
    fun `일일 예산 생성 시 usedBudget은 0으로 초기화된다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now())

        assertEquals(0, dailyBudget.usedBudget)
    }

    @Test
    fun `usedBudget을 변경할 수 있다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        dailyBudget.usedBudget = 50000

        assertEquals(50000, dailyBudget.usedBudget)
    }
}
