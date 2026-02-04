package lg.voltup.service

import lg.voltup.controller.dto.LoginRequest
import lg.voltup.exception.UserNotFoundException
import lg.voltup.repository.UserRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@Transactional
class AuthServiceTest @Autowired constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository
) {

    @Test
    @DisplayName("새로운 닉네임으로 로그인하면 유저가 생성된다")
    fun login_shouldCreateNewUser() {
        val request = LoginRequest(nickname = "newuser")

        val response = authService.login(request)

        assertEquals("newuser", response.nickname)
        assertNotNull(response.userId)

        val user = userRepository.findByNickname("newuser")
        assertNotNull(user)
    }

    @Test
    @DisplayName("기존 닉네임으로 로그인하면 기존 유저가 반환된다")
    fun login_shouldReturnExistingUser() {
        val request = LoginRequest(nickname = "existinguser")

        val firstLogin = authService.login(request)
        val secondLogin = authService.login(request)

        assertEquals(firstLogin.userId, secondLogin.userId)
        assertEquals(firstLogin.nickname, secondLogin.nickname)
    }

    @Test
    @DisplayName("유저 정보 조회")
    fun getUser_shouldReturnUserInfo() {
        val loginRequest = LoginRequest(nickname = "testuser")
        val loginResponse = authService.login(loginRequest)

        val userResponse = authService.getUser(loginResponse.userId)

        assertEquals(loginResponse.userId, userResponse.id)
        assertEquals("testuser", userResponse.nickname)
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 예외 발생")
    fun getUser_shouldThrowExceptionWhenNotFound() {
        assertThrows<UserNotFoundException> {
            authService.getUser(999L)
        }
    }
}
