package lg.voltup.controller.dto

data class LoginRequest(
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
