package com.giyeok.dexdio.dexreader;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.structs.header_item;
import com.giyeok.dexdio.dexreader.structs.string_data_item;
import com.giyeok.dexdio.dexreader.structs.string_id_item;
import com.giyeok.dexdio.dexreader.value.Array;

public class StringTable {
	private int[] stringLengths;
	private String[] stringTable;
	
	boolean loadStrings(header_item header, EndianRandomAccessFile file) throws IOException {
		long string_ids_size = header.string_ids_size();
		long string_ids_off = header.string_ids_off();
		
		if (string_ids_size >= Integer.MAX_VALUE) {
			System.out.println("Too many strings");
			return false;
		}
		
		file.seek(string_ids_off);
		
		Array string_ids = new Array(string_id_item.class, (int) string_ids_size);
		stringLengths = new int[(int) string_ids_size];
		stringTable = new String[(int) string_ids_size];
		
		string_ids.read(file);
		
		string_data_item string_datum = new string_data_item();
		for (int i = 0, length = string_ids.length(); i < length; i++) {
			file.seek(((string_id_item) string_ids.item(i)).string_data_off());
			string_datum.read(file);
			if (string_datum.getLength() < 0) {
				System.out.println("There exists too long string");
				return false;
			}
			stringLengths[i] = string_datum.getLength();
			stringTable[i] = string_datum.getValue();
			// System.out.println(stringTable[i]);
		}
		return true;
	}

	/**
	 * 전체 문자열의 갯수를 반환한다
	 * @return
	 */
	public int size() {
		return stringTable.length;
	}
	
	/**
	 * i번 문자열의 지정된 길이를 반환한다
	 * @param i
	 * @return
	 */
	public int getLengthOf(int i) {
		assert (stringLengths[i] == stringTable[i].length());
		return stringLengths[i];
	}
	
	/**
	 * i번 문자열을 반환한다
	 * @param i
	 * @return
	 */
	public String get(int i) {
		return stringTable[i];
	}
}
