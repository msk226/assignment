package lg.voltup.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Int,
    val stock: Int,
    val imageUrl: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)

data class ProductCreateRequest(
    @field:NotBlank(message = "상품명은 필수입니다.")
    @field:Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
    val name: String,
    val description: String? = null,
    @field:Positive(message = "가격은 양수여야 합니다.")
    val price: Int,
    @field:PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
    val stock: Int = 0,
    val imageUrl: String? = null
)

data class ProductUpdateRequest(
    @field:Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
    val name: String? = null,
    val description: String? = null,
    @field:Positive(message = "가격은 양수여야 합니다.")
    val price: Int? = null,
    @field:PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
    val stock: Int? = null,
    val imageUrl: String? = null,
    val isActive: Boolean? = null
)
