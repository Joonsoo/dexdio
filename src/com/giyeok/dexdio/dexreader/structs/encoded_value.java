package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.Byte;
import com.giyeok.dexdio.dexreader.value.Double;
import com.giyeok.dexdio.dexreader.value.Float;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Long;
import com.giyeok.dexdio.dexreader.value.Short;
import com.giyeok.dexdio.dexreader.value.Value;

public class encoded_value extends Value {
	private Byte value_arg_with_value_type;
	private byte value_type;
	private byte value_arg;
	
	public static final byte VALUE_BYTE = 0x00;
	public static final byte VALUE_SHORT = 0x02;
	public static final byte VALUE_CHAR = 0x03;
	public static final byte VALUE_INT = 0x04;
	public static final byte VALUE_LONG = 0x06;
	public static final byte VALUE_FLOAT = 0x10;
	public static final byte VALUE_DOUBLE = 0x11;
	public static final byte VALUE_STRING = 0x17;
	public static final byte VALUE_TYPE = 0x18;
	public static final byte VALUE_FIELD = 0x19;
	public static final byte VALUE_METHOD = 0x1a;
	public static final byte VALUE_ENUM = 0x1b;
	public static final byte VALUE_ARRAY = 0x1c;
	public static final byte VALUE_ANNOTATION = 0x1d;
	public static final byte VALUE_NULL = 0x1e;
	public static final byte VALUE_BOOLEAN = 0x1f;
	
	private Value value;
	
	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		value_arg_with_value_type = new Byte();
		value_arg_with_value_type.read(stream);
		
		value_type = (byte) ((value_arg_with_value_type.getUnsignedValue()) & 0x1f);
		value_arg = (byte) (value_arg_with_value_type.getUnsignedValue() >> 5);
		
		switch (value_type) {
		case VALUE_BYTE:
			if (value_arg != 0) {
				throw new IOException("Wrong format: VALUE_BYTE must have value_arg of 0");
			}
			value = new Byte(stream.readByte());
			break;
			
		case VALUE_SHORT:
		case VALUE_CHAR:
			if (! (0 <= value_arg && value_arg <= 1)) {
				System.out.println(value_arg);
				throw new IOException("Wrong format: VALUE_SHORT/VALUE_CHAR must have value_arg of 0..1");
			}
			value = new Short((short) littleEndian(stream, value_arg + 1));
			break;
			
		case VALUE_INT:
		case VALUE_STRING:
		case VALUE_TYPE:
		case VALUE_FIELD:
		case VALUE_METHOD:
		case VALUE_ENUM:
			if (! (0 <= value_arg && value_arg <= 3)) {
				throw new IOException("Wrong format: VALUE_INT/STRING/TYPE/FIELD/METHOD/ENUM must have value_arg of 0..3");
			}
			value = new Int((int) littleEndian(stream, value_arg + 1));
			break;
			
		case VALUE_LONG:
			if (! (0 <= value_arg && value_arg <= 7)) {
				throw new IOException("Wrong format: VALUE_LONG must have value_arg of 0..7");
			}
			value = new Long(littleEndian(stream, value_arg + 1));
			break;
			
		case VALUE_FLOAT:
			if (! (0 <= value_arg && value_arg <= 3)) {
				throw new IOException("Wrong format: VALUE_FLOAT must have value_arg of 0..3");
			}
			// TODO verify longBItsToDouble works correctly
			value = new Float(java.lang.Float.intBitsToFloat((int) littleEndian(stream, value_arg + 1)));
			break;
			
		case VALUE_DOUBLE:
			if (! (0 <= value_arg && value_arg <= 7)) {
				throw new IOException("Wrong format: VALUE_DOUBLE must have value_arg of 0..7");
			}
			// TODO verify longBitsToDouble works correctly;
			value = new Double(java.lang.Double.longBitsToDouble(littleEndian(stream, value_arg + 1)));
			break;
		
		case VALUE_ARRAY:
			if (value_arg != 0) {
				throw new IOException("Wrong format: VALUE_ARRAY must have value_arg of 0");
			}
			value = new encoded_array();
			value.read(stream);
			break;
			
		case VALUE_ANNOTATION:
			if (value_arg != 0) {
				throw new IOException("Wrong format: VALUE_ANNOTATION must have value_arg of 0");
			}
			value = new encoded_annotation();
			value.read(stream);
			break;
		
		case VALUE_NULL:
			if (value_arg != 0) {
				throw new IOException("Wrong format: VALUE_NULL must have value_arg of 0");
			}
			value = null;
			break;
			
		case VALUE_BOOLEAN:
			if (! (0 <= value_arg && value_arg <= 1)) {
				throw new IOException("Wrong format: VALUE_NULL must have value_arg of 0");
			}
			value = new Byte(value_arg);
			break;
		}
		if (value == null) {
			System.out.println("null");
		} else {
			System.out.println(value.toString());
		}
	}
	
	private long littleEndian(EndianRandomAccessFile stream, int length) throws IOException {
		byte b[] = new byte[length];
		
		stream.readFully(b);
		return littleEndian(b);
	}
	
	private long littleEndian(byte b[]) {
		long l = 0;
		
		for (int i = 0; i < b.length; i++) {
			l |= (b[i] << (i * 8));
		}
		return l;
	}
	
	public byte getValueType() {
		return value_type;
	}
	
	public Value getValue() {
		return value;
	}

	@Override
	public int getByteLength() {
		return value_arg_with_value_type.getByteLength() + value.getByteLength();
	}
}
