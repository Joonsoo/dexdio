package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.RandomAccessible;

public class Short extends Value {
	private short value;
	
	public Short() { }
	public Short(short value) { this.value = value; }
	
	@Override
	public void read(RandomAccessible stream) throws IOException {
		value = stream.readShort();
	}
	
	public short getValue() {
		return value;
	}
	
	public int getUnsignedValue() {
		return (((int) value) & 0xffff);
	}
	
	@Override
	public String toString() {
		return "short" + value;
	}
	
	@Override
	public int getByteLength() {
		return 2;
	}
}
