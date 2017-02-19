package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexField;
import com.giyeok.dexdio.model0.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstInstanceOp extends DexInstruction {

	public DexInstInstanceOp(DexCodeItem codeitem, int address, 
			InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 3;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
		assert getOperand(2) instanceof OperandConstantPool;
		
		((OperandConstantPool) getOperand(2)).setConstantKind(ConstantPoolKind.FIELD);
	}
	
	public DexField getField() {
		return getProgram().getFieldByFieldId(((OperandConstantPool) getOperand(2)).getValue());
	}
	
	public OperandRegister getSourceOrDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getSourceOrDestinationRegister() {
		return getSourceOrDestination().getRegister();
	}
	
	public OperandRegister getInstance() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getInstanceRegister() {
		return getInstance().getRegister();
	}
	
	public boolean isGetOperation() {
		switch (getOpcode()) {
		case 0x52: case 0x53: case 0x54: case 0x55: 
		case 0x56: case 0x57: case 0x58:
			return true;
		}
		return false;
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x53: case 0x5a:
			return true;
		}
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x52, "iget",
			0x53, "iget-wide",
			0x54, "iget-object",
			0x55, "iget-boolean",
			0x56, "iget-byte",
			0x57, "iget-char",
			0x58, "iget-short",
			0x59, "iput",
			0x5a, "iput-wide",
			0x5b, "iput-object",
			0x5c, "iput-boolean",
			0x5d, "iput-byte",
			0x5e, "iput-char",
			0x5f, "iput-short"
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
		return DexInstructionType.INSTANCE_OP;
	}
}
