package run.piece.dev.refactoring

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/*
 * fun add(a: Int, b: Int): Int {
 *     return a + b
 * }
 */

internal class ExampleClassTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun example1() {
        // given
        val mock = mockk<ExampleClass>()
        every { mock.add(1, 2) } returns 3

        // when
        val result = mock.add(1, 2)

        // then
        assertEquals(3, result)
        verify { mock.add(1, 2) }
    }

    // Mock 객체의 호출 횟수 검증
    @Test
    fun example2() {
        // given
        val mock = mockk<ExampleClass>()
        every { mock.add(any(), any()) } returns 0

        // when
        mock.add(1, 2)
        mock.add(3, 4)

        // then
        verify(exactly = 2) { mock.add(any(), any()) }
    }

    // 특정 매개변수 값에 대한 동작 지정
    @Test
    fun example3() {
        // given
        val mock = mockk<ExampleClass>()
        every { mock.add(1, 2) } returns 100
        every { mock.add(3, 4) } returns 200

        // when
        val result1 = mock.add(1, 2)
        val result2 = mock.add(3, 4)

        // then
        assertEquals(100, result1)
        assertEquals(200, result2)
    }

    // 예외를 던지도록 동작 지정
    @Test
    fun example4() {
        // given
        val mock = mockk<ExampleClass>()
        every { mock.add(0, 0) } throws ArithmeticException()

        // when
        // 아래 호출은 ArithmeticException을 던집니다.
        // mock.add(0, 0)
    }

    // 인자 매칭 지정
    @Test
    fun example5() {
        // given
        val mock = mockk<ExampleClass>()
        every { mock.add(match { it > 0 }, match { it < 10 }) } returns 999

        // when
        val result = mock.add(5, 7)

        // then
        assertEquals(999, result)
        // 아래 호출은 인자 매칭에 맞지 않아 동작하지 않음
        // println(mock.add(-1, 5))
    }

    // slot을 사용한 인자 캡쳐 및 검증
    @Test
    fun example6() {
        // given
        val mock = mockk<ExampleClass>()
        val capturedSlot = slot<Int>()
        every { mock.add(capture(capturedSlot), any()) } returns 0

        // when
        mock.add(42, 99)

        // then
        assertEquals(42, capturedSlot.captured)
    }

    // 특정 순서로 호출되는지 검증
    @Test
    fun example7() {
        // given
        val mock = mockk<ExampleClass>()
        every { mock.add(1, 2) } returns 100
        every { mock.add(3, 4) } returns 200

        // when
        mock.add(1, 2)
        mock.add(3, 4)

        // then
        // 아래 검증은 주어진 순서대로 호출되지 않으면 실패
        verifyOrder {
            mock.add(1, 2)
            mock.add(3, 4)
        }
    }

    // 주어진 인자 값에 따라 다른 결과 반환
    @Test
    fun example8() {
        // given
        val mock = mockk<ExampleClass>()
        every { mock.add(1, 1) } returns 10
        every { mock.add(2, 2) } returns 20
        every { mock.add(any(), any()) } returns -1

        // when
        val result1 = mock.add(1, 1)
        val result2 = mock.add(2, 2)
        val result3 = mock.add(3, 3)

        // then
        assertEquals(10, result1)
        assertEquals(20, result2)
        assertEquals(-1, result3)
    }
}
