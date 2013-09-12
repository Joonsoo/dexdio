package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.ULeb128;

public class annotation_element extends Container {

	public annotation_element() {
		super(new NamedValue[] {
				new NamedValue("name_idx", new ULeb128()),
				new NamedValue("value", new encoded_value())
		});
	}
	
	public int name_idx() {
		return (int) ((ULeb128) find("name_idx")).getValue();
	}
	
	public encoded_value value() {
		return (encoded_value) find("value");
	}
}
