package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemLHS;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexType;

public class InstSemInstanceField implements InstSemLHS, InstSemOperand {
	
	private InstSemRegister instance;
	private DexField field;
	private String fieldname;
	
	private DexCodeItem codeitem;

	public InstSemInstanceField(DexCodeItem codeitem, InstSemRegister instance, DexField field) {
		this.codeitem = codeitem;
		
		this.instance = instance;
		this.field = field;
		this.fieldname = null;
	}
	
	public InstSemInstanceField(DexCodeItem codeitem, InstSemRegister instance, String fieldname) {
		this.codeitem = codeitem;
		
		this.instance = instance;
		this.fieldname = fieldname;
		this.field = null;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public InstSemRegister getInstance() {
		return instance;
	}
	
	public DexField getField() {
		return field;
	}
	
	public String getFieldName() {
		if (field != null) {
			return field.getName();
		} else {
			return fieldname;
		}
	}

	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
		instance.setStatement(statement);
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
		if (field == null) {
			return instance.getStringRepresentation() + "." + fieldname;
		} else {
			return "((" + field.getBelongedClass().getTypeFullNameBeauty() + ")" + instance.getStringRepresentation() + ")." + field.getName();
		}
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public boolean mayReferSamePlace(InstSemLHS other) {
		if (other instanceof InstSemInstanceField) {
			if (this.field != null) {
				return this.field == ((InstSemInstanceField) other).field;
			} else {
				return this.fieldname.equals(((InstSemInstanceField) other).fieldname);
			}
		}
		return false;
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.INSTANCE_FIELD;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this, instance };
	}
}
