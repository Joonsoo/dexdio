package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;

public class annotation_off_item extends Container {

	public annotation_off_item() {
		super(new NamedValue[] {
				new NamedValue("annotation_off", new Int())
		});
	}
	
	public long annotation_off() {
		return ((Int) find("annotation_off")).getUnsignedValue();
	}
}
