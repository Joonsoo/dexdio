package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexType;
import com.giyeok.dexdio.model0.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstNewArray extends DexInstruction {

	public DexInstNewArray(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());

		((OperandConstantPool) getOperand(2)).setConstantKind(ConstantPoolKind.TYPE);
	}
	
	public DexType getType() {
		return getProgram().getTypeByTypeId(((OperandConstantPool) getOperand(2)).getValue());
	}
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}
	
	public OperandRegister getSizeOperand() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getSizeRegister() {
		return getSizeOperand().getRegister();
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x23, "new-array",
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
		return DexInstructionType.NEW_ARRAY;
	}
}
