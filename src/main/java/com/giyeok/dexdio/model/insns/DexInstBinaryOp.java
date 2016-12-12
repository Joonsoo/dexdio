package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;

public class DexInstBinaryOp extends DexInstruction {

	public DexInstBinaryOp(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);
		
		assert (mnemonics.containsKey(getOpcode()));
		
		if (getOperandsLength() == 3) {
			assert (getOperand(0) instanceof OperandRegister);
			assert (getOperand(1) instanceof OperandRegister);
			assert (getOperand(2) instanceof OperandRegister);
			
			dest = (OperandRegister) getOperand(0);
			op1 = (OperandRegister) getOperand(1);
			op2 = (OperandRegister) getOperand(2);
		} else if (getOperandsLength() == 2) {
			assert (getOperand(0) instanceof OperandRegister);
			assert (getOperand(1) instanceof OperandRegister);

			dest = (OperandRegister) getOperand(0);
			op1 = (OperandRegister) getOperand(0);
			op2 = (OperandRegister) getOperand(1);
		} else {
			assert false;
		}
	}
	
	private OperandRegister dest;
	private OperandRegister op1;
	private OperandRegister op2;
	
	public OperandRegister getDestination() {
		return dest;
	}
	
	public DexRegister getDestinationRegister() {
		return getDestination().getRegister();
	}
	
	public OperandRegister getFirstOperand() {
		return op1;
	}
	
	public DexRegister getFirstOperandRegister() {
		return getFirstOperand().getRegister();
	}
	
	public OperandRegister getSecondOperand() {
		return op2;
	}
	
	public DexRegister getSecondOperandRegister() {
		return getSecondOperand().getRegister();
	}
	
	public DexType getOperandsType() {
		switch (getOpcode()) {
		case 0x90: case 0x91: case 0x92: case 0x93: case 0x94: case 0x95: 
		case 0x96: case 0x97: case 0x98: case 0x99: case 0x9a:
		case 0xb0: case 0xb1: case 0xb2: case 0xb3: case 0xb4: case 0xb5: 
		case 0xb6: case 0xb7: case 0xb8: case 0xb9: case 0xba:
			return getProgram().getIntegerPrimitiveType();
		case 0x9b: case 0x9c: case 0x9d: case 0x9e: case 0x9f: case 0xa0: 
		case 0xa1: case 0xa2: case 0xa3: case 0xa4: case 0xa5:
		case 0xbb: case 0xbc: case 0xbd: case 0xbe: case 0xbf: case 0xc0: 
		case 0xc1: case 0xc2: case 0xc3: case 0xc4: case 0xc5:
			return getProgram().getLongPrimitiveType();
		case 0xa6: case 0xa7: case 0xa8: case 0xa9: case 0xaa:
		case 0xc6: case 0xc7: case 0xc8: case 0xc9: case 0xca:
			return getProgram().getFloatPrimitiveType();
		case 0xab: case 0xac: case 0xad: case 0xae: case 0xaf:
		case 0xcb: case 0xcc: case 0xcd: case 0xce: case 0xcf:
			return getProgram().getDoublePrimitiveType();
		}
		assert false;
		return null;
	}
	
	public boolean isWide() {
		// TODO needs verify
		switch (getOpcode()) {
		// *-long
		case 0x9b: case 0x9c: case 0x9d: case 0x9e: case 0x9f: case 0xa0:
		case 0xa1: case 0xa2: case 0xa3: case 0xa4: case 0xa5:
		// *-double
		case 0xab: case 0xac: case 0xad: case 0xae: case 0xaf:
		// *-long/2addr
		case 0xbb: case 0xbc: case 0xbd: case 0xbe: case 0xbf: case 0xc0:
		case 0xc1: case 0xc2: case 0xc3: case 0xc4: case 0xc5:
		// *-double/2addr
		case 0xcb: case 0xcc: case 0xcd: case 0xce: case 0xcf:
			return true;
		}
		return false;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x90, "add-int", 
			0x91, "sub-int", 
			0x92, "mul-int", 
			0x93, "div-int", 
			0x94, "rem-int", 
			0x95, "and-int", 
			0x96, "or-int", 
			0x97, "xor-int", 
			0x98, "shl-int", 
			0x99, "shr-int", 
			0x9a, "ushr-int", 
			0x9b, "add-long", 
			0x9c, "sub-long", 
			0x9d, "mul-long", 
			0x9e, "div-long", 
			0x9f, "rem-long", 
			0xa0, "and-long", 
			0xa1, "or-long", 
			0xa2, "xor-long", 
			0xa3, "shl-long", 
			0xa4, "shr-long", 
			0xa5, "ushr-long", 
			0xa6, "add-float", 
			0xa7, "sub-float", 
			0xa8, "mul-float", 
			0xa9, "div-float", 
			0xaa, "rem-float", 
			0xab, "add-double", 
			0xac, "sub-double", 
			0xad, "mul-double", 
			0xae, "div-double", 
			0xaf, "rem-double",

			0xb0, "add-int/2addr",
			0xb1, "sub-int/2addr",
			0xb2, "mul-int/2addr",
			0xb3, "div-int/2addr",
			0xb4, "rem-int/2addr",
			0xb5, "and-int/2addr",
			0xb6, "or-int/2addr",
			0xb7, "xor-int/2addr",
			0xb8, "shl-int/2addr",
			0xb9, "shr-int/2addr",
			0xba, "ushr-int/2addr",
			0xbb, "add-long/2addr",
			0xbc, "sub-long/2addr",
			0xbd, "mul-long/2addr",
			0xbe, "div-long/2addr",
			0xbf, "rem-long/2addr",
			0xc0, "and-long/2addr",
			0xc1, "or-long/2addr",
			0xc2, "xor-long/2addr",
			0xc3, "shl-long/2addr",
			0xc4, "shr-long/2addr",
			0xc5, "ushr-long/2addr",
			0xc6, "add-float/2addr",
			0xc7, "sub-float/2addr",
			0xc8, "mul-float/2addr",
			0xc9, "div-float/2addr",
			0xca, "rem-float/2addr",
			0xcb, "add-double/2addr",
			0xcc, "sub-double/2addr",
			0xcd, "mul-double/2addr",
			0xce, "div-double/2addr",
			0xcf, "rem-double/2addr"
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
		return DexInstructionType.BINARY_OP;
	}
}
