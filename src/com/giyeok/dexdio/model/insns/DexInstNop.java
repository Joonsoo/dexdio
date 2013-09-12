package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;

public class DexInstNop extends DexInstruction {

	public DexInstNop(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x00, "nop",
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
		return DexInstructionType.NOP;
	}
}
