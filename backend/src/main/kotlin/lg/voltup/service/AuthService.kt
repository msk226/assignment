package lg.voltup.service

import lg.voltup.controller.dto.LoginRequest
import lg.voltup.controller.dto.LoginResponse
import lg.voltup.controller.dto.UserResponse
import lg.voltup.entity.User
import lg.voltup.exception.UserNotFoundException
import lg.voltup.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByNickname(request.nickname)
            ?: userRepository.save(User.create(request.nickname))

        return LoginResponse(
            userId = user.id,
            nickname = user.nickname
        )
    }

    fun getUser(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("사용자를 찾을 수 없습니다.") }

        return UserResponse(
            id = user.id,
            nickname = user.nickname
        )
    }

    fun getUserByNickname(nickname: String): User? {
        return userRepository.findByNickname(nickname)
    }
}
