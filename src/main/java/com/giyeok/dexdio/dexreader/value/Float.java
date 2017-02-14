package com.giyeok.dexdio.dexreader.value;

import com.giyeok.dexdio.dexreader.RandomAccessible;

import java.io.IOException;

public class Float extends Value {
	private float value;
	
	public Float() { }
	public Float(float value) { this.value = value; }

	@Override
	public void read(RandomAccessible stream) throws IOException {
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
