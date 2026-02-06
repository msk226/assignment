package lg.voltup.entity.enums

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderStatusTest {
    @Test
    fun `OrderStatus는 COMPLETED와 CANCELLED 값을 가진다`() {
        val values = OrderStatus.entries

        assertEquals(2, values.size)
        assertTrue(values.contains(OrderStatus.COMPLETED))
        assertTrue(values.contains(OrderStatus.CANCELLED))
    }

    @Test
    fun `COMPLETED 상태의 name은 COMPLETED이다`() {
        assertEquals("COMPLETED", OrderStatus.COMPLETED.name)
    }

    @Test
    fun `CANCELLED 상태의 name은 CANCELLED이다`() {
        assertEquals("CANCELLED", OrderStatus.CANCELLED.name)
    }
}
