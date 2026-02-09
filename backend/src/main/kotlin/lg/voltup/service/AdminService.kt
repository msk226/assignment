package lg.voltup.service

import lg.voltup.controller.dto.BudgetResponse
import lg.voltup.controller.dto.BudgetUpdateRequest
import lg.voltup.controller.dto.DashboardResponse
import lg.voltup.entity.DailyBudget
import lg.voltup.repository.DailyBudgetRepository
import lg.voltup.repository.RouletteParticipationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class AdminService(
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val participationRepository: RouletteParticipationRepository,
) {
    fun getTodayBudget(): BudgetResponse {
        val budget = getOrCreateBudget(LocalDate.now())
        return budget.toResponse()
    }

    fun getBudget(date: LocalDate): BudgetResponse {
        val budget = getOrCreateBudget(date)
        return budget.toResponse()
    }

    fun updateTodayBudget(request: BudgetUpdateRequest): BudgetResponse {
        return updateBudget(LocalDate.now(), request)
    }

    fun updateBudget(date: LocalDate, request: BudgetUpdateRequest): BudgetResponse {
        val budget =
            dailyBudgetRepository.findByDateWithLock(date)
                ?: dailyBudgetRepository.save(DailyBudget.create(date, request.totalBudget))

        budget.updateTotalBudget(request.totalBudget)
        return budget.toResponse()
    }

    fun getDashboard(): DashboardResponse {
        val today = LocalDate.now()
        val budget = getOrCreateBudget(today)
        val participantCount = participationRepository.countActiveByDate(today)
        val totalPointsDistributed = participationRepository.sumPointsByDate(today)

        return DashboardResponse(
            date = today,
            totalBudget = budget.totalBudget,
            usedBudget = budget.usedBudget,
            remainingBudget = budget.remainingBudget,
            participantCount = participantCount,
            totalPointsDistributed = totalPointsDistributed,
        )
    }

    private fun getOrCreateBudget(date: LocalDate): DailyBudget {
        return dailyBudgetRepository.findByDate(date)
            ?: dailyBudgetRepository.save(DailyBudget.create(date))
    }

    private fun DailyBudget.toResponse() =
        BudgetResponse(
            date = date,
            totalBudget = totalBudget,
            usedBudget = usedBudget,
            remainingBudget = remainingBudget,
        )
}
