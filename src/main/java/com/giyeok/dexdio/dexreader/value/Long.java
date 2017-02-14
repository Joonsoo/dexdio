package com.giyeok.dexdio.dexreader.value;

import com.giyeok.dexdio.dexreader.RandomAccessible;

import java.io.IOException;

public class Long extends Value {
	private long value;
	
	public Long() { }
	public Long(long value) { this.value = value; }
	
	@Override
	public void read(RandomAccessible stream) throws IOException {
		value = stream.readLong();
	}
	
	public long getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "long" + value;
	}
	
	@Override
	public int getByteLength() {
		return 8;
	}
}
