package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;


public class OperandRegisterRange extends Operand {
	private int start;
	private int count;
	
	private DexRegister[] registers;

	public OperandRegisterRange(DexCodeItem codeitem, int start, int count) {
		super(codeitem);
		
		this.start = start;
		this.count = count;
		
		registers = new DexRegister[count];
		for (int i = 0; i < count; i++) {
			registers[i] = codeitem.getRegister(start + i);
		}
	}
	
	public DexRegister[] getRegisters() {
		return registers;
	}

	@Override
	public String getStringRepresentation() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("{");
		for (int i = 0; i < registers.length; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(registers[i].getName());
		}
		return buf.toString();
	}
}
