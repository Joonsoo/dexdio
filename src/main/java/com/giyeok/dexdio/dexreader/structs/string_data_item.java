package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.ULeb128;
import com.giyeok.dexdio.dexreader.value.Value;

public class string_data_item extends Value {
	private ULeb128 utf16_size;
	private String value;
	private int bytelength;
	
	public string_data_item() {
		utf16_size = new ULeb128();
	}

	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		int length;
		
		utf16_size.read(stream);
		length = (int) utf16_size.getValue();
		if (length >= 0) {
			byte b[] = new byte[length * 3];
			bytelength = 0;
			while (bytelength < b.length && stream.has()) {
				b[bytelength++] = stream.readByte();
				if (new String(b, 0, bytelength, "UTF-8").length() > length) {
					bytelength--;
					break;
				}
			}
			value = new String(b, 0, bytelength, "UTF-8");
		}
	}
	
	public int getLength() {
		return (int) utf16_size.getValue();
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public int getByteLength() {
		return utf16_size.getByteLength() + bytelength;
	}
}
