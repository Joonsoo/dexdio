package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Short;

public class try_item extends Container {

	public try_item() {
		super(new NamedValue[] {
				new NamedValue("start_addr", new Int()),
				new NamedValue("insn_count", new Short()),
				new NamedValue("handler_off", new Short())
		});
	}
	
	public int start_addr() {
		return ((Int) find("start_addr")).getValue();
	}
	
	public int insn_count() {
		return ((Short) find("insn_count")).getUnsignedValue();
	}
	
	public int handler_off() {
		return ((Short) find("handler_off")).getUnsignedValue();
	}
}
