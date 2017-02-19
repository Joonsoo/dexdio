package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;

public class DexInstReturn extends DexInstruction {

	public DexInstReturn(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 0 || (getOperandsLength() == 1 && getOperand(0) instanceof OperandRegister);
	}
	
	public OperandRegister getValue() {
		if (getOperandsLength() == 0) {
			return null;
		}
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getValueRegister() {
		if (getOperandsLength() == 0) {
			return null;
		}
		return getValue().getRegister();
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x10:
			return true;
		}
		return false;
	}
	
	public int[] getPossibleGoThroughs() {
		return new int[] { };
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x0e, "return-void",
			0x0f, "return",
			0x10, "return-wide",
			0x11, "return-object"
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
		return DexInstructionType.RETURN;
	}
}
