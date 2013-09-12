package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

public class Byte extends Value {
	private byte value;
	
	public Byte() { }
	public Byte(byte value) { this.value = value; }
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		value = stream.readByte();
	}
	
	public byte getValue() {
		return value;
	}
	
	public short getUnsignedValue() {
		return (short)(value & 0xff);
	}
	
	@Override
	public String toString() {
		return "byte" + value;
	}
	
	@Override
	public int getByteLength() {
		return 1;
	}
}
