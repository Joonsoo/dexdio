package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstNewInstance extends DexInstruction {

	public DexInstNewInstance(DexCodeItem codeitem, int address, 
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
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x22, "new-instance"
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
		return DexInstructionType.NEW_INSTANCE;
	}
}
