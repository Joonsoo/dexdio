package com.giyeok.dexdio.dexreader.value;


public class IntArray extends Array {

	public IntArray(int length) {
		super(Int.class, length);
	}

	public int getInt(int i) {
		return ((Int) super.item(i)).getValue();
	}
	
	public long getUnsigned(int i) {
		return ((Int) super.item(i)).getUnsignedValue();
	}

	public int[] asArray() {
		int length = length();
		int b[] = new int[length];
		
		for (int i = 0; i < length; i++) {
			b[i] = getInt(i);
		}
		return b;
	}
}
