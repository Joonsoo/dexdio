package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Byte;
import com.giyeok.dexdio.dexreader.value.Container;



public class annotation_item extends Container {
	public static final byte VISIBILITY_BUILD = 0x00;
	public static final byte VISIBILITY_RUNTIME = 0x01;
	public static final byte VISIBILITY_SYSTEM = 0x02;

	public annotation_item() {
		super(new NamedValue[] {
				new NamedValue("visibility", new Byte()),
				new NamedValue("annotation", new encoded_annotation())
		});
	}
	
	public short visibility() {
		return ((Byte) find("visibility")).getUnsignedValue();
	}
	
	public encoded_annotation annotation() {
		return (encoded_annotation) find("annotation");
	}
}
