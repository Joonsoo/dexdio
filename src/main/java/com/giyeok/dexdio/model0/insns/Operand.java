package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;

public abstract class Operand {
	private DexCodeItem codeitem;
	
	private InstructionData data;
	
	public Operand(DexCodeItem codeitem) {
		this.codeitem = codeitem;
		
		this.data = null;
	}
	
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	void setBelongedInstructionData(InstructionData data) {
		this.data = data;
	}
	
	public InstructionData getBelongedInstructionData() {
		return data;
	}

	public abstract String getStringRepresentation();
}
