package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexType;
import com.giyeok.dexdio.model0.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstInstanceOf extends DexInstruction {

	public DexInstInstanceOf(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 3;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
		assert getOperand(2) instanceof OperandConstantPool;
		
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
	
	public OperandRegister getInstance() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getInstanceRegister() {
		return getInstance().getRegister();
	}
	
	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x20, "instance-of",
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
		return DexInstructionType.INSTANCE_OF;
	}
}
