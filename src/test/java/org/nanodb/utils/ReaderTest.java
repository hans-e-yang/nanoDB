package org.nanodb.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.nanodb.internal.primitives.Int32Type;
import org.nanodb.internal.primitives.DataType;
import org.nanodb.utils.Reader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Reader}. Focuses on boundary handling, sequential state
 * advancement and proper exception throwing when reading beyond available bytes.
 */
public class ReaderTest {

	private final Int32Type int32 = new Int32Type();

	@Test
	@DisplayName("Reads a single Int32 value correctly")
	void readSingleInt32() {
		byte[] data = int32.toBytes(16909060); // 0x01020304
		Reader reader = new Reader(data);
		Integer value = reader.read(int32);
		assertEquals(16909060, value);
		// Further read should fail (offset == data.length)
		assertThrows(IllegalArgumentException.class, () -> reader.read(int32));
	}

	@Test
	@DisplayName("Reads two sequential Int32 values and advances state")
	void readSequentialInt32() {
		byte[] first = int32.toBytes(5);
		byte[] second = int32.toBytes(7);
		byte[] data = new byte[first.length + second.length];
		System.arraycopy(first, 0, data, 0, first.length);
		System.arraycopy(second, 0, data, first.length, second.length);

		Reader reader = new Reader(data);
		assertEquals(5, reader.read(int32));
		assertEquals(7, reader.read(int32));
		// No more bytes left
		assertThrows(IllegalArgumentException.class, () -> reader.read(int32));
	}

	@Test
	@DisplayName("Respects starting offset when using second constructor")
	void readWithStartingOffset() {
		byte[] first = int32.toBytes(111);
		byte[] second = int32.toBytes(222);
		byte[] combined = new byte[first.length + second.length];
		System.arraycopy(first, 0, combined, 0, first.length);
		System.arraycopy(second, 0, combined, first.length, second.length);

		// Start at the beginning of the second int (offset == first.length)
		Reader reader = new Reader(combined, first.length);
		assertEquals(222, reader.read(int32));
		assertThrows(IllegalArgumentException.class, () -> reader.read(int32));
	}

	@Test
	@DisplayName("Throws when attempting to read beyond chunk boundary")
	void throwsOnOverflowImmediate() {
		// Only 3 bytes, but Int32 requires 4
		byte[] tooSmall = new byte[]{1, 2, 3};
		Reader reader = new Reader(tooSmall);
		assertThrows(IllegalArgumentException.class, () -> reader.read(int32));
	}

	@Test
	@DisplayName("Reads custom 1-byte DataType sequentially then overflows on extra read")
	void readCustomOneByteType() {
		DataType<Byte> oneByteType = new DataType<>() {
			@Override public Byte fromBytes(byte[] bytes, int offset) { return bytes[offset]; }
			@Override public byte[] toBytes(Byte data) { return new byte[]{data}; }
			@Override public int byteSize() { return 1; }
			@Override public <T2> T2 accept(org.nanodb.internal.primitives.DataTypeVisitor<T2> visitor) { return null; }
		};

		byte[] data = new byte[]{10, 20, 30};
		Reader reader = new Reader(data);
		assertEquals((byte)10, reader.read(oneByteType));
		assertEquals((byte)20, reader.read(oneByteType));
		assertEquals((byte)30, reader.read(oneByteType));
		assertThrows(IllegalArgumentException.class, () -> reader.read(oneByteType));
	}
}
