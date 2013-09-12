package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexPrimitiveType;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;

public class OperandIntegerConstant extends Operand {
	private long value;
	
	/**
	 * type should one of 'b', 'h', 'H'(64-bit 'h'), 'i', 'l', 'n', 's'
	 * @param value
	 * @param type
	 */
	public OperandIntegerConstant(DexCodeItem codeitem, long value, char type) {
		super(codeitem);
		
		assert type == 'b' || type == 'h' || type == 'H' || type == 'i' || type == 'l' || type == 'n' || type == 's';
		
		switch (type) {
		case 'b':
			this.value = getRealValue(value, 8);
			break;
		case 'h':
			this.value = getRealValue(value, 16) << 16;
			break;
		case 'H':
			this.value = getRealValue(value, 16) << 48;
			break;
		case 'i':
			this.value = getRealValue(value, 32);
			break;
		case 'l':
			this.value = getRealValue(value, 64);
			break;
		case 'n':
			this.value = getRealValue(value, 4);
			break;
		case 's':
			this.value = getRealValue(value, 16);
			break;
		default:
			assert false;
		}
	}

	private long getRealValue(long value, int bits) {
		assert bits == 4 || bits == 8 || bits == 16 || bits == 32 || bits == 64;
		
		switch (bits) {
		case 4:
			return ((long) (((byte) (value)) << 4)) >> 4;
		case 8:
			return (byte) value;
		case 16:
			return (short) value;
		case 32:
			return (int) value;
		case 64:
			return value;
		default:
			assert false;
			return 0;
		}
	}

	@Override
	public String getStringRepresentation() {
		return "#+" + Long.toHexString(value);
	}
	
	public String getStringRepresentation(DexType type) {
		if (type == null) {
			return getStringRepresentation();
		} else {
			DexProgram program = getBelongedCodeItem().getProgram();
			
			if (type instanceof DexPrimitiveType) {
				if (type == program.getBooleanPrimitiveType()) {
					return (value == 0)? "false":"true";
				} else if (type == program.getBytePrimitiveType()) {
					return Long.toString(getRealValue(value, 8));
				} else if (type == program.getShortPrimitiveType()) {
					return Long.toString(getRealValue(value, 16));
				} else if (type == program.getIntegerPrimitiveType()) {
					return Long.toString(getRealValue(value, 32));
				} else if (type == program.getLongPrimitiveType()) {
					return Long.toString(getRealValue(value, 64)) + "l";
				} else if (type == program.getFloatPrimitiveType()) {
					// TODO verify this
					return Float.toString(Float.intBitsToFloat((int) value)) + "f";
				} else if (type == program.getDoublePrimitiveType()) {
					return Double.toString(Double.longBitsToDouble(value));
				}
			} else if (value == 0) {
				return "null";
			}
			return getStringRepresentation();
		}
	}
}
