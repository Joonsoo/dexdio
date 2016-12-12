package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstThrow extends DexInstruction {

	public DexInstThrow(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 1;
		assert getOperand(0) instanceof OperandRegister;
	}
	
	public int[] getPossibleGoThroughs() {
		return new int[0];
	}
	
	public OperandRegister getException() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getExceptionRegister() {
		return getException().getRegister();
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x27, "throw"
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		// this instruction always throws an exception
		return true;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.THROW;
	}
}
