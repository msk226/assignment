package lg.voltup.service

import lg.voltup.entity.Point
import lg.voltup.entity.User
import lg.voltup.entity.enums.PointStatus
import lg.voltup.repository.PointRepository
import lg.voltup.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class PointServiceTest @Autowired constructor(
    private val pointService: PointService,
    private val pointRepository: PointRepository,
    private val userRepository: UserRepository
) {

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(User.create("testuser"))
    }

    @Test
    @DisplayName("포인트 잔액 조회")
    fun getBalance_shouldReturnTotalBalance() {
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))

        val balance = pointService.getBalance(testUser.id)

        assertEquals(1000, balance.totalBalance)
    }

    @Test
    @DisplayName("만료된 포인트는 잔액에 포함되지 않는다")
    fun getBalance_shouldExcludeExpiredPoints() {
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().minusDays(1)))

        val balance = pointService.getBalance(testUser.id)

        assertEquals(500, balance.totalBalance)
    }

    @Test
    @DisplayName("7일 내 만료 예정 포인트 조회")
    fun getExpiringPoints_shouldReturnPointsExpiringWithin7Days() {
        pointRepository.save(Point.create(testUser.id, 300, LocalDateTime.now().plusDays(3)))
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(10)))

        val expiring = pointService.getExpiringPoints(testUser.id)

        assertEquals(1, expiring.points.size)
        assertEquals(300, expiring.totalExpiringAmount)
    }

    @Test
    @DisplayName("포인트 목록 조회 시 만료 여부가 표시된다")
    fun getPoints_shouldShowExpiredStatus() {
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))
        pointRepository.save(Point.create(testUser.id, 300, LocalDateTime.now().minusDays(1)))

        val points = pointService.getPoints(testUser.id)

        assertEquals(2, points.size)
        assertTrue(points.any { !it.isExpired })
        assertTrue(points.any { it.isExpired })
    }

    @Test
    @DisplayName("포인트 목록 조회 시 상태가 표시된다")
    fun getPoints_shouldShowStatus() {
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))

        val points = pointService.getPoints(testUser.id)

        assertEquals(1, points.size)
        assertEquals("EARNED", points[0].status)
    }

    @Test
    @DisplayName("상태별 포인트 필터링 - EARNED")
    fun getPoints_filterByEarnedStatus() {
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))
        pointRepository.save(Point.create(testUser.id, 300, LocalDateTime.now().minusDays(1)))

        val earnedPoints = pointService.getPoints(testUser.id, PointStatus.EARNED)

        assertEquals(1, earnedPoints.size)
        assertEquals("EARNED", earnedPoints[0].status)
        assertEquals(500, earnedPoints[0].amount)
    }

    @Test
    @DisplayName("상태별 포인트 필터링 - EXPIRED")
    fun getPoints_filterByExpiredStatus() {
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))
        pointRepository.save(Point.create(testUser.id, 300, LocalDateTime.now().minusDays(1)))

        val expiredPoints = pointService.getPoints(testUser.id, PointStatus.EXPIRED)

        assertEquals(1, expiredPoints.size)
        assertEquals("EXPIRED", expiredPoints[0].status)
        assertEquals(300, expiredPoints[0].amount)
    }

    @Test
    @DisplayName("상태별 포인트 필터링 - CANCELED")
    fun getPoints_filterByCanceledStatus() {
        val earnedPoint = pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))
        val canceledPoint = pointRepository.save(Point.create(testUser.id, 300, LocalDateTime.now().plusDays(30)))
        canceledPoint.cancel()
        pointRepository.save(canceledPoint)

        val canceledPoints = pointService.getPoints(testUser.id, PointStatus.CANCELED)

        assertEquals(1, canceledPoints.size)
        assertEquals("CANCELED", canceledPoints[0].status)
        assertEquals(300, canceledPoints[0].amount)
    }

    @Test
    @DisplayName("상태 필터 없이 조회하면 모든 포인트가 조회된다")
    fun getPoints_withoutFilter_shouldReturnAll() {
        pointRepository.save(Point.create(testUser.id, 500, LocalDateTime.now().plusDays(30)))
        pointRepository.save(Point.create(testUser.id, 300, LocalDateTime.now().minusDays(1)))
        val canceledPoint = pointRepository.save(Point.create(testUser.id, 200, LocalDateTime.now().plusDays(30)))
        canceledPoint.cancel()
        pointRepository.save(canceledPoint)

        val allPoints = pointService.getPoints(testUser.id)

        assertEquals(3, allPoints.size)
    }
}
