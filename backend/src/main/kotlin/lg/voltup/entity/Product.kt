package lg.voltup.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class Product private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    var description: String? = null,

    @Column(nullable = false)
    var price: Int,

    var stock: Int = 0,

    var imageUrl: String? = null,

    var isActive: Boolean = true,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            name: String,
            description: String?,
            price: Int,
            stock: Int,
            imageUrl: String?
        ): Product {
            require(name.isNotBlank()) { "상품명은 필수입니다." }
            require(price > 0) { "가격은 0보다 커야 합니다." }
            require(stock >= 0) { "재고는 0 이상이어야 합니다." }
            return Product(
                name = name,
                description = description,
                price = price,
                stock = stock,
                imageUrl = imageUrl
            )
        }
    }
}
