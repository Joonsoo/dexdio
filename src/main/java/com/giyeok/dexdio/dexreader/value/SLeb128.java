package com.giyeok.dexdio.dexreader.value;

import com.giyeok.dexdio.dexreader.RandomAccessible;

import java.io.IOException;

//In a .dex file, LEB128 is only ever used to encode 32-bit quantities

public class SLeb128 extends Value {
	private int value;
	private int length;

	@Override
	public void read(RandomAccessible stream) throws IOException {
		int b;

		value = 0;
		do {
			b = stream.readUnsignedByte();
			value |= ((b & 0x7f) << (length * 7));
			length++;
		} while ((b & 0x80) != 0);
		
		// TODO needs validation
		if ((b & 0x40) != 0) {
			switch (length) {
			case 1:
				value = value | 0xffffff80;
				break;
			case 2:
				value = value | 0xffff8000;
				break;
			case 3:
				value = value | 0xff800000;
				break;
			case 4:
				value = value | 0x80000000;
				break;
			}
		}
	}

	public int getValue() {
		return value;
	}

	@Override
	public int getByteLength() {
		return length;
	}
	
	@Override
	public String toString() {
		return "sleb" + value;
	}
}
