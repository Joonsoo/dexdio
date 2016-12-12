package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstIf extends DexInstruction {
	private int targetAddress;

	public DexInstIf(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		targetAddress = ((OperandBranchTarget) getOperand(2)).value() + address;
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 3;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
		assert getOperand(2) instanceof OperandBranchTarget;
	}
	
	public int[] getPossibleGoThroughs() {
		return new int[] { getAddress() + getLength(), targetAddress };
	}
	
	public OperandRegister getFirstOperand() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getFirstOperandRegister() {
		return getFirstOperand().getRegister();
	}
	
	public OperandRegister getSecondOperand() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getSecondOperandRegister() {
		return getSecondOperand().getRegister();
	}
	
	public OperandBranchTarget getBranchTarget() {
		return (OperandBranchTarget) getOperand(2);
	}
	
	public int getBranchTargetAddress() {
		return getAddress() + getBranchTarget().value();
	}
	
	public int getFallthroughAddress() {
		return getAddress() + getLength();
	}
	
	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x32, "if-eq",
			0x33, "if-ne",
			0x34, "if-lt",
			0x35, "if-ge",
			0x36, "if-gt",
			0x37, "if-le"
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}
	
	@Override
	public boolean canThrowException() {
		return false;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.IF;
	}
}
