package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.augmentation.Logger;
import com.giyeok.dexdio.augmentation.Logger.Log;
import com.giyeok.dexdio.model.DexAccessFlags;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexInternalClass;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexMethodContainingType;
import com.giyeok.dexdio.model.insns.OperandConstantPool.ConstantPoolKind;

public class DexInstInvoke extends DexInstruction {

	public DexInstInvoke(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperand(0) instanceof OperandConstantPool;
		
		((OperandConstantPool) getOperand(0)).setConstantKind(ConstantPoolKind.METHOD);
		
		// invoke-static 혹은 invoke-static/range 이면서 getOperandsLength() == 1이거나, 그렇지 않은 경우
		// getOperand(1)은 OperandRegisterRange이거나
		// getOperand(1..)이 모두 OperandRegister이어야 한다
		if (getOperandsLength() == 1) {
			assert getInvokeType() == InvokeType.STATIC;
		} else {
			if (getOperand(1) instanceof OperandRegisterRange) {
				assert getOperandsLength() == 2;
			} else {
				for (int i = 1; i < getOperandsLength(); i++) {
					assert getOperand(i) instanceof OperandRegister;
				}
			}
		}
		
		switch (getInvokeType()) {
		case VIRTUAL: {
			DexAccessFlags af = getInvokingMethod().getAccessFlags();
			if (! ((af.isUndefined()) || ((! af.isPrivate()) && (! af.isStatic()) && (! af.isFinal()) && (! af.isConstructor())))) {
				Logger.addMessage(getProgram(), Log.line(new Log[] { Log.log("invoke-virtual assertion failed at "), Log.log(codeitem.getBelongedMethod()), Log.log(":"), Log.log(Integer.toHexString(getAddress())) }));
			}
			break;
		}
		case SUPER: {
			DexMethodContainingType type = codeitem.getBelongedMethod().getBelongedType();
			assert type instanceof DexInternalClass;
			assert getInvokingMethod().getBelongedType() == ((DexInternalClass) type).getSuperClass();
			break;
		}
		case DIRECT: {
			DexAccessFlags af = getInvokingMethod().getAccessFlags();
			assert (af.isUndefined()) || (af.isPrivate()) || (af.isConstructor());
			break;
		}
		case STATIC: {
			DexAccessFlags af = getInvokingMethod().getAccessFlags();
			assert (af.isUndefined()) || (af.isStatic());
			break;
		}
		case INTERFACE: {
			break;
		}
		}
	}
	
	public DexMethod getInvokingMethod() {
		return getProgram().getMethodByMethodId(((OperandConstantPool) getOperand(0)).getValue());
	}
	
	public static enum InvokeType {
		VIRTUAL,
		SUPER,
		DIRECT,
		STATIC,
		INTERFACE
	}
	
	public InvokeType getInvokeType() {
		switch (getOpcode()) {
		case 0x6e: case 0x74:
			return InvokeType.VIRTUAL;
		case 0x6f: case 0x75:
			return InvokeType.SUPER;
		case 0x70: case 0x76:
			return InvokeType.DIRECT;
		case 0x71: case 0x77:
			return InvokeType.STATIC;
		case 0x72: case 0x78:
			return InvokeType.INTERFACE;
		/*
		 * 6e: invoke-virtual
		 * 74: invoke-virtual/range

		 * 6f: invoke-super
		 * 75: invoke-super/range
		 * 
		 * 70: invoke-direct
		 * 76: invoke-direct/range
		 * 
		 * 71: invoke-static
		 * 77: invoke-static/range
		 * 
		 * 72: invoke-interface
		 * 78: invoke-interface/range
		**/
		}
		assert false;
		return null;
	}
	
	public DexRegister[] getArgumentRegisters() {
		if (getOperandsLength() >= 2) {
			if (getOperand(1) instanceof OperandRegisterRange) {
				OperandRegisterRange orr = (OperandRegisterRange) getOperand(1);
				
				return orr.getRegisters();
			}
		}
		DexRegister registers[] = new DexRegister[getOperandsLength() - 1];
		for (int i = 0; i < registers.length; i++) {
			registers[i] = ((OperandRegister) getOperand(i + 1)).getRegister();
		}
		return registers;
	}
	
	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x6e, "invoke-virtual",
			0x6f, "invoke-super",
			0x70, "invoke-direct",
			0x71, "invoke-static",
			0x72, "invoke-interface",
			0x74, "invoke-virtual/range",
			0x75, "invoke-super/range",
			0x76, "invoke-direct/range",
			0x77, "invoke-static/range",
			0x78, "invoke-interface/range"
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		return true;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.INVOKE;
	}
}
