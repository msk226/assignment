package lg.voltup.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "daily_budgets")
class DailyBudget private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val date: LocalDate,

    val totalBudget: Int = 100000,

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
}
