package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Short;



public class method_id_item extends Container {

	public method_id_item() {
		super(new NamedValue[] {
				new NamedValue("class_idx", new Short()),
				new NamedValue("proto_idx", new Short()),
				new NamedValue("name_idx", new Int())
		});
	}
	
	public int class_idx() {
		return ((Short) find("class_idx")).getUnsignedValue();
	}
	
	public int proto_idx() {
		return ((Short) find("proto_idx")).getUnsignedValue();
	}
	
	public long name_idx() {
		return ((Int) find("name_idx")).getUnsignedValue();
	}
}
