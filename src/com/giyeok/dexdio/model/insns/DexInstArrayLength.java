package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstArrayLength extends DexInstruction {

	public DexInstArrayLength(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 2;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
	}
	
	public OperandRegister getSourceArray() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getSourceArrayRegister() {
		return getSourceArray().getRegister();
	}
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}

	public static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x21, "array-length",
		});
		
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		// e.g. not an array
		return true;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.ARRAY_LENGTH;
	}
}
