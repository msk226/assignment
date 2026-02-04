package lg.voltup.service

import lg.voltup.entity.Point
import lg.voltup.entity.User
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
}
