package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

public class Container extends Value {
	public static class NamedValue {
		private String name;
		private Value field;
		
		public NamedValue(String name, Value field) {
			this.name = name;
			this.field = field;
		}
	}
	
	private NamedValue[] fields;
	
	public Container(NamedValue[] fields) {
		this.fields = fields;
	}

	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		for (int i = 0; i < fields.length; i++) {
			fields[i].field.read(stream);
		}
	}
	
	protected Value find(String name) {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].name.equals(name)) {
				return fields[i].field;
			}
		}
		return null;
	}

	@Override
	public int getByteLength() {
		int bytelength = 0;
		
		for (NamedValue value: fields) {
			bytelength += value.field.getByteLength();
		}
		return bytelength;
	}
}
