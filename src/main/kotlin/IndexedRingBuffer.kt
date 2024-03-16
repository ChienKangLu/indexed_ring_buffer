import com.google.common.annotations.VisibleForTesting
import java.io.InputStream
import kotlin.math.min

class IndexedRingBuffer(val size: Int) {
    companion object {
        private const val MAX_BUFFER_SIZE = 2048
    }

    /**
     * Subrange of [buffer] from [start] inclusive and ending [end] exclusive.
     */
    class Buffer(
        private val parent: IndexedRingBuffer,
        private val buffer: ByteArray,
        val start: Int,
        val end: Int
    ) {
        val size = if (end > start) {
            end - start
        } else {
            (parent.size - start) + end
        }

        fun read(inputStream: InputStream) {
            if (end > start) {
                read(inputStream, start, size)
            } else {
                read(inputStream, start, parent.size - start)
                read(inputStream, 0, end)
            }
        }

        private fun read(inputStream: InputStream, start: Int, total: Int) {
            var remaining = total
            var read = 0
            while (remaining > 0) {
                val size = min(MAX_BUFFER_SIZE, remaining)
                inputStream.read(buffer, start + read, size)
                remaining -= size
                read += size
            }
        }

        fun copy(): ByteArray {
            return if (end > start) {
                buffer.copyOfRange(start, end)
            } else {
                val copy = ByteArray(size)
                buffer.copyInto(copy, 0, start, parent.size)
                buffer.copyInto(copy, parent.size - start, 0, end)
                copy
            }
        }

        fun recycle() = parent.recycle(this)

        override fun toString(): String {
            return "Buffer(start=$start, end=$end)"
        }
    }

    @VisibleForTesting
    var head = 0
    @VisibleForTesting
    var tail = -1
    private val buffer = ByteArray(size)

    fun obtain(size: Int): Buffer? {
        if (!hasSpace(size)) {
            return null
        }

        val start = (tail + 1) % this.size
        val end = (start + size) % this.size
        tail += size

        return Buffer(this, buffer, start, end)
    }

    fun recycle(buffer: Buffer) {
        head += buffer.size
    }

    fun isEmpty(): Boolean {
        return tail < head
    }

    fun isFull(): Boolean {
        return usedSize() == size
    }

    fun remaining(): Int {
        return size - usedSize()
    }

    fun usedSize(): Int {
        return (tail - head) + 1
    }

    private fun hasSpace(size: Int): Boolean {
        return this.size - usedSize() >= size
    }

    override fun toString(): String {
        return "IndexedRingBuffer(head=$head, tail=$tail, used=${usedSize()}, remaining=${remaining()})"
    }
}