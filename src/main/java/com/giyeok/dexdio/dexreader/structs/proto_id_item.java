package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;



public class proto_id_item extends Container {

	public proto_id_item() {
		super(new NamedValue[] {
				new NamedValue("shorty_idx", new Int()),
				new NamedValue("return_type_idx", new Int()),
				new NamedValue("parameters_off", new Int())
		});
	}
	
	public long shorty_idx() {
		return ((Int) find("shorty_idx")).getUnsignedValue();
	}
	
	public long return_type_idx() {
		return ((Int) find("return_type_idx")).getUnsignedValue();
	}

	public long parameters_off() {
		return ((Int) find("parameters_off")).getUnsignedValue();
	}
}
