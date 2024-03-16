import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IndexedRingBufferTest {
    @Test
    fun `should obtain buffers till the end`() {
        // GIVEN
        val ringBuffer = IndexedRingBuffer(6)

        // WHEN
        val buffer1 = ringBuffer.obtain(3)
        val buffer2 = ringBuffer.obtain(2)
        val buffer3 = ringBuffer.obtain(1)
        val buffer4 = ringBuffer.obtain(1)

        // THEN
        buffer1.verify(0, 3)
        buffer2.verify(3, 5)
        buffer3.verify(5, 0)
        assertEquals(buffer4, null)
        ringBuffer.verify(0, 5, 6, 0)
        assertEquals(ringBuffer.isFull(), true)
    }

    @Test
    fun `should obtain no buffer when no enough space`() {
        // GIVEN
        val ringBuffer = IndexedRingBuffer(6)

        // WHEN
        val buffer1 = ringBuffer.obtain(3)
        val buffer2 = ringBuffer.obtain(100)

        // THEN
        buffer1.verify(0, 3)
        assertEquals(buffer2, null)
        ringBuffer.verify(0, 2, 3, 3)
    }

    @Test
    fun `should obtain buffer after recycled`() {
        // GIVEN
        val ringBuffer = IndexedRingBuffer(3)

        // WHEN obtain buffer1 and buffer2
        val buffer1 = ringBuffer.obtain(2)
        val buffer2 = ringBuffer.obtain(1)
        buffer1.verify(0, 2)
        buffer2.verify(2, 0)
        ringBuffer.verify(0, 2, 3, 0)

        // WHEN recycle buffer1
        buffer1!!.recycle()
        ringBuffer.verify(2, 2, 1, 2)

        // WHEN obtain buffer3
        val buffer3 = ringBuffer.obtain(2)
        buffer3.verify(0, 2)
        ringBuffer.verify(2, 4, 3, 0)

        // WHEN obtain new buffer failed
        val buffer4 = ringBuffer.obtain(1)
        assertEquals(buffer4, null)
    }

    @Test
    fun `should obtain new buffer once all buffer are recycled`() {
        // GIVEN
        val ringBuffer = IndexedRingBuffer(3)

        // WHEN obtain buffer1 and buffer2
        val buffer1 = ringBuffer.obtain(2)
        val buffer2 = ringBuffer.obtain(1)
        buffer1.verify(0, 2)
        buffer2.verify(2, 0)
        ringBuffer.verify(0, 2, 3, 0)

        // WHEN recycle buffer1
        buffer1?.recycle()
        ringBuffer.verify(2, 2, 1, 2)

        // WHEN recycle buffer2
        buffer2?.recycle()
        ringBuffer.verify(3, 2, 0, 3)
        assertEquals(ringBuffer.isEmpty(), true)

        // WHEN obtain buffer3
        val buffer3 = ringBuffer.obtain(3)
        buffer3.verify(0, 0)
        ringBuffer.verify(3, 5, 3, 0)
    }

    @Test
    fun `should obtain buffer whose start is larger than end when using part of recycled range`() {
        // GIVEN
        val ringBuffer = IndexedRingBuffer(10)

        // WHEN obtain buffer1, buffer2 and buffer3
        val buffer1 = ringBuffer.obtain(2)
        buffer1.verify(0, 2)
        val buffer2 = ringBuffer.obtain(5)
        buffer2.verify(2, 7)
        val buffer3 = ringBuffer.obtain(2)
        buffer3.verify(7, 9)
        ringBuffer.verify(0, 8, 9, 1)

        // WHEN recycle buffer1
        buffer1?.recycle()
        ringBuffer.verify(2, 8, 7, 3)

        // WHEN obtain buffer4 (start > end)
        val buffer4 = ringBuffer.obtain(2)
        buffer4.verify(9, 1)
        ringBuffer.verify(2, 10, 9, 1)

        // WHEN recycle buffer2
        buffer2?.recycle()
        ringBuffer.verify(7, 10, 4, 6)

        // WHEN recycle buffer3
        buffer3?.recycle()
        ringBuffer.verify(9, 10, 2, 8)

        // WHEN recycle buffer4
        buffer4?.recycle()
        ringBuffer.verify(11, 10, 0, 10)
    }

    private fun IndexedRingBuffer.Buffer?.verify(start: Int, end: Int) {
        assertEquals(this?.start, start)
        assertEquals(this?.end, end)
    }

    private fun IndexedRingBuffer.verify(head: Int, tail: Int, used: Int, remaining: Int) {
        assertEquals(head, head)
        assertEquals(tail, tail)
        assertEquals(usedSize(), used)
        assertEquals(remaining(), remaining)
    }
}