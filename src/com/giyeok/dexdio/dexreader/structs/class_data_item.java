package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.ULeb128;
import com.giyeok.dexdio.dexreader.value.Value;

public class class_data_item extends Value {
	private ULeb128 static_fields_size;
	private ULeb128 instance_fields_size;
	private ULeb128 direct_methods_size;
	private ULeb128 virtual_methods_size;
	private Array static_fields;
	private Array instance_fields;
	private Array direct_methods;
	private Array virtual_methods;
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		static_fields_size = new ULeb128();
		instance_fields_size = new ULeb128();
		direct_methods_size = new ULeb128();
		virtual_methods_size = new ULeb128();
		
		static_fields_size.read(stream);
		instance_fields_size.read(stream);
		direct_methods_size.read(stream);
		virtual_methods_size.read(stream);

		if (static_fields_size.getValue() > Integer.MAX_VALUE || instance_fields_size.getValue() > Integer.MAX_VALUE ||
				direct_methods_size.getValue() > Integer.MAX_VALUE || virtual_methods_size.getValue() > Integer.MAX_VALUE) {
			return;
		}
		
		static_fields = new Array(encoded_field.class, (int) static_fields_size.getValue());
		instance_fields = new Array(encoded_field.class, (int) instance_fields_size.getValue());
		direct_methods = new Array(encoded_method.class, (int) direct_methods_size.getValue());
		virtual_methods = new Array(encoded_method.class, (int) virtual_methods_size.getValue());
		
		static_fields.read(stream);
		instance_fields.read(stream);
		direct_methods.read(stream);
		virtual_methods.read(stream);
	}
	
	private encoded_field static_fields_cache[] = null;
	private encoded_field instance_fields_cache[] = null;
	
	public int static_fields_size() {
		return (int) static_fields_size.getValue();
	}

	public encoded_field[] static_fields() {
		if (static_fields_cache == null) {
			static_fields_cache = static_fields.asArray(new encoded_field[0]);
		}
		return static_fields_cache;
	}

	public int instance_fields_size() {
		return (int) instance_fields_size.getValue();
	}
	
	public encoded_field[] instance_fields() {
		if (instance_fields_cache == null) {
			instance_fields_cache = instance_fields.asArray(new encoded_field[0]);
		}
		return instance_fields_cache;
	}

	private encoded_method direct_methods_cache[] = null;
	private encoded_method virtual_methods_cache[] = null;
	
	public int direct_methods_size() {
		return (int) direct_methods_size.getValue();
	}
	
	public encoded_method[] direct_methods() {
		if (direct_methods_cache == null) {
			direct_methods_cache = direct_methods.asArray(new encoded_method[0]);
		}
		return direct_methods_cache;
	}

	public int virtual_methods_size() {
		return (int) virtual_methods_size.getValue();
	}
	
	public encoded_method[] virtual_methods() {
		if (virtual_methods_cache == null) {
			virtual_methods_cache = virtual_methods.asArray(new encoded_method[0]);
		}
		return virtual_methods_cache;
	}

	@Override
	public int getByteLength() {
		return static_fields_size.getByteLength() + instance_fields_size.getByteLength() +
				direct_methods_size.getByteLength() + virtual_methods_size.getByteLength() +
				static_fields.getByteLength() + instance_fields.getByteLength() +
				direct_methods.getByteLength() + virtual_methods.getByteLength();
	}
}
