package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;

public class DexInstBinaryOpLit extends DexInstruction {
	public DexInstBinaryOpLit(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert (mnemonics.containsKey(getOpcode()));
		assert getOperandsLength() == 3;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandRegister;
		assert getOperand(2) instanceof OperandIntegerConstant;
	}
	
	public OperandRegister getDestination() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}
	
	public OperandRegister getFirstOperand() {
		return (OperandRegister) getOperand(1);
	}
	
	public DexRegister getFirstOperandRegister() {
		return getFirstOperand().getRegister();
	}
	
	public OperandIntegerConstant getSecondOperand() {
		return (OperandIntegerConstant) getOperand(2);
	}
	
	public DexType getOperandsType() {
		return getProgram().getIntegerPrimitiveType();
	}
	
	public boolean isWide() {
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0xd0, "add-int/lit16",
			0xd1, "rsub-int (reverse subtract)",
			0xd2, "mul-int/lit16",
			0xd3, "div-int/lit16",
			0xd4, "rem-int/lit16",
			0xd5, "and-int/lit16",
			0xd6, "or-int/lit16",
			0xd7, "xor-int/lit16",

			0xd8, "add-int/lit8",
			0xd9, "rsub-int/lit8",
			0xda, "mul-int/lit8",
			0xdb, "div-int/lit8",
			0xdc, "rem-int/lit8",
			0xdd, "and-int/lit8",
			0xde, "or-int/lit8",
			0xdf, "xor-int/lit8",
			0xe0, "shl-int/lit8",
			0xe1, "shr-int/lit8",
			0xe2, "ushr-int/lit8"
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		// e.g. div by zero
		return true;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.BINARY_OP_LIT;
	}

}
