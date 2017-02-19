package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;

public class DexInstFillArrayData extends DexInstruction {

	public DexInstFillArrayData(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 2;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandBranchTarget;
		
		int tableAddress = ((OperandBranchTarget) getOperand(1)).value() + address;
		// TODO table load
	}
	
	public OperandRegister getArray() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getArrayRegister() {
		return getArray().getRegister();
	}
	
	public OperandBranchTarget getPayloadAddress() {
		return (OperandBranchTarget) getOperand(1);
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x26, "fill-array-data"
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
		return DexInstructionType.FILL_ARRAY_DATA;
	}
}
