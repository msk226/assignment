package lg.voltup.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserTest {
    @Test
    fun `create 메서드로 사용자를 생성할 수 있다`() {
        val nickname = "테스트유저"

        val user = User.create(nickname)

        assertAll(
            { assertNotNull(user) },
            { assertEquals(nickname, user.nickname) },
            { assertNotNull(user.createdAt) },
        )
    }

    @Test
    fun `사용자 생성 시 id는 0으로 초기화된다`() {
        val user = User.create("테스트유저")

        assertEquals(0L, user.id)
    }
}
