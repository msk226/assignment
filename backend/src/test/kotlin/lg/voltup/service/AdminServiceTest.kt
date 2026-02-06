package lg.voltup.service

import lg.voltup.controller.dto.BudgetUpdateRequest
import lg.voltup.entity.DailyBudget
import lg.voltup.repository.DailyBudgetRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class AdminServiceTest
    @Autowired
    constructor(
        private val adminService: AdminService,
        private val dailyBudgetRepository: DailyBudgetRepository,
    ) {
        @BeforeEach
        fun setUp() {
            dailyBudgetRepository.deleteAll()
        }

        // getTodayBudget 테스트
        @Test
        @DisplayName("오늘 예산이 없으면 기본 예산을 생성하고 반환한다")
        fun getTodayBudget_shouldCreateDefaultBudgetWhenNotExists() {
            val result = adminService.getTodayBudget()

            assertEquals(LocalDate.now(), result.date)
            assertEquals(100000, result.totalBudget)
            assertEquals(0, result.usedBudget)
            assertEquals(100000, result.remainingBudget)

            // DB에 저장되었는지 확인
            val saved = dailyBudgetRepository.findByDate(LocalDate.now())
            assertTrue(saved != null)
        }

        @Test
        @DisplayName("오늘 예산이 있으면 해당 예산을 반환한다")
        fun getTodayBudget_shouldReturnExistingBudget() {
            dailyBudgetRepository.save(DailyBudget.create(LocalDate.now(), 200000))

            val result = adminService.getTodayBudget()

            assertEquals(200000, result.totalBudget)
        }

        // updateTodayBudget 테스트
        @Test
        @DisplayName("오늘 예산이 없으면 새로 생성하고 설정한다")
        fun updateTodayBudget_shouldCreateNewBudgetWhenNotExists() {
            val request = BudgetUpdateRequest(totalBudget = 150000)

            val result = adminService.updateTodayBudget(request)

            assertEquals(150000, result.totalBudget)
            assertEquals(0, result.usedBudget)
        }

        @Test
        @DisplayName("오늘 예산이 있으면 업데이트한다")
        fun updateTodayBudget_shouldUpdateExistingBudget() {
            dailyBudgetRepository.save(DailyBudget.create(LocalDate.now(), 100000))
            val request = BudgetUpdateRequest(totalBudget = 200000)

            val result = adminService.updateTodayBudget(request)

            assertEquals(200000, result.totalBudget)
        }

        @Test
        @DisplayName("예산을 줄일 때 이미 사용된 예산보다 작으면 예외가 발생한다")
        fun updateTodayBudget_shouldThrowExceptionWhenNewBudgetLessThanUsed() {
            val budget = dailyBudgetRepository.save(DailyBudget.create(LocalDate.now(), 100000))
            budget.usedBudget = 50000
            dailyBudgetRepository.save(budget)

            val request = BudgetUpdateRequest(totalBudget = 30000)

            assertThrows<IllegalArgumentException> {
                adminService.updateTodayBudget(request)
            }
        }

        @Test
        @DisplayName("예산을 사용된 예산과 같게 설정할 수 있다")
        fun updateTodayBudget_shouldAllowSettingBudgetEqualToUsed() {
            val budget = dailyBudgetRepository.save(DailyBudget.create(LocalDate.now(), 100000))
            budget.usedBudget = 50000
            dailyBudgetRepository.save(budget)

            val request = BudgetUpdateRequest(totalBudget = 50000)

            val result = adminService.updateTodayBudget(request)

            assertEquals(50000, result.totalBudget)
            assertEquals(0, result.remainingBudget)
        }

        // getDashboard 테스트
        @Test
        @DisplayName("대시보드에서 오늘 예산 현황을 조회할 수 있다")
        fun getDashboard_shouldReturnTodayBudgetStatus() {
            val budget = dailyBudgetRepository.save(DailyBudget.create(LocalDate.now(), 100000))
            budget.usedBudget = 30000
            dailyBudgetRepository.save(budget)

            val result = adminService.getDashboard()

            assertEquals(LocalDate.now(), result.date)
            assertEquals(100000, result.totalBudget)
            assertEquals(30000, result.usedBudget)
            assertEquals(70000, result.remainingBudget)
        }
    }
