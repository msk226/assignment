package lg.voltup.controller.dto

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
    val name: String,
    val description: String? = null,
    val price: Int,
    val stock: Int = 0,
    val imageUrl: String? = null
)

data class ProductUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Int? = null,
    val stock: Int? = null,
    val imageUrl: String? = null,
    val isActive: Boolean? = null
)
