package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemLHS;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexType;

public class InstSemStaticField implements InstSemLHS, InstSemOperand {
	
	private DexField field;
	
	private DexCodeItem codeitem;

	public InstSemStaticField(DexCodeItem codeitem, DexField field) {
		this.codeitem = codeitem;
		
		this.field = field;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public DexField getField() {
		return field;
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
		return field.getBelongedClass().getTypeFullNameBeauty() + "." + field.getName();
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public boolean mayReferSamePlace(InstSemLHS other) {
		if (other instanceof InstSemStaticField) {
			return this.field == ((InstSemStaticField) other).field;
		}
		return false;
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.STATIC_FIELD;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this };
	}
}
