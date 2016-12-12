package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexProgram;

public abstract class DexInstruction {
	private DexCodeItem codeitem;
	private InstructionData instructionData;
	private int address;

	protected DexInstruction(DexCodeItem codeitem, int address, InstructionData instructionData) {
		this.codeitem = codeitem;
		this.address = address;
		this.instructionData = instructionData;
	}
	
	public DexCodeItem getCodeItem() {
		return codeitem;
	}
	
	public DexProgram getProgram() {
		return codeitem.getProgram();
	}
	
	public int getLength() {
		return instructionData.length();
	}
	
	public int getAddress() {
		return address;
	}
	
	public static String getOpcodeHex(int opcode) {
		final String hexcode = "0123456789abcdef";

		return hexcode.charAt((opcode & 0xf) >> 4) + "" + hexcode.charAt(opcode & 0xf);
	}

	public int[] getPossibleGoThroughs() {
		return new int[] { address + instructionData.length() };
	}
	
	public DexInstruction[] getPossibleNextInstructions() {
		int addresses[] = getPossibleGoThroughs();
		DexInstruction instructions[] = new DexInstruction[addresses.length];
		
		for (int i = 0; i < addresses.length; i++) {
			instructions[i] = codeitem.getInstructionAtAddress(addresses[i]);
		}
		return instructions;
	}

	public String getStringRepresentation() {
		return getOpcodeMnemonic() + " " + instructionData.getOperandsStringRepresentation();
	}
	
	public abstract MnemonicMap getMnemonicMap();
	
	public String getOpcodeMnemonic() {
		return getMnemonicMap().get(instructionData.opcode());
	}
	
	public int getOpcode() {
		return instructionData.opcode();
	}
	
	public int getOperandsLength() {
		return instructionData.operandsLength();
	}

	public Operand getOperand(int i) {
		return instructionData.operand(i);
	}
	
	public abstract boolean canThrowException();
	public abstract DexInstructionType getInstructionType();
	
	public static enum DexInstructionType {
		ARRAY_LENGTH,
		ARRAY_OP,
		BINARY_OP,
		BINARY_OP_LIT,
		CHECK_CAST,
		COMPARE,
		FILL_ARRAY_DATA,
		FILLED_NEW_ARRAY,
		GOTO,
		IF,
		IF_ZERO,
		INSTANCE_OF,
		INSTANCE_OP,
		INVOKE,
		MONITOR,
		MOVE,
		MOVE_CONST,
		MOVE_EXCEPTION,
		MOVE_RESULT,
		NEW_ARRAY,
		NEW_INSTANCE,
		NOP,
		RETURN,
		STATIC_OP,
		SWITCH,
		THROW,
		UNARY_OP
	}
}
