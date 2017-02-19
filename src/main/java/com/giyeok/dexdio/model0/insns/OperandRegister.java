package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;

public class OperandRegister extends Operand {
	private int number;
	
	private DexRegister register;

	public OperandRegister(DexCodeItem codeitem, int number) {
		super(codeitem);
		
		this.number = number;
		this.register = codeitem.getRegister(number);
	}
	
	public DexRegister getRegister() {
		return register;
	}

	@Override
	public String getStringRepresentation() {
		return register.getName();
	}
}
