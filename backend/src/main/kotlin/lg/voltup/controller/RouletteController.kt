package lg.voltup.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lg.voltup.controller.dto.RouletteHistoryResponse
import lg.voltup.controller.dto.RouletteSpinResponse
import lg.voltup.controller.dto.RouletteStatusResponse
import lg.voltup.service.RouletteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "룰렛", description = "룰렛 참여 관련 API")
@RestController
@RequestMapping("/api/roulette")
class RouletteController(
    private val rouletteService: RouletteService,
) {
    @Operation(summary = "룰렛 참여", description = "룰렛을 돌려 포인트를 획득합니다. 1일 1회만 참여 가능합니다.")
    @PostMapping("/spin")
    fun spin(
        @RequestHeader("X-User-Id") userId: Long,
    ): ResponseEntity<RouletteSpinResponse> {
        return ResponseEntity.ok(rouletteService.spinRoulette(userId))
    }

    @Operation(summary = "룰렛 상태 조회", description = "오늘 참여 여부와 잔여 예산을 조회합니다.")
    @GetMapping("/status")
    fun getStatus(
        @RequestHeader("X-User-Id") userId: Long,
    ): ResponseEntity<RouletteStatusResponse> {
        return ResponseEntity.ok(rouletteService.getStatus(userId))
    }

    @Operation(summary = "당첨 내역 조회", description = "사용자의 룰렛 당첨 내역을 조회합니다.")
    @GetMapping("/history")
    fun getHistory(
        @RequestHeader("X-User-Id") userId: Long,
    ): ResponseEntity<List<RouletteHistoryResponse>> {
        return ResponseEntity.ok(rouletteService.getUserHistory(userId))
    }
}
