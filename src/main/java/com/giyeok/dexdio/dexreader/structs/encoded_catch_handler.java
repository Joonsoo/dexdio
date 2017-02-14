package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.RandomAccessible;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.SLeb128;
import com.giyeok.dexdio.dexreader.value.ULeb128;
import com.giyeok.dexdio.dexreader.value.Value;

import java.io.IOException;

public class encoded_catch_handler extends Value {
	private SLeb128 size;
	private Array handlers;
	private ULeb128 catch_all_addr;

	@Override
	public void read(RandomAccessible stream) throws IOException {
		size = new SLeb128();
		size.read(stream);
		
		handlers = new Array(encoded_type_addr_pair.class, Math.abs(size.getValue()));
		handlers.read(stream);
		
		if (size.getValue() > 0) {
			catch_all_addr = null;
		} else {
			catch_all_addr = new ULeb128();
			catch_all_addr.read(stream);
		}
	}
	
	public int size() {
		return size.getValue();
	}
	
	public encoded_type_addr_pair[] handlers() {
		return handlers.asArray(new encoded_type_addr_pair[0]);
	}
	
	public int catch_all_addr() {
		return (int) catch_all_addr.getValue();
	}

	@Override
	public int getByteLength() {
		if (catch_all_addr == null) {
			return size.getByteLength() + handlers.getByteLength();
		} else {
			return size.getByteLength() + handlers.getByteLength() + catch_all_addr.getByteLength();
		}
	}
}
