package lg.voltup.repository

import lg.voltup.entity.Order
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>
    fun findAllByOrderByCreatedAtDesc(): List<Order>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    fun findByIdWithLock(orderId: Long): Order?
}
