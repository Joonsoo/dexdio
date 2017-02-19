package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemElement;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexField;
import com.giyeok.dexdio.model0.DexType;
import com.giyeok.dexdio.model0.insns.OperandBranchTarget;
import com.giyeok.dexdio.model0.insns.OperandConstantPool;
import com.giyeok.dexdio.model0.insns.OperandIntegerConstant;

public class InstSemConst implements InstSemOperand, InstSemElement {
	
	private OperandIntegerConstant intval;
	private OperandConstantPool poolval;
	private OperandBranchTarget branch;
	private DexType type;
	private DexField field;
	
	private DexCodeItem codeitem;
	
	private InstSemConst(DexCodeItem codeitem) {
		this.codeitem = codeitem;
		
		this.intval = null;
		this.poolval = null;
		this.type = null;
		this.field = null;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}

	public InstSemConst(DexCodeItem codeitem, OperandIntegerConstant intval) {
		this(codeitem);
		this.intval = intval;
	}
	
	public InstSemConst(DexCodeItem codeitem, OperandConstantPool poolval) {
		this(codeitem);
		this.poolval = poolval;
	}
	
	public InstSemConst(DexCodeItem codeitem, OperandBranchTarget branch) {
		this(codeitem);
		this.branch = branch;
	}
	
	public InstSemConst(DexCodeItem codeitem, DexType type) {
		this(codeitem);
		this.type = type;
	}
	
	public InstSemConst(DexCodeItem codeitem, DexField field) {
		this(codeitem);
		this.field = field;
	}

	@Override
	public String getStringRepresentation() {
		if (intval != null) {
			return intval.getStringRepresentation();
		} else if (poolval != null) {
			return poolval.getStringRepresentation();
		} else if (branch != null) {
			return branch.getStringRepresentation();
		} else if (type != null) {
			return type.getTypeFullNameBeauty();
		} else if (field != null) {
			return field.getBelongedClass().getTypeFullNameBeauty() + "." + field.getName();
		}
		return "";
	}
	
	public String getStringRepresentation(DexType type) {
		if (intval != null) {
			return intval.getStringRepresentation(type);
		} else {
			return getStringRepresentation();
		}
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
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.CONST;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this };
	}
}
