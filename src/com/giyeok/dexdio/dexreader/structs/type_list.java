package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Short;
import com.giyeok.dexdio.dexreader.value.Value;

public class type_list extends Value {
	private Int size;
	private Array list;

	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		size = new Int();
		
		size.read(stream);
		if (size.getValue() >= 0) {
			list = new Array(Short.class, size.getValue());
			
			list.read(stream);
		}
	}
	
	public int size() {
		return size.getValue();
	}
	
	public int item(int i) {
		return ((Short) list.item(i)).getUnsignedValue();
	}
	
	public int[] asIntArray() {
		int size = size();
		int arr[] = new int[size];
		
		for (int j = 0; j < size; j++) {
			arr[j] = item(j);
		}
		return arr;
	}

	@Override
	public int getByteLength() {
		return size.getByteLength() + list.getByteLength();
	}
}
