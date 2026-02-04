package lg.voltup.repository

import lg.voltup.entity.DailyBudget
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyBudgetRepository : JpaRepository<DailyBudget, Long> {
    fun findByDate(date: LocalDate): DailyBudget?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyBudget d WHERE d.date = :date")
    fun findByDateWithLock(date: LocalDate): DailyBudget?
}
