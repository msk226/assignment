package lg.voltup.repository

import lg.voltup.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>
}
