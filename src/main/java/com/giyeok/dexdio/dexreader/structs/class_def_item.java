package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;



public class class_def_item extends Container {
	
	public class_def_item() {
		super(new NamedValue[] {
				new NamedValue("class_idx", new Int()),
				new NamedValue("access_flags", new Int()),
				new NamedValue("superclass_idx", new Int()),
				new NamedValue("interfaces_off", new Int()),
				new NamedValue("source_file_idx", new Int()),
				new NamedValue("annotations_off", new Int()),
				new NamedValue("class_data_off", new Int()),
				new NamedValue("static_values_off", new Int()),
		});
	}
	
	public long class_idx() {
		return ((Int) find("class_idx")).getUnsignedValue();
	}
	
	public long access_flags() {
		return ((Int) find("access_flags")).getUnsignedValue();
	}
	
	public long superclass_idx() {
		return ((Int) find("superclass_idx")).getUnsignedValue();
	}
	
	public long interfaces_off() {
		return ((Int) find("interfaces_off")).getUnsignedValue();
	}
	
	public long source_file_idx() {
		return ((Int) find("source_file_idx")).getUnsignedValue();
	}
	
	public long annotations_off() {
		return ((Int) find("annotations_off")).getUnsignedValue();
	}
	
	public long class_data_off() {
		return ((Int) find("class_data_off")).getUnsignedValue();
	}
	
	public long static_values_off() {
		return ((Int) find("static_values_off")).getUnsignedValue();
	}
}
