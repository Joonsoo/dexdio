package com.giyeok.dexdio.dexreader;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.structs.field_id_item;
import com.giyeok.dexdio.dexreader.structs.header_item;
import com.giyeok.dexdio.dexreader.value.Array;

public class FieldTable {
	private StringTable stringTable;
	private TypeTable typeTable;
	
	public FieldTable(StringTable stringTable, TypeTable typeTable) {
		this.stringTable = stringTable;
		this.typeTable = typeTable;
	}
	
	public class Field {
		private int class_idx;
		private int type_idx;
		private int name_idx;
		
		public Field(int class_idx, int type_idx, int name_idx) {
			this.class_idx = class_idx;
			this.type_idx = type_idx;
			this.name_idx = name_idx;
		}
		
		public int getClassIdx() {
			return class_idx;
		}
		
		public String getClassTypeName() {
			return typeTable.getTypeName(class_idx);
		}
		
		public int getTypeIdx() {
			return type_idx;
		}
		
		public String getTypeName() {
			return typeTable.getTypeName(type_idx);
		}
		
		public int getNameIdx() {
			return name_idx;
		}
		
		public String getName() {
			return stringTable.get(name_idx);
		}
	}
	
	private Field[] fieldTable;
	
	public int size() {
		return fieldTable.length;
	}
	
	public Field get(int i) {
		return fieldTable[i];
	}

	boolean loadFields(header_item header, EndianRandomAccessFile file) throws IOException {
		int field_ids_size = (int) header.field_ids_size();
		long field_ids_off = header.field_ids_off();
		
		System.out.println(field_ids_size + " " + field_ids_off);
		file.seek(field_ids_off);
		
		Array field_ids = new Array(field_id_item.class, field_ids_size);
		fieldTable = new Field[field_ids_size];
		
		field_ids.read(file);
		
		for (int i = 0; i < field_ids_size; i++) {
			field_id_item field_id_item = (field_id_item) field_ids.item(i);
			fieldTable[i] = new Field(field_id_item.class_idx(),
										field_id_item.type_idx(),
										(int) field_id_item.name_idx());
			System.out.print(i + "-");
			System.out.println(fieldTable[i].getClassTypeName() + "." + fieldTable[i].getName() + " : " + fieldTable[i].getTypeName());
		}
		return true;
	}
	
}
