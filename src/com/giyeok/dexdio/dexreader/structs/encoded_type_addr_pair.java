package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.ULeb128;

public class encoded_type_addr_pair extends Container {

	public encoded_type_addr_pair() {
		super(new NamedValue[] {
				new NamedValue("type_idx", new ULeb128()),
				new NamedValue("addr", new ULeb128())
		});
	}
	
	public int type_idx() {
		return (int) ((ULeb128) find("type_idx")).getValue();
	}
	
	public int addr() {
		return (int) ((ULeb128) find("addr")).getValue();
	}
}
