package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.RandomAccessible;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.ULeb128;
import com.giyeok.dexdio.dexreader.value.Value;

import java.io.IOException;

public class encoded_annotation extends Value {
	private ULeb128 type_idx;
	private ULeb128 size;
	private Array elements;

	@Override
	public void read(RandomAccessible stream) throws IOException {
		type_idx = new ULeb128();
		size = new ULeb128();
		
		type_idx.read(stream);
		size.read(stream);
		
		elements = new Array(annotation_element.class, (int) size.getValue());
		elements.read(stream);
	}
	
	public int type_idx() {
		return (int) type_idx.getValue();
	}
	
	public int size() {
		return (int) size.getValue();
	}
	
	public annotation_element[] elements() {
		return elements.asArray(new annotation_element[0]);
	}

	@Override
	public int getByteLength() {
		return type_idx.getByteLength() + size.getByteLength() + elements.getByteLength();
	}
}
