package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstMoveException extends DexInstruction {

	public DexInstMoveException(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 1;
		assert getOperand(0) instanceof OperandRegister;
	}
	
	public OperandRegister getTarget() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getTargetRegister() {
		return getTarget().getRegister();
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x0d, "move-exception",
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
		return DexInstructionType.MOVE_EXCEPTION;
	}
}
