package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.RandomAccessible;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Short;
import com.giyeok.dexdio.dexreader.value.Value;

import java.io.IOException;

public class code_item extends Value {
	private Short registers_size;
	private Short ins_size;
	private Short outs_size;
	private Short tries_size;
	private Int debug_info_off;
	private Int insns_size;
	private Array insns;
	private Array tries;
	private encoded_catch_handler_list handlers;

	@Override
	public void read(RandomAccessible stream) throws IOException {
		registers_size = new Short();
		ins_size = new Short();
		outs_size = new Short();
		tries_size = new Short();
		debug_info_off = new Int();
		insns_size = new Int();
		
		registers_size.read(stream);
		ins_size.read(stream);
		outs_size.read(stream);
		tries_size.read(stream);
		debug_info_off.read(stream);
		insns_size.read(stream);
		
		insns = new Array(Short.class, insns_size.getValue());
		insns.read(stream);
		if (insns_size.getValue() != 0 && (insns_size.getValue() % 2 != 0)) {
			new Short().read(stream);		// padding
		}
		if (tries_size.getValue() == 0) {
			tries = null;
			handlers = null;
		} else {
			tries = new Array(try_item.class, tries_size.getValue());
			tries.read(stream);
			handlers = new encoded_catch_handler_list();
			handlers.read(stream);
		}
	}
	
	public int registers_size() {
		return registers_size.getUnsignedValue();
	}

	public int ins_size() {
		return ins_size.getUnsignedValue();
	}

	public int outs_size() {
		return outs_size.getUnsignedValue();
	}
	
	public int tries_size() {
		return tries_size.getUnsignedValue();
	}
	
	public long debug_info_off() {
		return debug_info_off.getUnsignedValue();
	}
	
	public int insns_size() {
		return insns_size.getValue();
	}
	
	public int[] insns() {
		int length = this.insns.length();
		int insns[] = new int[length];
		
		for (int i = 0; i < length; i++) {
			insns[i] = ((Short) this.insns.item(i)).getUnsignedValue();
		}
		return insns;
	}
	
	public try_item[] tries() {
		return tries.asArray(new try_item[0]);
	}
	
	public encoded_catch_handler_list handlers() {
		return handlers;
	}

	@Override
	public int getByteLength() {
		return registers_size.getByteLength() + ins_size.getByteLength() +
				outs_size.getByteLength() + tries_size.getByteLength() +
				debug_info_off.getByteLength() + insns_size.getByteLength() +
				insns.getByteLength() + tries.getByteLength() + handlers.getByteLength();
	}
}
