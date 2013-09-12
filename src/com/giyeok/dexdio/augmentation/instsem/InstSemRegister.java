package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemElement;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemLHS;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexType;

public class InstSemRegister implements InstSemLHS, InstSemOperand, InstSemElement {
	
	DexRegister register;
	
	private DexCodeItem codeitem;
	
	public InstSemRegister(DexCodeItem codeitem, DexRegister register) {
		this.codeitem = codeitem;
		
		this.register = register;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public DexRegister getRegister() {
		return register;
	}
	
	public static InstSemRegister[] genFromDexRegisters(DexCodeItem codeitem, DexRegister[] registers, DexType[] types, int from) {
		InstSemRegister result[] = new InstSemRegister[types.length];
		
		int j = from;
		for (int i = 0; i < types.length; i++) {
			/*
			if (registers.length >= j) {
				System.out.println("Method invocation arguments mismatch");
				return genFromDexRegisters(registers, from);
			}
			*/
			if (types[i].isWide()) {
				result[i] = new InstSemRegisterPair(codeitem, registers[j]);
				result[i].setOperandTypeData(types[i]);
				j += 2;
			} else {
				result[i] = new InstSemRegister(codeitem, registers[j]);
				result[i].setOperandTypeData(types[i]);
				j += 1;
			}
		}
		if (j != registers.length) {
			System.out.println("Method invocation arguments mismatch");
			return genFromDexRegisters(codeitem, registers, from);
		}
		return result;
	}
	
	public static InstSemRegister[] genFromDexRegisters(DexCodeItem codeitem, DexRegister[] registers, int from) {
		InstSemRegister result[] = new InstSemRegister[registers.length - from];
		
		for (int i = from; i < registers.length; i++) {
			result[i - from] = new InstSemRegister(codeitem, registers[i]);
		}
		return result;
	}

	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
	}

	@Override
	public InstSemStatement getStatement() {
		return this.statement;
	}
	
	private DexType operandType;

	@Override
	public void setOperandTypeData(DexType type) {
		this.operandType = type;
	}

	@Override
	public DexType getOperandTypeData() {
		return operandType;
	}

	@Override
	public String getStringRepresentation() {
		return register.getName();
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public boolean mayReferSamePlace(InstSemLHS other) {
		if (other instanceof InstSemRegister && ! (other instanceof InstSemRegisterPair)) {
			return this.register == ((InstSemRegister) other).register;
		}
		return false;
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.REGISTER;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this };
	}
}
