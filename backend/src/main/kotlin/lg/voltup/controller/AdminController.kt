package lg.voltup.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lg.voltup.controller.dto.*
import lg.voltup.service.AdminService
import lg.voltup.service.OrderService
import lg.voltup.service.ProductService
import lg.voltup.service.RouletteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "어드민", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService,
    private val productService: ProductService,
    private val orderService: OrderService,
    private val rouletteService: RouletteService
) {

    // === 대시보드 ===
    @Operation(summary = "대시보드", description = "오늘의 예산 현황, 참여자 수, 지급 포인트를 조회합니다.")
    @GetMapping("/dashboard")
    fun getDashboard(): ResponseEntity<DashboardResponse> {
        return ResponseEntity.ok(adminService.getDashboard())
    }

    // === 예산 관리 ===
    @Operation(summary = "예산 조회", description = "오늘의 일일 예산을 조회합니다.")
    @GetMapping("/budget")
    fun getBudget(): ResponseEntity<BudgetResponse> {
        return ResponseEntity.ok(adminService.getTodayBudget())
    }

    @Operation(summary = "예산 설정", description = "오늘의 일일 예산을 설정합니다.")
    @PutMapping("/budget")
    fun updateBudget(@RequestBody request: BudgetUpdateRequest): ResponseEntity<BudgetResponse> {
        return ResponseEntity.ok(adminService.updateTodayBudget(request))
    }

    // === 룰렛 참여 관리 ===
    @Operation(summary = "룰렛 참여 목록", description = "오늘의 룰렛 참여 목록을 조회합니다.")
    @GetMapping("/roulette")
    fun getRouletteParticipations(): ResponseEntity<List<RouletteParticipationResponse>> {
        return ResponseEntity.ok(rouletteService.getTodayParticipations())
    }

    @Operation(summary = "룰렛 참여 취소", description = "룰렛 참여를 취소하고 포인트를 회수합니다.")
    @DeleteMapping("/roulette/{participationId}")
    fun cancelRouletteParticipation(@PathVariable participationId: Long): ResponseEntity<Unit> {
        rouletteService.cancelParticipation(participationId)
        return ResponseEntity.noContent().build()
    }

    // === 상품 관리 ===
    @Operation(summary = "상품 목록 (어드민)", description = "전체 상품 목록을 조회합니다. (비활성 포함)")
    @GetMapping("/products")
    fun getProducts(): ResponseEntity<List<ProductResponse>> {
        return ResponseEntity.ok(productService.getAllProducts())
    }

    @Operation(summary = "상품 등록", description = "새 상품을 등록합니다.")
    @PostMapping("/products")
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<ProductResponse> {
        return ResponseEntity.ok(productService.createProduct(request))
    }

    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다.")
    @PutMapping("/products/{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: ProductUpdateRequest
    ): ResponseEntity<ProductResponse> {
        return ResponseEntity.ok(productService.updateProduct(productId, request))
    }

    @Operation(summary = "상품 삭제", description = "상품을 비활성화합니다.")
    @DeleteMapping("/products/{productId}")
    fun deleteProduct(@PathVariable productId: Long): ResponseEntity<Unit> {
        productService.deleteProduct(productId)
        return ResponseEntity.noContent().build()
    }

    // === 주문 관리 ===
    @Operation(summary = "전체 주문 목록", description = "전체 주문 목록을 조회합니다.")
    @GetMapping("/orders")
    fun getOrders(): ResponseEntity<List<OrderResponse>> {
        return ResponseEntity.ok(orderService.getAllOrders())
    }

    @Operation(summary = "주문 취소", description = "주문을 취소하고 포인트를 환불합니다.")
    @DeleteMapping("/orders/{orderId}")
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.cancelOrder(orderId))
    }
}
