package lg.voltup.service

import lg.voltup.controller.dto.CancelParticipationResponse
import lg.voltup.controller.dto.RouletteHistoryResponse
import lg.voltup.controller.dto.RouletteParticipationResponse
import lg.voltup.controller.dto.RouletteSpinResponse
import lg.voltup.controller.dto.RouletteStatusResponse
import lg.voltup.entity.DailyBudget
import lg.voltup.entity.Point
import lg.voltup.entity.RouletteParticipation
import lg.voltup.exception.AlreadyParticipatedException
import lg.voltup.exception.ParticipationAlreadyCancelledException
import lg.voltup.exception.ParticipationNotFoundException
import lg.voltup.exception.PointsAlreadyUsedException
import lg.voltup.repository.DailyBudgetRepository
import lg.voltup.repository.PointRepository
import lg.voltup.repository.RouletteParticipationRepository
import lg.voltup.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.random.Random

@Service
class RouletteService(
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val participationRepository: RouletteParticipationRepository,
    private val pointRepository: PointRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun spinRoulette(userId: Long): RouletteSpinResponse {
        val today = LocalDate.now()

        // 예산 락을 먼저 획득하여 동시성 제어의 시작점으로 사용
        val budget = getOrCreateBudgetWithLock(today)

        // 락 획득 후 참여 여부 체크 (Double-Spin Race Condition 방지)
        validateNotAlreadyParticipated(userId, today)
        val maxPoints = budget.calculateDistributablePoints()
        val points = calculateRandomPoints(maxPoints)

        budget.distribute(points)
        val participation = saveParticipation(userId, today, points)
        savePoint(userId, points, participation.id)

        return RouletteSpinResponse(
            points = points,
            remainingBudget = budget.remainingBudget,
            message = "${points}p를 획득했습니다!",
        )
    }

    @Transactional(readOnly = true)
    fun getStatus(userId: Long): RouletteStatusResponse {
        val today = LocalDate.now()
        val budget =
            dailyBudgetRepository.findByDate(today)
                ?: DailyBudget.create(today)
        val participation = participationRepository.findByUserIdAndDate(userId, today)

        return RouletteStatusResponse(
            hasParticipatedToday = participation != null,
            todayPoints = participation?.points,
            remainingBudget = budget.remainingBudget,
            totalBudget = budget.totalBudget,
        )
    }

    @Transactional(readOnly = true)
    fun getTodayParticipations(): List<RouletteParticipationResponse> {
        val today = LocalDate.now()
        return participationRepository.findAllByDate(today).map { participation ->
            val user = userRepository.findById(participation.userId).orElse(null)
            RouletteParticipationResponse(
                id = participation.id,
                userId = participation.userId,
                nickname = user?.nickname,
                points = participation.points,
                status = participation.status.name,
                createdAt = participation.createdAt,
                cancelledAt = participation.cancelledAt,
            )
        }
    }

    @Transactional
    fun cancelParticipation(participationId: Long) {
        val participation =
            participationRepository.findByIdWithLock(participationId)
                ?: throw ParticipationNotFoundException("참여 기록을 찾을 수 없습니다.")

        if (participation.isCancelled) {
            throw ParticipationAlreadyCancelledException("이미 취소된 참여입니다.")
        }

        participation.cancel()
        restoreBudgetIfToday(participation)
        removePointIfExists(participation)
    }

    @Transactional(readOnly = true)
    fun getUserHistory(userId: Long): List<RouletteHistoryResponse> {
        return participationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { participation ->
            val point = findMatchingPoint(participation)
            val hasUsedPoints = point != null && point.usedAmount > 0

            RouletteHistoryResponse(
                id = participation.id,
                points = participation.points,
                date = participation.date,
                isCancelled = participation.isCancelled,
                isCancellable = !participation.isCancelled && !hasUsedPoints,
                createdAt = participation.createdAt,
                cancelledAt = participation.cancelledAt,
            )
        }
    }

    private fun findMatchingPoint(participation: RouletteParticipation): lg.voltup.entity.Point? {
        return pointRepository.findByParticipationId(participation.id)
    }

    private fun restoreBudgetIfTodayAndReturn(participation: RouletteParticipation): Boolean {
        if (participation.date == LocalDate.now()) {
            dailyBudgetRepository.findByDateWithLock(participation.date)?.restore(participation.points)
            return true
        }
        return false
    }

    @Transactional
    fun cancelParticipationByAdmin(participationId: Long): CancelParticipationResponse {
        val participation = findParticipationForCancellation(participationId)
        val point = findAndValidatePointForCancellation(participation)

        participation.cancel()
        val budgetRestored = restoreBudgetIfTodayAndReturn(participation)
        point?.cancel()

        return buildCancelResponse(participationId, participation.points, budgetRestored)
    }

    private fun findParticipationForCancellation(participationId: Long): RouletteParticipation {
        val participation =
            participationRepository.findByIdWithLock(participationId)
                ?: throw ParticipationNotFoundException("참여 기록을 찾을 수 없습니다.")

        if (participation.isCancelled) {
            throw ParticipationAlreadyCancelledException("이미 취소된 참여입니다.")
        }
        return participation
    }

    private fun findAndValidatePointForCancellation(participation: RouletteParticipation): Point? {
        val point = findMatchingPoint(participation)
        if (point != null && point.usedAmount > 0) {
            throw PointsAlreadyUsedException("이미 사용한 포인트가 있어 취소할 수 없습니다.")
        }
        return point
    }

    private fun buildCancelResponse(
        participationId: Long,
        points: Int,
        budgetRestored: Boolean,
    ): CancelParticipationResponse {
        val message =
            if (budgetRestored) {
                "${points}p 당첨이 취소되었습니다. 예산이 복구되었습니다."
            } else {
                "${points}p 당첨이 취소되었습니다."
            }
        return CancelParticipationResponse(
            participationId = participationId,
            cancelledPoints = points,
            budgetRestored = budgetRestored,
            message = message,
        )
    }

    private fun validateNotAlreadyParticipated(
        userId: Long,
        date: LocalDate,
    ) {
        if (participationRepository.existsByUserIdAndDate(userId, date)) {
            throw AlreadyParticipatedException("오늘 이미 참여했습니다.")
        }
    }

    private fun getOrCreateBudgetWithLock(date: LocalDate): DailyBudget {
        return dailyBudgetRepository.findByDateWithLock(date)
            ?: dailyBudgetRepository.save(DailyBudget.create(date))
    }

    private fun calculateRandomPoints(
        maxPoints: Int,
        minPoints: Int = 100,
    ): Int {
        return if (maxPoints < minPoints) maxPoints else Random.nextInt(minPoints, maxPoints + 1)
    }

    private fun saveParticipation(
        userId: Long,
        date: LocalDate,
        points: Int,
    ): RouletteParticipation {
        return participationRepository.save(
            RouletteParticipation.create(userId = userId, date = date, points = points),
        )
    }

    private fun savePoint(
        userId: Long,
        points: Int,
        participationId: Long,
    ) {
        pointRepository.save(Point.createWithDefaultExpiry(userId, points, participationId))
    }

    private fun restoreBudgetIfToday(participation: RouletteParticipation) {
        if (participation.date == LocalDate.now()) {
            dailyBudgetRepository.findByDateWithLock(participation.date)
                ?.restore(participation.points)
        }
    }

    private fun removePointIfExists(participation: RouletteParticipation) {
        findMatchingPoint(participation)?.cancel()
    }
}
