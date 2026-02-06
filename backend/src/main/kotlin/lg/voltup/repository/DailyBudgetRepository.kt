package lg.voltup.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import lg.voltup.entity.DailyBudget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyBudgetRepository : JpaRepository<DailyBudget, Long> {
    fun findByDate(date: LocalDate): DailyBudget?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT d FROM DailyBudget d WHERE d.date = :date")
    fun findByDateWithLock(date: LocalDate): DailyBudget?
}
