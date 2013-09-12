package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstMoveConst extends DexInstruction {

	protected DexInstMoveConst(DexCodeItem codeitem, int address,
			InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert (mnemonics.containsKey(getOpcode()));
		assert (getOperandsLength() == 2);
		assert (getOperand(0) instanceof OperandRegister);

		switch (getOpcode()) {
		case 0x1a: case 0x1b:
			((OperandConstantPool) getOperand(1)).setConstantKind(ConstantPoolKind.STRING);
			break;
		case 0x1c:
			((OperandConstantPool) getOperand(1)).setConstantKind(ConstantPoolKind.TYPE);
			break;
		}
	}

	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public Operand getSource() {
		return getOperand(1);
	}
	
	public DexType getConstClassType() {
		if (getOpcode() != 0x1c) {
			return null;
		}
		return getProgram().getTypeByTypeId(((OperandConstantPool) getOperand(1)).getValue());
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x16: case 0x17: case 0x18: case 0x19:
			return true;
		}
		return false;
	}
	
	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x12, "const/4",
			0x13, "const/16",
			0x14, "const",
			0x15, "const/high16",
			0x16, "const-wide/16",
			0x17, "const-wide/32",
			0x18, "const-wide vAA",
			0x19, "const-wide/high16",
			0x1a, "const-string",
			0x1b, "const-string/jumbo",
			0x1c, "const-class"
	});

	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		return false;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.MOVE_CONST;
	}
}
