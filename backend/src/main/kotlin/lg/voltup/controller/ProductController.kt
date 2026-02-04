package lg.voltup.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lg.voltup.controller.dto.ProductResponse
import lg.voltup.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "상품", description = "상품 조회 관련 API (사용자)")
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    @Operation(summary = "상품 목록 조회", description = "판매 중인 상품 목록을 조회합니다.")
    @GetMapping
    fun getProducts(): ResponseEntity<List<ProductResponse>> {
        return ResponseEntity.ok(productService.getActiveProducts())
    }

    @Operation(summary = "상품 상세 조회", description = "상품 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: Long): ResponseEntity<ProductResponse> {
        return ResponseEntity.ok(productService.getProduct(productId))
    }
}
