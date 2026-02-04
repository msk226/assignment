package lg.voltup.service

import lg.voltup.controller.dto.RouletteParticipationResponse
import lg.voltup.controller.dto.RouletteSpinResponse
import lg.voltup.controller.dto.RouletteStatusResponse
import lg.voltup.entity.DailyBudget
import lg.voltup.entity.Point
import lg.voltup.entity.RouletteParticipation
import lg.voltup.exception.AlreadyParticipatedException
import lg.voltup.exception.ParticipationNotFoundException
import lg.voltup.repository.DailyBudgetRepository
import lg.voltup.repository.PointRepository
import lg.voltup.repository.RouletteParticipationRepository
import lg.voltup.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class RouletteService(
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val participationRepository: RouletteParticipationRepository,
    private val pointRepository: PointRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun spinRoulette(userId: Long): RouletteSpinResponse {
        val today = LocalDate.now()

        validateNotAlreadyParticipated(userId, today)

        val budget = getOrCreateBudgetWithLock(today)
        val maxPoints = budget.calculateDistributablePoints()
        val points = calculateRandomPoints(maxPoints)

        budget.distribute(points)
        saveParticipation(userId, today, points)
        savePoint(userId, points)

        return RouletteSpinResponse(
            points = points,
            remainingBudget = budget.remainingBudget,
            message = "${points}p를 획득했습니다!"
        )
    }

    fun getStatus(userId: Long): RouletteStatusResponse {
        val today = LocalDate.now()
        val budget = dailyBudgetRepository.findByDate(today)
            ?: DailyBudget.create(today)
        val participation = participationRepository.findByUserIdAndDate(userId, today)

        return RouletteStatusResponse(
            hasParticipatedToday = participation != null,
            todayPoints = participation?.points,
            remainingBudget = budget.remainingBudget,
            totalBudget = budget.totalBudget
        )
    }

    fun getTodayParticipations(): List<RouletteParticipationResponse> {
        val today = LocalDate.now()
        return participationRepository.findAllByDate(today).map { participation ->
            val user = userRepository.findById(participation.userId).orElse(null)
            RouletteParticipationResponse(
                id = participation.id,
                userId = participation.userId,
                nickname = user?.nickname,
                points = participation.points,
                createdAt = participation.createdAt
            )
        }
    }

    @Transactional
    fun cancelParticipation(participationId: Long) {
        val participation = participationRepository.findByIdWithLock(participationId)
            ?: throw ParticipationNotFoundException("참여 기록을 찾을 수 없습니다.")

        restoreBudgetIfToday(participation)
        removePointIfExists(participation)
        participationRepository.delete(participation)
    }

    private fun validateNotAlreadyParticipated(userId: Long, date: LocalDate) {
        if (participationRepository.existsByUserIdAndDate(userId, date)) {
            throw AlreadyParticipatedException("오늘 이미 참여했습니다.")
        }
    }

    private fun getOrCreateBudgetWithLock(date: LocalDate): DailyBudget {
        return dailyBudgetRepository.findByDateWithLock(date)
            ?: dailyBudgetRepository.save(DailyBudget.create(date))
    }

    private fun calculateRandomPoints(maxPoints: Int, minPoints: Int = 100): Int {
        return if (maxPoints < minPoints) maxPoints else Random.nextInt(minPoints, maxPoints + 1)
    }

    private fun saveParticipation(userId: Long, date: LocalDate, points: Int) {
        participationRepository.save(
            RouletteParticipation.create(userId = userId, date = date, points = points)
        )
    }

    private fun savePoint(userId: Long, points: Int) {
        pointRepository.save(Point.createWithDefaultExpiry(userId, points))
    }

    private fun restoreBudgetIfToday(participation: RouletteParticipation) {
        if (participation.date == LocalDate.now()) {
            dailyBudgetRepository.findByDateWithLock(participation.date)
                ?.restore(participation.points)
        }
    }

    private fun removePointIfExists(participation: RouletteParticipation) {
        val userPoints =
            pointRepository.findValidPointsByUserIdWithLock(participation.userId, LocalDateTime.now())

        // 참여 기록과 동일한 금액의 포인트를 찾아 회수
        // 사용되지 않은 포인트는 삭제, 부분 사용된 포인트는 남은 금액만큼 usedAmount 증가
        val matchingPoint = userPoints.find {
            it.amount == participation.points &&
            it.earnedAt.toLocalDate() == participation.date
        }

        matchingPoint?.let { point ->
            if (point.usedAmount == 0) {
                pointRepository.delete(point)
            } else {
                // 부분 사용된 경우, 남은 금액을 모두 사용 처리
                point.use(point.availableAmount)
            }
        }
    }
}
