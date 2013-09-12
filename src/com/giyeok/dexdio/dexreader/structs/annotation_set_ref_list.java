package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Value;

public class annotation_set_ref_list extends Value {
	private Int size;
	private Array list;
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		size = new Int();
		size.read(stream);
		
		int length = size.getValue();
		if (length < 0) {
			throw new IOException("Too many items in annotation_set_ref_list");
		}
		
		list = new Array(annotation_set_ref_item.class, length);
		list.read(stream);
	}
	
	public int size() {
		return size.getValue();
	}
	
	public annotation_set_ref_item[] list() {
		return list.asArray(new annotation_set_ref_item[0]);
	}

	public class annotation_set_ref_item extends Container {
	
		public annotation_set_ref_item() {
			super(new NamedValue[] {
					new NamedValue("annotations_off", new Int())
			});
		}
		
		public long annotations_off() {
			return ((Int) find("annotations_off")).getUnsignedValue();
		}
	}

	@Override
	public int getByteLength() {
		return size.getByteLength() + list.getByteLength();
	}
}

