package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemLHS;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;

public class InstSemArrayReference implements InstSemOperand, InstSemLHS {
	
	private InstSemRegister array;
	private InstSemRegister index;
	
	private DexCodeItem codeitem;

	public InstSemArrayReference(DexCodeItem codeitem, InstSemRegister array, InstSemRegister index) {
		this.codeitem = codeitem;

		this.array = array;
		this.index = index;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public InstSemRegister getArray() {
		return array;
	}
	
	public InstSemRegister getIndex() {
		return index;
	}

	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
		array.setStatement(statement);
		index.setStatement(statement);
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
		return array.getStringRepresentation() + "[" + index.getStringRepresentation() + "]";
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public boolean mayReferSamePlace(InstSemLHS other) {
		if (other instanceof InstSemArrayReference) {
			return true;
		}
		return false;
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.ARRAY_REFERENCE;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this, array, index };
	}
}
