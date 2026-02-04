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
class AdminService(
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val participationRepository: RouletteParticipationRepository
) {

    fun getTodayBudget(): BudgetResponse {
        val budget = getOrCreateTodayBudget()
        return budget.toResponse()
    }

    @Transactional
    fun updateTodayBudget(request: BudgetUpdateRequest): BudgetResponse {
        val today = LocalDate.now()
        val budget = dailyBudgetRepository.findByDateWithLock(today)
            ?: dailyBudgetRepository.save(DailyBudget.create(today, request.totalBudget))

        return budget.toResponse()
    }

    fun getDashboard(): DashboardResponse {
        val today = LocalDate.now()
        val budget = getOrCreateTodayBudget()
        val participantCount = participationRepository.countByDate(today)
        val totalPointsDistributed =
            participationRepository.findAllByDate(today).sumOf { it.points }

        return DashboardResponse(
            date = today,
            totalBudget = budget.totalBudget,
            usedBudget = budget.usedBudget,
            remainingBudget = budget.remainingBudget,
            participantCount = participantCount,
            totalPointsDistributed = totalPointsDistributed
        )
    }

    private fun getOrCreateTodayBudget(): DailyBudget {
        return dailyBudgetRepository.findByDate(LocalDate.now())
            ?: DailyBudget.create(LocalDate.now())
    }

    private fun DailyBudget.toResponse() = BudgetResponse(
        date = date,
        totalBudget = totalBudget,
        usedBudget = usedBudget,
        remainingBudget = remainingBudget
    )
}
