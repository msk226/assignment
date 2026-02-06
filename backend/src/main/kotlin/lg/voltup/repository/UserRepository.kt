package lg.voltup.repository

import lg.voltup.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByNickname(nickname: String): User?

    fun existsByNickname(nickname: String): Boolean
}
