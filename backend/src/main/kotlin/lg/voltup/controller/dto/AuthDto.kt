package lg.voltup.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 1, max = 20, message = "닉네임은 1~20자여야 합니다.")
    val nickname: String
)

data class LoginResponse(
    val userId: Long,
    val nickname: String
)

data class UserResponse(
    val id: Long,
    val nickname: String
)
