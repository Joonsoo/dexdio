package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;

public class DexInstMove extends DexInstruction {

	public DexInstMove(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert (mnemonics.containsKey(getOpcode()));
		assert (getOperandsLength() == 2);
		assert (getOperand(0) instanceof OperandRegister);
		assert (getOperand(1) instanceof OperandRegister);
	}
	
	public DexRegister getSourceRegister() {
		return ((OperandRegister) getOperand(1)).getRegister();
	}
	
	public DexRegister getDestinationRegister() {
		return ((OperandRegister) getOperand(0)).getRegister();
	}
	
	public Operand getDestination() {
		return getOperand(0);
	}
	
	public Operand getSource() {
		return getOperand(1);
	}
	
	public boolean isWide() {
		switch (getOpcode()) {
		case 0x04: case 0x05: case 0x06:
			return true;
		}
		return false;
	}
	
	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x01, "move",
			0x02, "move/from16",
			0x03, "move/16",
			0x04, "move-wide",
			0x05, "move-wide/from16",
			0x06, "move-wide/16",
			0x07, "move-object",
			0x08, "move-object/from16",
			0x09, "move-object/16"
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
		return DexInstructionType.MOVE;
	}
}
