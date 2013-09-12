package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstCheckCast extends DexInstruction {

	public DexInstCheckCast(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 2;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandConstantPool;

		((OperandConstantPool) getOperand(1)).setConstantKind(ConstantPoolKind.TYPE);
	}
	
	public DexType getType() {
		return getProgram().getTypeByTypeId(((OperandConstantPool) getOperand(1)).getValue());
	}
	
	public OperandRegister getInstance() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getInstanceRegister() {
		return getInstance().getRegister();
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
		0x1f, "check-cast",
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		// this instruction is designed to throw an exception
		return true;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.CHECK_CAST;
	}
}
