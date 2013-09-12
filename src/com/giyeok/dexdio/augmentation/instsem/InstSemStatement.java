package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.model.insns.DexInstruction;

public abstract class InstSemStatement implements InstructionSemantic {

	private DexInstruction instruction;
	
	public InstSemStatement() {
		instruction = null;
	}
	
	public void setInstruction(DexInstruction instruction) {
		this.instruction = instruction;
	}
	
	public DexInstruction getInstruction() {
		return instruction;
	}

}
