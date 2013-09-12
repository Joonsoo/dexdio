package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;

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
