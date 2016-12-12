package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.ULeb128;
import com.giyeok.dexdio.dexreader.value.Value;

public class encoded_array extends Value {
	private ULeb128 size;
	private Array values;
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
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
