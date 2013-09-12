package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstMoveResult extends DexInstruction {

	public DexInstMoveResult(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 1;
		assert getOperand(0) instanceof OperandRegister;
	}
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x0b:
			return true;
		}
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x0a, "move-result",
			0x0b, "move-result-wide",
			0x0c, "move-result-object",
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}
	
	public boolean canThrowException() {
		return false;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.MOVE_RESULT;
	}
}
