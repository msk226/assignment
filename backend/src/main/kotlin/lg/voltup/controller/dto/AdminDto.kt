package lg.voltup.controller.dto

import java.time.LocalDate

data class BudgetResponse(
    val date: LocalDate,
    val totalBudget: Int,
    val usedBudget: Int,
    val remainingBudget: Int
)

data class BudgetUpdateRequest(
    val totalBudget: Int
)

data class DashboardResponse(
    val date: LocalDate,
    val totalBudget: Int,
    val usedBudget: Int,
    val remainingBudget: Int,
    val participantCount: Long,
    val totalPointsDistributed: Int
)
