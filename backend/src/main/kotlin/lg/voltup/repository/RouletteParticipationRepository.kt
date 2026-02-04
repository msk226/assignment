package lg.voltup.repository

import lg.voltup.entity.RouletteParticipation
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RouletteParticipationRepository : JpaRepository<RouletteParticipation, Long> {
    fun existsByUserIdAndDate(userId: Long, date: LocalDate): Boolean
    fun findByUserIdAndDate(userId: Long, date: LocalDate): RouletteParticipation?
    fun findAllByDate(date: LocalDate): List<RouletteParticipation>
    fun findAllByUserId(userId: Long): List<RouletteParticipation>
    fun countByDate(date: LocalDate): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT r FROM RouletteParticipation r WHERE r.id = :participationId")
    fun findByIdWithLock(participationId: Long): RouletteParticipation?
}
