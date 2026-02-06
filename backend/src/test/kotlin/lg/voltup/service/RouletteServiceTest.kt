package lg.voltup.service

import lg.voltup.entity.DailyBudget
import lg.voltup.entity.User
import lg.voltup.entity.enums.PointStatus
import lg.voltup.exception.AlreadyParticipatedException
import lg.voltup.exception.BudgetExhaustedException
import lg.voltup.exception.ParticipationAlreadyCancelledException
import lg.voltup.exception.ParticipationNotFoundException
import lg.voltup.exception.PointsAlreadyUsedException
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
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class RouletteServiceTest
    @Autowired
    constructor(
        private val rouletteService: RouletteService,
        private val userRepository: UserRepository,
        private val dailyBudgetRepository: DailyBudgetRepository,
        private val participationRepository: RouletteParticipationRepository,
        private val pointRepository: PointRepository,
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

            val exception =
                assertThrows<AlreadyParticipatedException> {
                    rouletteService.spinRoulette(testUser.id)
                }

            assertEquals("오늘 이미 참여했습니다.", exception.message)
        }

        @Test
        @DisplayName("예산이 소진되면 예외가 발생한다")
        fun spinRoulette_shouldThrowExceptionWhenBudgetExhausted() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 0))

            val exception =
                assertThrows<BudgetExhaustedException> {
                    rouletteService.spinRoulette(testUser.id)
                }

            assertEquals("오늘 예산이 소진되었습니다.", exception.message)
        }

        @Test
        @DisplayName("잔여 예산이 100p 미만이면 예산 소진으로 처리된다")
        fun spinRoulette_shouldThrowExceptionWhenRemainingBudgetLessThan100() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 50))

            val exception =
                assertThrows<BudgetExhaustedException> {
                    rouletteService.spinRoulette(testUser.id)
                }

            assertEquals("오늘 예산이 소진되었습니다.", exception.message)
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
        @DisplayName("룰렛 참여 취소 시 포인트가 CANCELED 상태로 변경된다")
        fun cancelParticipation_shouldCancelPoint() {
            rouletteService.spinRoulette(testUser.id)

            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())
            assertNotNull(participation)

            rouletteService.cancelParticipation(participation.id)

            val points = pointRepository.findAllByUserId(testUser.id)
            assertEquals(1, points.size)
            assertEquals(PointStatus.CANCELED, points[0].status)
        }

        @Test
        @DisplayName("룰렛 참여 취소 시 예산이 복구된다")
        fun cancelParticipation_shouldRestoreBudget() {
            val spinResult = rouletteService.spinRoulette(testUser.id)

            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())
            assertNotNull(participation)

            val budgetBeforeCancel = dailyBudgetRepository.findByDate(LocalDate.now())!!.usedBudget

            rouletteService.cancelParticipation(participation.id)

            val budgetAfterCancel = dailyBudgetRepository.findByDate(LocalDate.now())!!.usedBudget
            assertEquals(budgetBeforeCancel - spinResult.points, budgetAfterCancel)
        }

        @Test
        @DisplayName("룰렛 참여 취소 시 참여 기록이 취소 상태로 변경된다")
        fun cancelParticipation_shouldMarkAsCancel() {
            rouletteService.spinRoulette(testUser.id)

            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())
            assertNotNull(participation)

            rouletteService.cancelParticipation(participation.id)

            val cancelledParticipation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())
            assertNotNull(cancelledParticipation)
            assertTrue(cancelledParticipation.isCancelled)
        }

        @Test
        @DisplayName("존재하지 않는 참여 기록을 취소하면 예외가 발생한다")
        fun cancelParticipation_shouldThrowExceptionWhenNotFound() {
            val exception =
                assertThrows<ParticipationNotFoundException> {
                    rouletteService.cancelParticipation(9999L)
                }

            assertEquals("참여 기록을 찾을 수 없습니다.", exception.message)
        }

        @Test
        @DisplayName("참여 취소 후에도 당일 재참여할 수 없다")
        fun cancelParticipation_shouldNotAllowReParticipation() {
            rouletteService.spinRoulette(testUser.id)

            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())
            assertNotNull(participation)
            rouletteService.cancelParticipation(participation.id)

            val exception =
                assertThrows<AlreadyParticipatedException> {
                    rouletteService.spinRoulette(testUser.id)
                }

            assertEquals("오늘 이미 참여했습니다.", exception.message)
        }

        @Test
        @DisplayName("이미 취소된 참여를 다시 취소하면 예외가 발생한다 (일반 취소)")
        fun cancelParticipation_shouldThrowExceptionWhenAlreadyCancelled() {
            rouletteService.spinRoulette(testUser.id)
            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!

            rouletteService.cancelParticipation(participation.id)

            val exception =
                assertThrows<ParticipationAlreadyCancelledException> {
                    rouletteService.cancelParticipation(participation.id)
                }

            assertEquals("이미 취소된 참여입니다.", exception.message)
        }

        @Test
        @DisplayName("부분 사용된 포인트가 있는 참여를 취소하면 포인트가 CANCELED 상태로 변경된다")
        fun cancelParticipation_shouldCancelPartiallyUsedPoints() {
            rouletteService.spinRoulette(testUser.id)

            // 포인트 일부 사용
            val point = pointRepository.findValidPointsByUserId(testUser.id, LocalDateTime.now()).first()
            point.use(50) // 50p 사용
            pointRepository.save(point)

            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())
            assertNotNull(participation)

            rouletteService.cancelParticipation(participation.id)

            // 부분 사용된 포인트는 CANCELED 상태로 변경됨
            val updatedPoint = pointRepository.findById(point.id).get()
            assertEquals(PointStatus.CANCELED, updatedPoint.status)
            assertEquals(50, updatedPoint.usedAmount) // 사용된 금액은 유지됨
        }

        // === 당첨 내역 조회 및 관리자 취소 테스트 ===

        @Test
        @DisplayName("사용자 당첨 내역을 조회할 수 있다")
        fun getUserHistory_shouldReturnParticipationHistory() {
            rouletteService.spinRoulette(testUser.id)

            val history = rouletteService.getUserHistory(testUser.id)

            assertEquals(1, history.size)
            assertEquals(LocalDate.now(), history[0].date)
            assertEquals(false, history[0].isCancelled)
            assertEquals(true, history[0].isCancellable)
        }

        @Test
        @DisplayName("관리자가 당첨을 취소할 수 있다")
        fun cancelParticipationByAdmin_shouldCancelSuccessfully() {
            rouletteService.spinRoulette(testUser.id)
            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!

            val result = rouletteService.cancelParticipationByAdmin(participation.id)

            assertEquals(participation.id, result.participationId)
            assertEquals(participation.points, result.cancelledPoints)
            assertEquals(true, result.budgetRestored)

            val updatedParticipation = participationRepository.findById(participation.id).get()
            assertEquals(true, updatedParticipation.isCancelled)
        }

        @Test
        @DisplayName("포인트를 사용한 경우 취소할 수 없다")
        fun cancelParticipationByAdmin_shouldFailWhenPointsUsed() {
            rouletteService.spinRoulette(testUser.id)

            val point = pointRepository.findValidPointsByUserId(testUser.id, LocalDateTime.now()).first()
            point.use(50)
            pointRepository.save(point)

            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!

            val exception =
                assertThrows<PointsAlreadyUsedException> {
                    rouletteService.cancelParticipationByAdmin(participation.id)
                }

            assertEquals("이미 사용한 포인트가 있어 취소할 수 없습니다.", exception.message)
        }

        @Test
        @DisplayName("이미 취소된 참여를 다시 취소할 수 없다")
        fun cancelParticipationByAdmin_shouldFailWhenAlreadyCancelled() {
            rouletteService.spinRoulette(testUser.id)
            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!

            rouletteService.cancelParticipationByAdmin(participation.id)

            val exception =
                assertThrows<ParticipationAlreadyCancelledException> {
                    rouletteService.cancelParticipationByAdmin(participation.id)
                }

            assertEquals("이미 취소된 참여입니다.", exception.message)
        }

        @Test
        @DisplayName("관리자 취소 후에도 당일 재참여할 수 없다")
        fun cancelParticipationByAdmin_shouldNotAllowReParticipation() {
            rouletteService.spinRoulette(testUser.id)
            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!

            rouletteService.cancelParticipationByAdmin(participation.id)

            val exception =
                assertThrows<AlreadyParticipatedException> {
                    rouletteService.spinRoulette(testUser.id)
                }

            assertEquals("오늘 이미 참여했습니다.", exception.message)
        }

        @Test
        @DisplayName("당일 취소 시 예산이 복구된다")
        fun cancelParticipationByAdmin_shouldRestoreBudgetForToday() {
            val spinResult = rouletteService.spinRoulette(testUser.id)
            val budgetBefore = dailyBudgetRepository.findByDate(LocalDate.now())!!.remainingBudget

            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!
            val result = rouletteService.cancelParticipationByAdmin(participation.id)

            assertEquals(true, result.budgetRestored)

            val budgetAfter = dailyBudgetRepository.findByDate(LocalDate.now())!!.remainingBudget
            assertEquals(budgetBefore + spinResult.points, budgetAfter)
        }

        @Test
        @DisplayName("취소된 참여는 이력에서 isCancellable이 false로 표시된다")
        fun getUserHistory_shouldShowCancelledAsNotCancellable() {
            rouletteService.spinRoulette(testUser.id)
            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!

            rouletteService.cancelParticipationByAdmin(participation.id)

            val history = rouletteService.getUserHistory(testUser.id)

            assertEquals(1, history.size)
            assertEquals(true, history[0].isCancelled)
            assertEquals(false, history[0].isCancellable)
            assertNotNull(history[0].cancelledAt)
        }

        @Test
        @DisplayName("포인트 사용된 참여는 이력에서 isCancellable이 false로 표시된다")
        fun getUserHistory_shouldShowUsedPointsAsNotCancellable() {
            rouletteService.spinRoulette(testUser.id)

            val point = pointRepository.findValidPointsByUserId(testUser.id, LocalDateTime.now()).first()
            point.use(50)
            pointRepository.save(point)

            val history = rouletteService.getUserHistory(testUser.id)

            assertEquals(1, history.size)
            assertEquals(false, history[0].isCancelled)
            assertEquals(false, history[0].isCancellable)
        }

        // === 관리자용 참여 내역 조회 테스트 ===

        @Test
        @DisplayName("관리자 참여 내역 조회 시 상태가 PARTICIPATED로 표시된다")
        fun getTodayParticipations_shouldShowParticipatedStatus() {
            rouletteService.spinRoulette(testUser.id)

            val participations = rouletteService.getTodayParticipations()

            assertEquals(1, participations.size)
            assertEquals("PARTICIPATED", participations[0].status)
            assertEquals(null, participations[0].cancelledAt)
        }

        @Test
        @DisplayName("관리자 참여 내역 조회 시 취소된 참여는 CANCELLED로 표시된다")
        fun getTodayParticipations_shouldShowCancelledStatus() {
            rouletteService.spinRoulette(testUser.id)
            val participation = participationRepository.findByUserIdAndDate(testUser.id, LocalDate.now())!!

            rouletteService.cancelParticipationByAdmin(participation.id)

            val participations = rouletteService.getTodayParticipations()

            assertEquals(1, participations.size)
            assertEquals("CANCELLED", participations[0].status)
            assertNotNull(participations[0].cancelledAt)
        }
    }
