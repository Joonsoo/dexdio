package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;
import java.util.Arrays;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.RandomAccessible;

public class Array extends Value {
	private int length;
	private Value[] array;
	private Class<? extends Value> cls;

	public Array(Class<? extends Value> cls, int length) {
		this.cls = cls;
		this.length = length;
		this.array = (Value[]) java.lang.reflect.Array.newInstance(cls, length);
	}

	@Override
	public void read(RandomAccessible stream) throws IOException {
		try {
			for (int i = 0; i < length; i++) {
				array[i] = cls.newInstance();
				array[i].read(stream);
			}
		} catch (InstantiationException e) {
			assert false;
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			assert false;
			e.printStackTrace();
		}
	}
	
	public int length() {
		return length;
	}
	
	public Value item(int i) {
		if (i < 0 || i >= length) {
			return null;
		}
		return array[i];
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Value> T[] asArray(T[] array) {
		array = Arrays.copyOf(array, length);
		for (int i = 0; i < length; i++) {
			array[i] = (T) this.array[i];
		}
		return array;
	}

	@Override
	public int getByteLength() {
		int bytelength = 0;
		for (int i = 0; i < length; i++) {
			bytelength += array[i].getByteLength();
		}
		return bytelength;
	}
}
