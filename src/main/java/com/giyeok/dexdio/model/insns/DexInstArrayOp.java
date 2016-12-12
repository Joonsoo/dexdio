package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstArrayOp extends DexInstruction {

	public DexInstArrayOp(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 3;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
		assert getOperand(2) instanceof OperandRegister;
	}
	
	public boolean isGetOperation() {
		switch (getOpcode()) {
		case 0x44: case 0x45: case 0x46: case 0x47:
		case 0x48: case 0x49: case 0x4a:
			return true;
		}
		return false;
	}
	
	public OperandRegister getArrayOperand() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getArrayRegister() {
		return getArrayOperand().getRegister();
	}
	
	public OperandRegister getSourceOrDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getSourceOrDestinationRegister() {
		return getSourceOrDestination().getRegister();
	}
	
	public OperandRegister getIndexOperand() {
		return (OperandRegister) getOperand(2);
	}
	
	public DexRegister getIndexRegister() {
		return getIndexOperand().getRegister();
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x45: case 0x4c:
			return true;
		}
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
		0x44, "aget",
		0x45, "aget-wide",
		0x46, "aget-object",
		0x47, "aget-boolean",
		0x48, "aget-byte",
		0x49, "aget-char",
		0x4a, "aget-short",
		0x4b, "aput",
		0x4c, "aput-wide",
		0x4d, "aput-object",
		0x4e, "aput-boolean",
		0x4f, "aput-byte",
		0x50, "aput-char",
		0x51, "aput-short",
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		// e.g. out of bound
		return true;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.ARRAY_OP;
	}
}
