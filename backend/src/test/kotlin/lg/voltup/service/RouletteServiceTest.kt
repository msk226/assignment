package lg.voltup.service

import lg.voltup.entity.DailyBudget
import lg.voltup.entity.User
import lg.voltup.exception.AlreadyParticipatedException
import lg.voltup.exception.BudgetExhaustedException
import lg.voltup.repository.DailyBudgetRepository
import lg.voltup.repository.PointRepository
import lg.voltup.repository.RouletteParticipationRepository
import lg.voltup.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class RouletteServiceTest @Autowired constructor(
    private val rouletteService: RouletteService,
    private val userRepository: UserRepository,
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val participationRepository: RouletteParticipationRepository,
    private val pointRepository: PointRepository
) {

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(User.create("testuser"))
    }

    @Test
    @DisplayName("룰렛 참여 시 100~1000p 사이의 포인트를 획득한다")
    fun spinRoulette_shouldReturnPointsBetween100And1000() {
        val result = rouletteService.spinRoulette(testUser.id)

        assertTrue(result.points in 100..1000)
        assertEquals("${result.points}p를 획득했습니다!", result.message)
    }

    @Test
    @DisplayName("룰렛 참여 후 포인트가 지급된다")
    fun spinRoulette_shouldCreatePoint() {
        val result = rouletteService.spinRoulette(testUser.id)

        val points = pointRepository.findAllByUserId(testUser.id)
        assertEquals(1, points.size)
        assertEquals(result.points, points[0].amount)
    }

    @Test
    @DisplayName("같은 날 두 번 참여하면 예외가 발생한다")
    fun spinRoulette_shouldThrowExceptionWhenAlreadyParticipated() {
        rouletteService.spinRoulette(testUser.id)

        val exception = assertThrows<AlreadyParticipatedException> {
            rouletteService.spinRoulette(testUser.id)
        }

        assertEquals("오늘 이미 참여했습니다.", exception.message)
    }

    @Test
    @DisplayName("예산이 소진되면 예외가 발생한다")
    fun spinRoulette_shouldThrowExceptionWhenBudgetExhausted() {
        val today = LocalDate.now()
        dailyBudgetRepository.save(DailyBudget.create(today, 0))

        val exception = assertThrows<BudgetExhaustedException> {
            rouletteService.spinRoulette(testUser.id)
        }

        assertEquals("오늘 예산이 소진되었습니다.", exception.message)
    }

    @Test
    @DisplayName("잔여 예산이 100p 미만이면 잔여 예산만큼 지급된다")
    fun spinRoulette_shouldReturnRemainingBudgetWhenLessThan100() {
        val today = LocalDate.now()
        dailyBudgetRepository.save(DailyBudget.create(today, 50))

        val result = rouletteService.spinRoulette(testUser.id)

        assertEquals(50, result.points)
    }

    @Test
    @DisplayName("룰렛 상태 조회 - 참여 전")
    fun getStatus_beforeParticipation() {
        val status = rouletteService.getStatus(testUser.id)

        assertEquals(false, status.hasParticipatedToday)
        assertEquals(null, status.todayPoints)
        assertEquals(100000, status.totalBudget)
    }

    @Test
    @DisplayName("룰렛 상태 조회 - 참여 후")
    fun getStatus_afterParticipation() {
        val spinResult = rouletteService.spinRoulette(testUser.id)
        val status = rouletteService.getStatus(testUser.id)

        assertEquals(true, status.hasParticipatedToday)
        assertEquals(spinResult.points, status.todayPoints)
    }

    @Test
    @DisplayName("룰렛 참여 취소 시 포인트가 회수된다")
    fun cancelParticipation_shouldRemovePoint() {
        rouletteService.spinRoulette(testUser.id)

        val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())
        assertNotNull(participation)

        rouletteService.cancelParticipation(participation.id)

        val points = pointRepository.findAllByUserId(testUser.id)
        assertTrue(points.isEmpty() || points.all { it.usedAmount == it.amount })
    }
}
