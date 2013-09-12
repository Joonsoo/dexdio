package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

public class Double extends Value {
	private double value;
	
	public Double() { }
	public Double(double value) { this.value = value; }

	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		value = stream.readDouble();
	}
	
	public double getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "double" + value;
	}
	
	@Override
	public int getByteLength() {
		return 8;
	}
}
