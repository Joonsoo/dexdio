package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Value;

public class annotation_set_item extends Value {
	private Int size;
	private Array entries;

	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		size = new Int();
		size.read(stream);
		
		entries = new Array(annotation_off_item.class, size.getValue());
		entries.read(stream);
	}
	
	public int size() {
		return size.getValue();
	}
	
	public annotation_off_item[] entries() {
		return entries.asArray(new annotation_off_item[0]);
	}

	@Override
	public int getByteLength() {
		return size.getByteLength() + entries.getByteLength();
	}
}
