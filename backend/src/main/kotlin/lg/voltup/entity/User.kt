package lg.voltup.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val nickname: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(nickname: String): User {
            return User(nickname = nickname)
        }
    }
}
