package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexType;
import com.giyeok.dexdio.model0.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstFilledNewArray extends DexInstruction {

	public DexInstFilledNewArray(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert mnemonics.containsKey(getOpcode());
		
		// TODO verify following 
		((OperandConstantPool) getOperand(0)).setConstantKind(ConstantPoolKind.TYPE);
	}

	public DexType getType() {
		return getProgram().getTypeByTypeId(((OperandConstantPool) getOperand(0)).getValue());
	}
	
	public DexRegister[] getArguments() {
		// TODO verify this
		if (getOpcode() == 0x24) {
			DexRegister registers[] = new DexRegister[getOperandsLength() - 1];
			for (int i = 1; i < getOperandsLength(); i++) {
				registers[i - 1] = ((OperandRegister) getOperand(i)).getRegister();
			}
			return registers;
		} else {
			assert getOpcode() == 0x25;
			OperandRegisterRange range = (OperandRegisterRange) getOperand(1);
			return range.getRegisters();
		}
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x24, "filled-new-array",
			0x25, "filled-new-array/range"
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
		return DexInstructionType.FILLED_NEW_ARRAY;
	}
}
