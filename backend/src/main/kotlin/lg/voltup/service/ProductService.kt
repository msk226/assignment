package lg.voltup.service

import lg.voltup.controller.dto.ProductResponse
import lg.voltup.entity.Product
import lg.voltup.exception.ProductNotFoundException
import lg.voltup.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    fun getActiveProducts(): List<ProductResponse> {
        return productRepository.findAllByIsActiveTrue().map { it.toResponse() }
    }

    fun getProduct(productId: Long): ProductResponse {
        return findProductById(productId).toResponse()
    }

    private fun findProductById(productId: Long): Product {
        return productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException("상품을 찾을 수 없습니다.") }
    }

    private fun Product.toResponse() = ProductResponse(
        id = id,
        name = name,
        description = description,
        price = price,
        stock = stock,
        imageUrl = imageUrl,
        isActive = isActive,
        createdAt = createdAt
    )
}
