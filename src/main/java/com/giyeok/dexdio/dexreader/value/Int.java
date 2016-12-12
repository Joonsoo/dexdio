package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

public class Int extends Value {
	private int value;
	
	public Int() { }
	public Int(int value) { this.value = value; }
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		value = stream.readInt();
	}
	
	public int getValue() {
		return value;
	}
	
	public long getUnsignedValue() {
		return (((long) value) & 0xffffffffL);
	}
	
	@Override
	public String toString() {
		return "int" + value;
	}
	
	@Override
	public int getByteLength() {
		return 4;
	}
}

