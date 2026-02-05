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

    @Query("SELECT r FROM RouletteParticipation r WHERE r.userId = :userId ORDER BY r.createdAt DESC")
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<RouletteParticipation>

    @Query("SELECT r FROM RouletteParticipation r WHERE r.date = :date AND r.cancelledAt IS NULL")
    fun findAllActiveByDate(date: LocalDate): List<RouletteParticipation>

    @Query("SELECT COUNT(r) FROM RouletteParticipation r WHERE r.date = :date AND r.cancelledAt IS NULL")
    fun countActiveByDate(date: LocalDate): Long

    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RouletteParticipation r WHERE r.date = :date AND r.cancelledAt IS NULL")
    fun sumPointsByDate(date: LocalDate): Int
}
