package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;

public class DexInstUnaryOp extends DexInstruction {

	public DexInstUnaryOp(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 2;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
	}
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}
	
	public OperandRegister getSource() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getSourceRegister() {
		return getSource().getRegister();
	}
	
	public boolean isSourceWide() {
		switch (getOpcode()) {
		case 0x7d: case 0x7e:					// *-long
		case 0x80:								// neg-double
		case 0x84: case 0x85: case 0x86:		// long-to-*
		case 0x8a: case 0x8b: case 0x8c:		// double-to-*
			return true;
		}
		return false;
	}
	
	public boolean isDestinationWide() {
		switch (getOpcode()) {
		case 0x7d: case 0x7e:					// *-long
		case 0x80:								// neg-double
		case 0x81: case 0x88: case 0x8b:		// *-to-long
		case 0x83: case 0x86: case 0x89:		// *-to-double
			return true;
		}
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x7b, "neg-int",
			0x7c, "not-int",
			0x7d, "neg-long",
			0x7e, "not-long",
			0x7f, "neg-float",
			0x80, "neg-double",
			0x81, "int-to-long",
			0x82, "int-to-float",
			0x83, "int-to-double",
			0x84, "long-to-int",
			0x85, "long-to-float",
			0x86, "long-to-double",
			0x87, "float-to-int",
			0x88, "float-to-long",
			0x89, "float-to-double",
			0x8a, "double-to-int",
			0x8b, "double-to-long",
			0x8c, "double-to-float",
			0x8d, "int-to-byte",
			0x8e, "int-to-char",
			0x8f, "int-to-short"
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		// not sure
		return true;
	}

	public DexType getDestinationType() {
		switch (getOpcode()) {
		case 0x7b: // neg-int
		case 0x7c: // not-int
			return getProgram().getIntegerPrimitiveType();
		case 0x7d: // neg-long
		case 0x7e: // not-long
			return getProgram().getLongPrimitiveType();
		case 0x7f: // neg-float
			return getProgram().getFloatPrimitiveType();
		case 0x80: // neg-double
			return getProgram().getDoublePrimitiveType();
		case 0x81: // int-to-long
		case 0x88: // float-to-long
		case 0x8b: // double-to-long
			return getProgram().getLongPrimitiveType();
		case 0x82: // int-to-float
		case 0x85: // long-to-float
		case 0x8c: // double-to-float
			return getProgram().getFloatPrimitiveType();
		case 0x83: // int-to-double
		case 0x86: // long-to-double
		case 0x89: // float-to-double
			return getProgram().getDoublePrimitiveType();
		case 0x84: // long-to-int
		case 0x87: // float-to-int
		case 0x8a: // double-to-int
			return getProgram().getIntegerPrimitiveType();
		case 0x8d: // int-to-byte
			return getProgram().getBytePrimitiveType();
		case 0x8e: // int-to-char
			return getProgram().getCharPrimitiveType();
		case 0x8f: // int-to-short
			return getProgram().getShortPrimitiveType();
		}
		assert false;
		return null;
	}
	
	public DexType getSourceType() {
		switch (getOpcode()) {
		case 0x7b: // neg-int
		case 0x7c: // not-int
			return getProgram().getIntegerPrimitiveType();
		case 0x7d: // neg-long
		case 0x7e: // not-long
			return getProgram().getLongPrimitiveType();
		case 0x7f: // neg-float
			return getProgram().getFloatPrimitiveType();
		case 0x80: // neg-double
			return getProgram().getDoublePrimitiveType();
		case 0x81: // int-to-long
		case 0x82: // int-to-float
		case 0x83: // int-to-double
			return getProgram().getIntegerPrimitiveType();
		case 0x84: // long-to-int
		case 0x85: // long-to-float
		case 0x86: // long-to-double
			return getProgram().getLongPrimitiveType();
		case 0x87: // float-to-int
		case 0x88: // float-to-long
		case 0x89: // float-to-double
			return getProgram().getFloatPrimitiveType();
		case 0x8a: // double-to-int
		case 0x8b: // double-to-long
		case 0x8c: // double-to-float
			return getProgram().getDoublePrimitiveType();
		case 0x8d: // int-to-byte
		case 0x8e: // int-to-char
		case 0x8f: // int-to-short
			return getProgram().getIntegerPrimitiveType();
		}
		assert false;
		return null;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.UNARY_OP;
	}
}
