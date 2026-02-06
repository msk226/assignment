package lg.voltup.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductTest {
    @Test
    fun `create 메서드로 상품을 생성할 수 있다`() {
        val name = "테스트상품"
        val description = "테스트 설명"
        val price = 10000
        val stock = 100
        val imageUrl = "https://example.com/image.jpg"

        val product = Product.create(name, description, price, stock, imageUrl)

        assertAll(
            { assertNotNull(product) },
            { assertEquals(name, product.name) },
            { assertEquals(description, product.description) },
            { assertEquals(price, product.price) },
            { assertEquals(stock, product.stock) },
            { assertEquals(imageUrl, product.imageUrl) },
            { assertTrue(product.isActive) },
        )
    }

    @Test
    fun `description과 imageUrl이 null인 상품을 생성할 수 있다`() {
        val product =
            Product.create(
                name = "테스트상품",
                description = null,
                price = 10000,
                stock = 100,
                imageUrl = null,
            )

        assertAll(
            { assertNull(product.description) },
            { assertNull(product.imageUrl) },
        )
    }

    @Test
    fun `상품명이 빈 문자열일 경우 예외가 발생한다`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product.create(
                    name = "",
                    description = null,
                    price = 10000,
                    stock = 100,
                    imageUrl = null,
                )
            }

        assertEquals("상품명은 필수입니다.", exception.message)
    }

    @Test
    fun `상품명이 공백만 있을 경우 예외가 발생한다`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product.create(
                    name = "   ",
                    description = null,
                    price = 10000,
                    stock = 100,
                    imageUrl = null,
                )
            }

        assertEquals("상품명은 필수입니다.", exception.message)
    }

    @Test
    fun `가격이 0일 경우 예외가 발생한다`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product.create(
                    name = "테스트상품",
                    description = null,
                    price = 0,
                    stock = 100,
                    imageUrl = null,
                )
            }

        assertEquals("가격은 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `가격이 음수일 경우 예외가 발생한다`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product.create(
                    name = "테스트상품",
                    description = null,
                    price = -1000,
                    stock = 100,
                    imageUrl = null,
                )
            }

        assertEquals("가격은 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `재고가 음수일 경우 예외가 발생한다`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Product.create(
                    name = "테스트상품",
                    description = null,
                    price = 10000,
                    stock = -1,
                    imageUrl = null,
                )
            }

        assertEquals("재고는 0 이상이어야 합니다.", exception.message)
    }

    @Test
    fun `재고가 0인 상품을 생성할 수 있다`() {
        val product =
            Product.create(
                name = "테스트상품",
                description = null,
                price = 10000,
                stock = 0,
                imageUrl = null,
            )

        assertEquals(0, product.stock)
    }

    @Test
    fun `상품 생성 시 isActive는 true로 초기화된다`() {
        val product =
            Product.create(
                name = "테스트상품",
                description = null,
                price = 10000,
                stock = 100,
                imageUrl = null,
            )

        assertTrue(product.isActive)
    }
}
