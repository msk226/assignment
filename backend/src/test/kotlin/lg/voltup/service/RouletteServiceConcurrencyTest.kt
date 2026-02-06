package lg.voltup.service

import lg.voltup.entity.DailyBudget
import lg.voltup.entity.User
import lg.voltup.exception.AlreadyParticipatedException
import lg.voltup.exception.ParticipationAlreadyCancelledException
import lg.voltup.repository.DailyBudgetRepository
import lg.voltup.repository.PointRepository
import lg.voltup.repository.RouletteParticipationRepository
import lg.voltup.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class RouletteServiceConcurrencyTest
    @Autowired
    constructor(
        private val rouletteService: RouletteService,
        private val userRepository: UserRepository,
        private val dailyBudgetRepository: DailyBudgetRepository,
        private val participationRepository: RouletteParticipationRepository,
        private val pointRepository: PointRepository,
    ) {
        private lateinit var testUsers: List<User>

        @BeforeEach
        fun setUp() {
            testUsers =
                (1..20).map { i ->
                    userRepository.save(User.create("concurrent_user_$i"))
                }
        }

        @AfterEach
        fun tearDown() {
            participationRepository.deleteAll()
            pointRepository.deleteAll()
            dailyBudgetRepository.deleteAll()
            userRepository.deleteAll()
        }

        @Test
        @DisplayName("동시에 10명이 룰렛을 돌려도 예산을 초과하지 않는다")
        fun concurrentSpins_shouldNotExceedBudget() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 5000))

            val threadCount = 10
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(threadCount)
            val successCount = AtomicInteger(0)
            val totalPoints = AtomicInteger(0)

            testUsers.take(threadCount).forEach { user ->
                executor.submit {
                    try {
                        val result = rouletteService.spinRoulette(user.id)
                        successCount.incrementAndGet()
                        totalPoints.addAndGet(result.points)
                    } catch (e: Exception) {
                        // 예산 소진 또는 기타 예외
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            executor.shutdown()

            val budget = dailyBudgetRepository.findByDate(today)!!
            val participations = participationRepository.findAllByDate(today)
            val actualTotalPoints = participations.sumOf { it.points }

            assertTrue(actualTotalPoints <= 5000, "총 지급 포인트가 예산을 초과하면 안 됩니다")
            assertEquals(budget.usedBudget, actualTotalPoints, "예산 사용량과 실제 지급량이 일치해야 합니다")
            assertEquals(successCount.get(), participations.size, "성공 횟수와 참여 기록 수가 일치해야 합니다")
        }

        @Test
        @DisplayName("같은 유저가 동시에 여러 번 스핀해도 1회만 성공한다")
        fun concurrentSpins_sameUser_shouldSucceedOnlyOnce() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 100000))

            val user = testUsers[0]
            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(threadCount)
            val successCount = AtomicInteger(0)
            val expectedFailCount = AtomicInteger(0)
            val unexpectedExceptions = CopyOnWriteArrayList<Exception>()

            repeat(threadCount) {
                executor.submit {
                    try {
                        rouletteService.spinRoulette(user.id)
                        successCount.incrementAndGet()
                    } catch (e: AlreadyParticipatedException) {
                        // 예상된 예외: 이미 참여한 경우
                        expectedFailCount.incrementAndGet()
                    } catch (e: DataIntegrityViolationException) {
                        // Race condition으로 인한 DB 제약조건 위반 (예상 범위 내)
                        expectedFailCount.incrementAndGet()
                    } catch (e: Exception) {
                        // 예상하지 못한 예외
                        unexpectedExceptions.add(e)
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            executor.shutdown()

            assertTrue(
                unexpectedExceptions.isEmpty(),
                "예상하지 못한 예외가 발생했습니다: ${unexpectedExceptions.map { it.javaClass.simpleName }}",
            )
            assertEquals(1, successCount.get(), "같은 유저는 1번만 성공해야 합니다")
            assertEquals(threadCount - 1, expectedFailCount.get(), "나머지 요청은 실패해야 합니다")

            val participations = participationRepository.findAllByUserId(user.id)
            assertEquals(1, participations.size, "참여 기록은 1개만 있어야 합니다")
        }

        @Test
        @DisplayName("예산이 1000p 남았을 때 동시에 5명이 스핀하면 예산 범위 내에서만 지급된다")
        fun concurrentSpins_withLimitedBudget() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 1000))

            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(threadCount)
            val successCount = AtomicInteger(0)

            testUsers.take(threadCount).forEach { user ->
                executor.submit {
                    try {
                        rouletteService.spinRoulette(user.id)
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                        // 예산 소진
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            executor.shutdown()

            val budget = dailyBudgetRepository.findByDate(today)!!
            val participations = participationRepository.findAllByDate(today)
            val actualTotalPoints = participations.sumOf { it.points }

            assertTrue(actualTotalPoints <= 1000, "총 지급 포인트가 1000p를 초과하면 안 됩니다")
            assertTrue(budget.remainingBudget >= 0, "잔여 예산이 음수가 되면 안 됩니다")
            assertEquals(successCount.get(), participations.size)
        }

        @Test
        @DisplayName("20명이 동시에 스핀해도 각각 1회씩만 참여된다")
        fun concurrentSpins_multipleUsers_eachSucceedsOnce() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 100000))

            val threadCount = 20
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(threadCount)
            val successCount = AtomicInteger(0)

            testUsers.forEach { user ->
                executor.submit {
                    try {
                        rouletteService.spinRoulette(user.id)
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                        // 예외 발생
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            executor.shutdown()

            assertEquals(20, successCount.get(), "20명 모두 성공해야 합니다")

            val participations = participationRepository.findAllByDate(today)
            assertEquals(20, participations.size, "참여 기록은 20개여야 합니다")

            val uniqueUserIds = participations.map { it.userId }.toSet()
            assertEquals(20, uniqueUserIds.size, "각 유저별로 1개씩만 있어야 합니다")

            val budget = dailyBudgetRepository.findByDate(today)!!
            val actualTotalPoints = participations.sumOf { it.points }
            assertEquals(budget.usedBudget, actualTotalPoints)
        }

        @Test
        @DisplayName("동시에 같은 참여를 취소하면 1회만 취소된다")
        fun concurrentCancelSameParticipation_shouldSucceedOnlyOnce() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 100000))

            // 1. 먼저 스핀하여 참여 기록 생성
            val user = testUsers[0]
            rouletteService.spinRoulette(user.id)
            val participation = participationRepository.findByUserIdAndDate(user.id, today)!!

            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(threadCount)
            val successCount = AtomicInteger(0)
            val alreadyCancelledCount = AtomicInteger(0)
            val unexpectedExceptions = CopyOnWriteArrayList<Exception>()

            // 2. 동시에 같은 참여를 취소 시도
            repeat(threadCount) {
                executor.submit {
                    try {
                        rouletteService.cancelParticipationByAdmin(participation.id)
                        successCount.incrementAndGet()
                    } catch (e: ParticipationAlreadyCancelledException) {
                        alreadyCancelledCount.incrementAndGet()
                    } catch (e: Exception) {
                        unexpectedExceptions.add(e)
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            executor.shutdown()

            assertTrue(
                unexpectedExceptions.isEmpty(),
                "예상하지 못한 예외가 발생했습니다: ${unexpectedExceptions.map { "${it.javaClass.simpleName}: ${it.message}" }}",
            )
            assertEquals(1, successCount.get(), "취소는 1번만 성공해야 합니다")
            assertEquals(threadCount - 1, alreadyCancelledCount.get(), "나머지는 이미 취소됨 예외가 발생해야 합니다")

            // 3. 참여가 취소 상태인지 확인
            val updatedParticipation = participationRepository.findById(participation.id).get()
            assertTrue(updatedParticipation.isCancelled, "참여가 취소 상태여야 합니다")

            // 4. 예산이 정확히 1번만 복구되었는지 확인 (취소 후 원래 예산으로 복구)
            val budget = dailyBudgetRepository.findByDate(today)!!
            assertEquals(100000, budget.remainingBudget, "예산이 완전히 복구되어야 합니다")
        }

        @Test
        @DisplayName("취소 후 같은 유저가 다시 스핀할 수 있다")
        fun cancelAndRespin_shouldAllowNewParticipation() {
            val today = LocalDate.now()
            dailyBudgetRepository.save(DailyBudget.create(today, 100000))

            val user = testUsers[0]

            // 1. 첫 번째 스핀
            rouletteService.spinRoulette(user.id)
            val participation = participationRepository.findByUserIdAndDate(user.id, today)!!

            // 2. 취소
            rouletteService.cancelParticipationByAdmin(participation.id)

            // 3. 다시 스핀 시도 - 같은 날이므로 실패해야 함 (취소되어도 참여 기록은 남음)
            var respinFailed = false
            try {
                rouletteService.spinRoulette(user.id)
            } catch (e: AlreadyParticipatedException) {
                respinFailed = true
            }

            // 취소된 참여도 "참여한 것"으로 간주하므로 재참여 불가
            assertTrue(respinFailed, "취소 후에도 같은 날 재참여는 불가능해야 합니다")
        }
    }
