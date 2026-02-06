package lg.voltup.entity

import jakarta.persistence.*
import lg.voltup.exception.InsufficientStockException
import lg.voltup.exception.ProductNotAvailableException
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
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            name: String,
            description: String?,
            price: Int,
            stock: Int,
            imageUrl: String?,
        ): Product {
            require(name.isNotBlank()) { "상품명은 필수입니다." }
            require(price > 0) { "가격은 0보다 커야 합니다." }
            require(stock >= 0) { "재고는 0 이상이어야 합니다." }
            return Product(
                name = name,
                description = description,
                price = price,
                stock = stock,
                imageUrl = imageUrl,
            )
        }
    }

    fun validatePurchasable() {
        if (!isActive) {
            throw ProductNotAvailableException("판매 중인 상품이 아닙니다.")
        }
        if (stock <= 0) {
            throw InsufficientStockException("재고가 부족합니다.")
        }
    }

    fun purchase() {
        if (stock <= 0) {
            throw InsufficientStockException("재고가 부족합니다.")
        }
        stock -= 1
        updatedAt = LocalDateTime.now()
    }

    fun restoreStock(quantity: Int = 1) {
        require(quantity > 0) { "복구 수량은 0보다 커야 합니다." }
        stock += quantity
        updatedAt = LocalDateTime.now()
    }

    fun deactivate() {
        isActive = false
        updatedAt = LocalDateTime.now()
    }

    fun update(
        name: String? = null,
        description: String? = null,
        price: Int? = null,
        stock: Int? = null,
        imageUrl: String? = null,
        isActive: Boolean? = null,
    ) {
        name?.let {
            require(it.isNotBlank()) { "상품명은 필수입니다." }
            this.name = it
        }
        description?.let { this.description = it }
        price?.let {
            require(it > 0) { "가격은 0보다 커야 합니다." }
            this.price = it
        }
        stock?.let {
            require(it >= 0) { "재고는 0 이상이어야 합니다." }
            this.stock = it
        }
        imageUrl?.let { this.imageUrl = it }
        isActive?.let { this.isActive = it }
        updatedAt = LocalDateTime.now()
    }
}
