package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;



public class string_id_item extends Container {

	public string_id_item() {
		super(new NamedValue[] {
				new NamedValue("string_data_off", new Int())
		});
	}
	
	public long string_data_off() {
		return ((Int) find("string_data_off")).getUnsignedValue();
	}
}

