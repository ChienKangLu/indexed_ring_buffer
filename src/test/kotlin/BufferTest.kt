import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.*

class BufferTest {
    @Test
    fun `should read data from input stream and fill obtained buffer`() {
        // GIVEN
        val ringBuffer = IndexedRingBuffer(10)

        // WHEN
        val buffer = ringBuffer.obtain(4)
        val data = byteArrayOf(1, 2, 3, 4)
        val stream = ByteArrayInputStream(data)
        buffer?.read(stream)

        // THEN
        val copied = buffer?.copy()
        assertTrue(Arrays.equals(data, copied))
    }

    @Test
    fun `should read data from input stream and fill obtained buffer while reusing recycled parts`() {
        // GIVEN
        val ringBuffer = IndexedRingBuffer(10)
        val buffer1 = ringBuffer.obtain(8)
        buffer1?.recycle()

        // WHEN
        val buffer = ringBuffer.obtain(5)
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val stream = ByteArrayInputStream(data)
        buffer?.read(stream)

        // THEN
        val copied = buffer?.copy()
        assertTrue(Arrays.equals(data, copied))
    }
}