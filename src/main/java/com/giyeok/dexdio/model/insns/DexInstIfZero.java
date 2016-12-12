package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstIfZero extends DexInstruction {
	private int targetAddress;

	public DexInstIfZero(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		targetAddress = ((OperandBranchTarget) getOperand(1)).value() + address;
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 2;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandBranchTarget;
	}
	
	public int[] getPossibleGoThroughs() {
		return new int[] { getAddress() + getLength(), targetAddress };
	}
	
	public OperandRegister getOperand() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getOperandRegister() {
		return getOperand().getRegister();
	}
	
	public OperandBranchTarget getBranchTarget() {
		return (OperandBranchTarget) getOperand(1);
	}
	
	public int getBranchTargetAddress() {
		return getAddress() + getBranchTarget().value();
	}
	
	public int getFallthroughAddress() {
		return getAddress() + getLength();
	}
	
	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x38, "if-eqz",
			0x39, "if-nez",
			0x3a, "if-ltz",
			0x3b, "if-gez",
			0x3c, "if-gtz",
			0x3d, "if-lez",
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
		return DexInstructionType.IF_ZERO;
	}
}
