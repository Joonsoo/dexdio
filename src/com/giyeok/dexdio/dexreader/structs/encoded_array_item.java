package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;


public class encoded_array_item extends Container {
	
	public encoded_array_item() {
		super(new NamedValue[] {
				new NamedValue("value", new encoded_array())
		});
	}
	
	public encoded_array value() {
		return (encoded_array) find("value");
	}
}
