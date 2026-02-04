package lg.voltup.repository

import lg.voltup.entity.RouletteParticipation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RouletteParticipationRepository : JpaRepository<RouletteParticipation, Long> {
    fun existsByUserIdAndDate(userId: Long, date: LocalDate): Boolean
    fun findByUserIdAndDate(userId: Long, date: LocalDate): RouletteParticipation?
    fun findAllByDate(date: LocalDate): List<RouletteParticipation>
    fun findAllByUserId(userId: Long): List<RouletteParticipation>
    fun countByDate(date: LocalDate): Long
}
