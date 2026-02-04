package lg.voltup.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import lg.voltup.controller.dto.LoginRequest
import lg.voltup.controller.dto.LoginResponse
import lg.voltup.controller.dto.UserResponse
import lg.voltup.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "인증", description = "로그인 관련 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "로그인", description = "닉네임으로 로그인합니다. 없는 닉네임이면 자동 생성됩니다.")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 유저 정보를 조회합니다.")
    @GetMapping("/me")
    fun getMe(@RequestHeader("X-User-Id") userId: Long): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(authService.getUser(userId))
    }
}
