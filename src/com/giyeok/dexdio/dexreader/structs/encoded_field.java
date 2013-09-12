package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.ULeb128;

public class encoded_field extends Container {

	public encoded_field() {
		super(new NamedValue[] {
				new NamedValue("field_idx_diff", new ULeb128()),
				new NamedValue("access_flags", new ULeb128())
		});
	}
	
	public int field_idx_diff() {
		return (int) ((ULeb128) find("field_idx_diff")).getValue();
	}
	
	public int access_flags() {
		return (int) ((ULeb128) find("access_flags")).getValue();
	}
}
