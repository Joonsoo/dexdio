package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.RandomAccessible;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.ULeb128;
import com.giyeok.dexdio.dexreader.value.Value;

import java.io.IOException;

public class encoded_array extends Value {
	private ULeb128 size;
	private Array values;
	
	@Override
	public void read(RandomAccessible stream) throws IOException {
		size = new ULeb128();
		size.read(stream);
		
		values = new Array(encoded_value.class, (int) size.getValue());
		values.read(stream);
	}
	
	public int size() {
		return (int) size.getValue();
	}
	
	public encoded_value[] values() {
		return values.asArray(new encoded_value[0]);
	}

	@Override
	public int getByteLength() {
		return size.getByteLength() + values.getByteLength();
	}
}
