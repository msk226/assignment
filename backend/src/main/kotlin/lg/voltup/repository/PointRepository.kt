package lg.voltup.repository

import lg.voltup.entity.Point
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PointRepository : JpaRepository<Point, Long> {
    fun findAllByUserId(userId: Long): List<Point>

    @Query("SELECT p FROM Point p WHERE p.userId = :userId AND p.expiresAt > :now AND p.usedAmount < p.amount ORDER BY p.expiresAt ASC")
    fun findValidPointsByUserId(userId: Long, now: LocalDateTime = LocalDateTime.now()): List<Point>

    @Query("SELECT p FROM Point p WHERE p.userId = :userId AND p.expiresAt > :now AND p.expiresAt <= :expiringDate AND p.usedAmount < p.amount ORDER BY p.expiresAt ASC")
    fun findExpiringPoints(userId: Long, now: LocalDateTime, expiringDate: LocalDateTime): List<Point>

    @Query("SELECT COALESCE(SUM(p.amount - p.usedAmount), 0) FROM Point p WHERE p.userId = :userId AND p.expiresAt > :now AND p.usedAmount < p.amount")
    fun getAvailableBalance(userId: Long, now: LocalDateTime = LocalDateTime.now()): Int
}
