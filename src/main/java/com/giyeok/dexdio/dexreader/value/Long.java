package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

public class Long extends Value {
	private long value;
	
	public Long() { }
	public Long(long value) { this.value = value; }
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
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
