package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.ULeb128;



public class encoded_method extends Container {
	private code_item code_item;
	
	public encoded_method() {
		super(new NamedValue[] {
				new NamedValue("method_idx_diff", new ULeb128()),
				new NamedValue("access_flags", new ULeb128()),
				new NamedValue("code_off", new ULeb128())
		});
		code_item = null;
	}
	
	public int method_idx_diff() {
		return (int) ((ULeb128) find("method_idx_diff")).getValue();
	}
	
	public int access_flags() {
		return (int) ((ULeb128) find("access_flags")).getValue();
	}
	
	public long code_off() {
		return ((ULeb128) find("code_off")).getValue();
	}
	
	public void code_item(code_item code_item) {
		this.code_item = code_item;
	}
	
	public code_item code_item() {
		return code_item;
	}
}
