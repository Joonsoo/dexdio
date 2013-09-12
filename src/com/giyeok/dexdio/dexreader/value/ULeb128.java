package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

//In a .dex file, LEB128 is only ever used to encode 32-bit quantities

public class ULeb128 extends Value {
	private long value;
	private int length;
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		int b;

		value = 0;
		length = 0;
		do {
			b = stream.readUnsignedByte();
			value |= ((b & 0x7f) << (length * 7));
			length++;
		} while ((b & 0x80) != 0);
	}

	public long getValue() {
		return value;
	}
	
	public long getUnsignedP1() {
		return value - 1;
	}
	
	@Override
	public int getByteLength() {
		return length;
	}
	
	@Override
	public String toString() {
		return "uleb" + value;
	}
}
