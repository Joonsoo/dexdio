package com.giyeok.dexdio.dexreader;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.structs.header_item;
import com.giyeok.dexdio.dexreader.value.IntArray;

public class TypeTable {
	private StringTable stringTable;
	private int[] typeTable;
	
	public TypeTable(StringTable stringTable) {
		this.stringTable = stringTable;
	}
	
	boolean loadTypes(header_item header, RandomAccessible file) throws IOException {
		int type_ids_size = (int) header.type_ids_size();
		long type_ids_off = header.type_ids_off();
		
		file.seek(type_ids_off);
		
		IntArray string_ids = new IntArray(type_ids_size);
		typeTable = new int[type_ids_size];
		
		string_ids.read(file);
		
		for (int i = 0; i < type_ids_size; i++) {
			typeTable[i] = string_ids.getInt(i);
			if (typeTable[i] < 0 || typeTable[i] >= stringTable.size()) {
				return false;
			}
			// System.out.println(stringTable.get(typeTable[i]));
		}
		return true;
	}
	
	public int size() {
		return typeTable.length;
	}
	
	public int get(int i) {
		return typeTable[i];
	}
	
	public String getTypeName(int i) {
		return stringTable.get(typeTable[i]);
	}
}
