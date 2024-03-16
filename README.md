# Indexed Ring Buffer
A ring buffer implemented in Kotlin. User can obtain indexed buffers with dynamic size, fill obtained buffer with data and recycle obtained buffers in order.

The main idea comes from https://www.baeldung.com/java-ring-buffer and is appended with subrange of buffer given the desired size.

Notice: it is NOT designed for thread-safe yet, welcome to fork and synchronize it per your requirements.

## Usage

Initialize `IndexedRingBuffer` given a fixed size. It will allocate a `ByteArray` as backing filed:

```kotlin
val ringBuffer = IndexedRingBuffer(6)
```

Obtain several buffers with different sizes:

```kotlin
val buffer1 = ringBuffer.obtain(3)
val buffer2 = ringBuffer.obtain(2)
val buffer3 = ringBuffer.obtain(1)
```

Each buffer have `start` and `end` indicating the subrange of the backing `ByteArray` inside `IndexedRingBuffer`.

```kotlin
assert(buffer1.start == 0) 
assert(buffer1.end == 3)
```

If there is no remaining space, `IndexedRingBuffer.obtain()` will return `null`.

```kotlin
val buffer4 = ringBuffer.obtain(100)
assert(buffer4 == null)
```

To fill obtained buffer with data from `InputStream`:

```kotlin
val data = byteArrayOf(1, 2, 3)
val stream = ByteArrayInputStream(data)
buffer1?.read(stream)
```

To copy the data from obtained buffer:

```kotlin
val copied = buffer1?.copy()
```

To recycle the obtained buffer:

```kotlin
buffer1.recycle()
buffer2.recycle()
buffer3.recycle()
```

Notice: buffers must be recycled in order (first-in-first-out), otherwise the index will be wrong.

## Reference
https://www.baeldung.com/java-ring-buffer