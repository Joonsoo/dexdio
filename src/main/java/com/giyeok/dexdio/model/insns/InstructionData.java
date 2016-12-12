package com.giyeok.dexdio.model.insns;

public class InstructionData {
	private int length;
	private int[] raw;
	private int opcode;
	private Operand[] operands;
	
	private DexInstruction belongedInstruction;

	public InstructionData(int length, int[] raw, int opcode, Operand[] operands) {
		this.length = length;
		this.raw = raw;
		this.opcode = opcode;
		this.operands = operands;
		
		if (operands != null) {
			for (Operand operand: operands) {
				operand.setBelongedInstructionData(this);
			}
		}

		this.belongedInstruction = null;
		
		assert ((raw[0] & 0xff) == opcode);
	}
	
	void setBelongedInstruction(DexInstruction belonged) {
		this.belongedInstruction = belonged;
	}
	
	public DexInstruction getBelongedInstruction() {
		return belongedInstruction;
	}
	
	public int opcode() {
		return opcode;
	}
	
	public int length() {
		return length;
	}
	
	public int operandsLength() {
		if (operands == null) {
			return 0;
		}
		return operands.length;
	}
	
	public Operand operand(int i) {
		return operands[i];
	}
	
	public String getOperandsStringRepresentation() {
		if (operands == null) {
			return "";
		} else {
			StringBuffer buf = new StringBuffer();
			
			for (int i = 0; i < operands.length; i++) {
				if (i > 0) {
					buf.append(", ");
				}
				buf.append(operands[i].getStringRepresentation());
			}
			return buf.toString();
		}
	}
}
