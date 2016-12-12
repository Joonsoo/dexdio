package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstStaticOp extends DexInstruction {

	public DexInstStaticOp(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 2;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandConstantPool;

		((OperandConstantPool) getOperand(1)).setConstantKind(ConstantPoolKind.FIELD);
	}
	
	public DexField getField() {
		return getProgram().getFieldByFieldId(((OperandConstantPool) getOperand(1)).getValue());
	}
	
	public OperandRegister getSourceOrDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getSourceOrDestinationRegister() {
		return getSourceOrDestination().getRegister();
	}
	
	public boolean isGetOperation() {
		switch (getOpcode()) {
		case 0x60: case 0x61: case 0x62: case 0x63:
		case 0x64: case 0x65: case 0x66:
			return true;
		}
		return false;
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x61: case 0x68:
			return true;
		}
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x60, "sget",
			0x61, "sget-wide",
			0x62, "sget-object",
			0x63, "sget-boolean",
			0x64, "sget-byte",
			0x65, "sget-char",
			0x66, "sget-short",
			0x67, "sput",
			0x68, "sput-wide",
			0x69, "sput-object",
			0x6a, "sput-boolean",
			0x6b, "sput-byte",
			0x6c, "sput-char",
			0x6d, "sput-short"
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

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.STATIC_OP;
	}
}
