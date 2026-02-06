package lg.voltup.service

import lg.voltup.controller.dto.ProductCreateRequest
import lg.voltup.controller.dto.ProductResponse
import lg.voltup.controller.dto.ProductUpdateRequest
import lg.voltup.entity.Product
import lg.voltup.exception.ProductNotFoundException
import lg.voltup.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    fun getActiveProducts(): List<ProductResponse> {
        return productRepository.findAllByIsActiveTrue().map { it.toResponse() }
    }

    fun getAllProducts(): List<ProductResponse> {
        return productRepository.findAll().map { it.toResponse() }
    }

    fun getProduct(productId: Long): ProductResponse {
        return findProductById(productId).toResponse()
    }

    @Transactional
    fun createProduct(request: ProductCreateRequest): ProductResponse {
        val product =
            Product.create(
                name = request.name,
                description = request.description,
                price = request.price,
                stock = request.stock,
                imageUrl = request.imageUrl,
            )
        return productRepository.save(product).toResponse()
    }

    @Transactional
    fun updateProduct(
        productId: Long,
        request: ProductUpdateRequest,
    ): ProductResponse {
        val product = findProductById(productId)
        product.update(
            name = request.name,
            description = request.description,
            price = request.price,
            stock = request.stock,
            imageUrl = request.imageUrl,
            isActive = request.isActive,
        )
        return product.toResponse()
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        findProductById(productId).deactivate()
    }

    private fun findProductById(productId: Long): Product {
        return productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException("상품을 찾을 수 없습니다.") }
    }

    private fun Product.toResponse() =
        ProductResponse(
            id = id,
            name = name,
            description = description,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            isActive = isActive,
            createdAt = createdAt,
        )
}
