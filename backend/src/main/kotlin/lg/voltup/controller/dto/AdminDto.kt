package lg.voltup.controller.dto

import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class BudgetResponse(
    val date: LocalDate,
    val totalBudget: Int,
    val usedBudget: Int,
    val remainingBudget: Int
)

data class BudgetUpdateRequest(
    @field:Positive(message = "예산은 양수여야 합니다.")
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
