package com.giyeok.dexdio.dexreader.value;


public class ByteArray extends Array {

	public ByteArray(int length) {
		super(Byte.class, length);
	}

	public byte getByte(int i) {
		return ((Byte) super.item(i)).getValue();
	}
	
	public short getUnsigned(int i) {
		return ((Byte) super.item(i)).getUnsignedValue();
	}
	
	public byte[] asArray() {
		int length = length();
		byte b[] = new byte[length];
		
		for (int i = 0; i < length; i++) {
			b[i] = getByte(i);
		}
		return b;
	}
}
