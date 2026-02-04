package lg.voltup.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "roulette_participations",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "date"])]
)
class RouletteParticipation private constructor(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val date: LocalDate,

    val points: Int,

    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(userId: Long, date: LocalDate, points: Int): RouletteParticipation {
            return RouletteParticipation(
                userId = userId,
                date = date,
                points = points
            )
        }
    }
}
