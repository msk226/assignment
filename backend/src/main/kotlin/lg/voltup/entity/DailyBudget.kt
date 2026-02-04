package lg.voltup.entity

import jakarta.persistence.*
import lg.voltup.exception.BudgetExhaustedException
import java.time.LocalDate

@Entity
@Table(name = "daily_budgets")
class DailyBudget private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val date: LocalDate,

    @Column(nullable = false)
    var totalBudget: Int = 100000,

    var usedBudget: Int = 0
) {
    companion object {
        fun create(date: LocalDate, totalBudget: Int = 100000): DailyBudget {
            return DailyBudget(
                date = date,
                totalBudget = totalBudget
            )
        }
    }

    val remainingBudget: Int
        get() = totalBudget - usedBudget

    val isExhausted: Boolean
        get() = remainingBudget <= 0

    fun calculateDistributablePoints(requestedMax: Int = 1000, requestedMin: Int = 100): Int {
        if (isExhausted || remainingBudget < requestedMin) {
            throw BudgetExhaustedException("오늘 예산이 소진되었습니다.")
        }
        return minOf(requestedMax, remainingBudget)
    }

    fun updateTotalBudget(newBudget: Int) {
        require(newBudget >= usedBudget) { "새 예산은 이미 사용된 예산(${usedBudget}p)보다 작을 수 없습니다." }
        totalBudget = newBudget
    }

    fun distribute(points: Int) {
        require(points > 0) { "지급 포인트는 0보다 커야 합니다." }
        if (points > remainingBudget) {
            throw BudgetExhaustedException("잔여 예산이 부족합니다.")
        }
        usedBudget += points
    }

    fun restore(points: Int) {
        require(points > 0) { "복구 포인트는 0보다 커야 합니다." }
        usedBudget = maxOf(0, usedBudget - points)
    }
}
