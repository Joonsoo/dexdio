package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

public class Float extends Value {
	private float value;
	
	public Float() { }
	public Float(float value) { this.value = value; }

	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		value = stream.readFloat();
	}
	
	public float getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "float" + value;
	}
	
	@Override
	public int getByteLength() {
		return 4;
	}
}
