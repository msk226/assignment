package lg.voltup.entity

import lg.voltup.exception.BudgetExhaustedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

    // remainingBudget 테스트
    @Test
    fun `remainingBudget은 totalBudget에서 usedBudget을 뺀 값이다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 30000

        assertEquals(70000, dailyBudget.remainingBudget)
    }

    @Test
    fun `usedBudget이 0이면 remainingBudget은 totalBudget과 같다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        assertEquals(100000, dailyBudget.remainingBudget)
    }

    // isExhausted 테스트
    @Test
    fun `remainingBudget이 0이면 isExhausted는 true이다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 100000

        assertTrue(dailyBudget.isExhausted)
    }

    @Test
    fun `remainingBudget이 0보다 크면 isExhausted는 false이다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 99999

        assertFalse(dailyBudget.isExhausted)
    }

    // calculateDistributablePoints 테스트
    @Test
    fun `calculateDistributablePoints는 잔여 예산 내에서 최대 요청 금액을 반환한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        val distributable = dailyBudget.calculateDistributablePoints(1000, 100)

        assertEquals(1000, distributable)
    }

    @Test
    fun `calculateDistributablePoints는 잔여 예산이 요청 최대 금액보다 작으면 잔여 예산을 반환한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 99500

        val distributable = dailyBudget.calculateDistributablePoints(1000, 100)

        assertEquals(500, distributable)
    }

    @Test
    fun `calculateDistributablePoints는 예산이 소진되면 BudgetExhaustedException을 던진다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 100000

        val exception = assertThrows<BudgetExhaustedException> {
            dailyBudget.calculateDistributablePoints(1000, 100)
        }

        assertEquals("오늘 예산이 소진되었습니다.", exception.message)
    }

    // distribute 테스트
    @Test
    fun `distribute로 포인트를 지급하면 usedBudget이 증가한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        dailyBudget.distribute(500)

        assertEquals(500, dailyBudget.usedBudget)
    }

    @Test
    fun `distribute를 여러 번 호출하면 usedBudget이 누적된다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        dailyBudget.distribute(500)
        dailyBudget.distribute(300)
        dailyBudget.distribute(200)

        assertEquals(1000, dailyBudget.usedBudget)
    }

    @Test
    fun `distribute에 0을 전달하면 IllegalArgumentException이 발생한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        val exception = assertThrows<IllegalArgumentException> {
            dailyBudget.distribute(0)
        }

        assertEquals("지급 포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `distribute에 음수를 전달하면 IllegalArgumentException이 발생한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        val exception = assertThrows<IllegalArgumentException> {
            dailyBudget.distribute(-100)
        }

        assertEquals("지급 포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `distribute 시 잔여 예산보다 큰 금액을 지급하면 BudgetExhaustedException이 발생한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 99500

        val exception = assertThrows<BudgetExhaustedException> {
            dailyBudget.distribute(1000)
        }

        assertEquals("잔여 예산이 부족합니다.", exception.message)
    }

    @Test
    fun `distribute 시 잔여 예산과 동일한 금액은 지급할 수 있다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 99500

        dailyBudget.distribute(500)

        assertEquals(100000, dailyBudget.usedBudget)
        assertTrue(dailyBudget.isExhausted)
    }

    // restore 테스트
    @Test
    fun `restore로 포인트를 복구하면 usedBudget이 감소한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 50000

        dailyBudget.restore(10000)

        assertEquals(40000, dailyBudget.usedBudget)
    }

    @Test
    fun `restore에 0을 전달하면 IllegalArgumentException이 발생한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        val exception = assertThrows<IllegalArgumentException> {
            dailyBudget.restore(0)
        }

        assertEquals("복구 포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `restore에 음수를 전달하면 IllegalArgumentException이 발생한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        val exception = assertThrows<IllegalArgumentException> {
            dailyBudget.restore(-100)
        }

        assertEquals("복구 포인트는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `restore 시 usedBudget보다 큰 금액을 복구해도 usedBudget은 0 이하가 되지 않는다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 5000

        dailyBudget.restore(10000)

        assertEquals(0, dailyBudget.usedBudget)
    }

    // calculateDistributablePoints - 최소 포인트 미만 테스트
    @Test
    fun `calculateDistributablePoints는 잔여 예산이 최소 요청 금액 미만이면 예외를 던진다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 99950 // 잔여 50p

        val exception = assertThrows<BudgetExhaustedException> {
            dailyBudget.calculateDistributablePoints(1000, 100)
        }

        assertEquals("오늘 예산이 소진되었습니다.", exception.message)
    }

    @Test
    fun `calculateDistributablePoints는 잔여 예산이 정확히 최소 요청 금액이면 해당 금액을 반환한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 99900 // 잔여 100p

        val distributable = dailyBudget.calculateDistributablePoints(1000, 100)

        assertEquals(100, distributable)
    }

    // updateTotalBudget 테스트
    @Test
    fun `updateTotalBudget으로 예산을 변경할 수 있다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)

        dailyBudget.updateTotalBudget(200000)

        assertEquals(200000, dailyBudget.totalBudget)
    }

    @Test
    fun `updateTotalBudget으로 예산을 줄일 수 있다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 30000

        dailyBudget.updateTotalBudget(50000)

        assertEquals(50000, dailyBudget.totalBudget)
        assertEquals(20000, dailyBudget.remainingBudget)
    }

    @Test
    fun `updateTotalBudget에서 새 예산이 사용된 예산보다 작으면 예외가 발생한다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 50000

        val exception = assertThrows<IllegalArgumentException> {
            dailyBudget.updateTotalBudget(30000)
        }

        assertTrue(exception.message!!.contains("이미 사용된 예산"))
    }

    @Test
    fun `updateTotalBudget에서 새 예산이 사용된 예산과 같으면 설정 가능하다`() {
        val dailyBudget = DailyBudget.create(LocalDate.now(), 100000)
        dailyBudget.usedBudget = 50000

        dailyBudget.updateTotalBudget(50000)

        assertEquals(50000, dailyBudget.totalBudget)
        assertEquals(0, dailyBudget.remainingBudget)
        assertTrue(dailyBudget.isExhausted)
    }
}
