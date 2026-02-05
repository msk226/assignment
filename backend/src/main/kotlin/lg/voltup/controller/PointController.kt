package lg.voltup.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import lg.voltup.controller.dto.ExpiringPointsResponse
import lg.voltup.controller.dto.PointBalanceResponse
import lg.voltup.controller.dto.PointResponse
import lg.voltup.entity.enums.PointStatus
import lg.voltup.service.PointService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "포인트", description = "포인트 조회 관련 API")
@RestController
@RequestMapping("/api/points")
class PointController(
    private val pointService: PointService
) {

    @Operation(summary = "내 포인트 목록", description = "내 포인트 목록을 조회합니다. 상태별 필터링이 가능합니다.")
    @GetMapping
    fun getPoints(
        @RequestHeader("X-User-Id") userId: Long,
        @Parameter(description = "포인트 상태 필터 (EARNED, EXPIRED, CANCELED)")
        @RequestParam(required = false) status: PointStatus?
    ): ResponseEntity<List<PointResponse>> {
        return ResponseEntity.ok(pointService.getPoints(userId, status))
    }

    @Operation(summary = "포인트 잔액 조회", description = "사용 가능한 총 포인트를 조회합니다.")
    @GetMapping("/balance")
    fun getBalance(@RequestHeader("X-User-Id") userId: Long): ResponseEntity<PointBalanceResponse> {
        return ResponseEntity.ok(pointService.getBalance(userId))
    }

    @Operation(summary = "만료 예정 포인트", description = "7일 내 만료 예정인 포인트를 조회합니다.")
    @GetMapping("/expiring")
    fun getExpiringPoints(@RequestHeader("X-User-Id") userId: Long): ResponseEntity<ExpiringPointsResponse> {
        return ResponseEntity.ok(pointService.getExpiringPoints(userId))
    }
}
