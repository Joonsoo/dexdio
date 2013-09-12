package com.giyeok.dexdio.dexreader.structs;

import com.giyeok.dexdio.dexreader.value.ByteArray;
import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;


public class header_item extends Container {

	public header_item() {
		super(new NamedValue[] {
				new NamedValue("magic", new ByteArray(8)),
				new NamedValue("checksum", new Int()),
				new NamedValue("signature", new ByteArray(20)),
				new NamedValue("file_size", new Int()),
				new NamedValue("header_size", new Int()),
				new NamedValue("endian_tag", new Int()),
				new NamedValue("link_size", new Int()),
				new NamedValue("link_off", new Int()),
				new NamedValue("map_off", new Int()),
				new NamedValue("string_ids_size", new Int()),
				new NamedValue("string_ids_off", new Int()),
				new NamedValue("type_ids_size", new Int()),
				new NamedValue("type_ids_off", new Int()),
				new NamedValue("proto_ids_size", new Int()),
				new NamedValue("proto_ids_off", new Int()),
				new NamedValue("field_ids_size", new Int()),
				new NamedValue("field_ids_off", new Int()),
				new NamedValue("method_ids_size", new Int()),
				new NamedValue("method_ids_off", new Int()),
				new NamedValue("class_defs_size", new Int()),
				new NamedValue("class_defs_off", new Int()),
				new NamedValue("data_size", new Int()),
				new NamedValue("data_off", new Int())
		});
	}
	
	public byte[] magic() {
		return ((ByteArray) find("magic")).asArray();
	}
	
	public long checksum() {
		return ((Int) find("checksum")).getUnsignedValue();
	}
	
	public byte[] signature() {
		return ((ByteArray) find("signature")).asArray();
	}
	
	public long file_size() {
		return ((Int) find("file_size")).getUnsignedValue();
	}
	
	public long header_size() {
		return ((Int) find("header_size")).getUnsignedValue();
	}
	
	public long endian_tag() {
		return ((Int) find("endian_tag")).getUnsignedValue();
	}
	
	public long link_size() {
		return ((Int) find("link_size")).getUnsignedValue();
	}
	
	public long link_off() {
		return ((Int) find("link_off")).getUnsignedValue();
	}
	
	public long map_off() {
		return ((Int) find("map_off")).getUnsignedValue();
	}
	
	public long string_ids_size() {
		return ((Int) find("string_ids_size")).getUnsignedValue();
	}
	
	public long string_ids_off() {
		return ((Int) find("string_ids_off")).getUnsignedValue();
	}
	
	public long type_ids_size() {
		return ((Int) find("type_ids_size")).getUnsignedValue();
	}
	
	public long type_ids_off() {
		return ((Int) find("type_ids_off")).getUnsignedValue();
	}
	
	public long proto_ids_size() {
		return ((Int) find("proto_ids_size")).getUnsignedValue();
	}
	
	public long proto_ids_off() {
		return ((Int) find("proto_ids_off")).getUnsignedValue();
	}
	
	public long field_ids_size() {
		return ((Int) find("field_ids_size")).getUnsignedValue();
	}
	
	public long field_ids_off() {
		return ((Int) find("field_ids_off")).getUnsignedValue();
	}
	
	public long method_ids_size() {
		return ((Int) find("method_ids_size")).getUnsignedValue();
	}
	
	public long method_ids_off() {
		return ((Int) find("method_ids_off")).getUnsignedValue();
	}
	
	public long class_defs_size() {
		return ((Int) find("class_defs_size")).getUnsignedValue();
	}
	
	public long class_defs_off() {
		return ((Int) find("class_defs_off")).getUnsignedValue();
	}
	
	public long data_size() {
		return ((Int) find("data_size")).getUnsignedValue();
	}
	
	public long data_off() {
		return ((Int) find("data_off")).getUnsignedValue();
	}
}
