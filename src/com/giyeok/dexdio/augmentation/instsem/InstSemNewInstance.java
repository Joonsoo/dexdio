package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;

public class InstSemNewInstance implements InstSemOperand {
	
	private DexType type;
	
	private DexCodeItem codeitem;

	public InstSemNewInstance(DexCodeItem codeitem, DexType type) {
		this.codeitem = codeitem;
		
		this.type = type;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public DexType getType() {
		return type;
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
		return "new " + type.getTypeFullNameBeauty();
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.NEW_INSTANCE;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this };
	}
}
