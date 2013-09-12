package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;

public class DexInstGoto extends DexInstruction {
	private int targetAddress;

	public DexInstGoto(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 1;
		assert getOperand(0) instanceof OperandBranchTarget;

		targetAddress = ((OperandBranchTarget) getOperand(0)).value() + address;
	}
	
	public OperandBranchTarget getBranchTarget() {
		return (OperandBranchTarget) getOperand(0);
	}
	
	public int getBranchTargetAddress() {
		return targetAddress;
	}

	public int[] getPossibleGoThroughs() {
		return new int[] { targetAddress };
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x28, "goto",
			0x29, "goto/16",
			0x2a, "goto/32"
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
		return DexInstructionType.GOTO;
	}
}
