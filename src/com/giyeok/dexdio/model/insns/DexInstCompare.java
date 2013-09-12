package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;

public class DexInstCompare extends DexInstruction {

	public DexInstCompare(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 3;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
		assert getOperand(2) instanceof OperandRegister;
	}
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}
	
	public OperandRegister getFirstOperand() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getFirstOperandRegister() {
		return getFirstOperand().getRegister();
	}
	
	public OperandRegister getSecondOperand() {
		return (OperandRegister) getOperand(2);
	}
	
	public DexRegister getSecondOperandRegister() {
		return getSecondOperand().getRegister();
	}
	
	public DexType getOperandsType() {
		switch (getOpcode()) {
		case 0x2d: case 0x2e:
			return getProgram().getFloatPrimitiveType();
		case 0x2f: case 0x30:
			return getProgram().getDoublePrimitiveType();
		case 0x31:
			return getProgram().getLongPrimitiveType();
		}
		assert false;
		return null;
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x2f: case 0x30: case 0x31:
			return true;
		}
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x2d, "cmpl-float",
			0x2e, "cmpg-float",
			0x2f, "cmpl-double",
			0x30, "cmpg-double",
			0x31, "cmp-long"
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
		return DexInstructionType.COMPARE;
	}
}
