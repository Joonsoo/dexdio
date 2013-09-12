package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;

public class OperandBranchTarget extends Operand {
	private int value;

	public OperandBranchTarget(DexCodeItem codeitem, int value, int length) {
		super(codeitem);
		
		assert length == 1 || length == 2 || length == 4;
		
		if (length == 1) {
			this.value = (byte) value;
		} else if (length == 2) {
			this.value = (short) value;
		} else {
			this.value = value;
		}
	}
	
	public int value() {
		return value;
	}

	@Override
	public String getStringRepresentation() {
		return Integer.toHexString(getBelongedInstructionData().getBelongedInstruction().getAddress() + value);
	}
}
