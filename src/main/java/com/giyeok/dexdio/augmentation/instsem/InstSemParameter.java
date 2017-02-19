package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexParameter;
import com.giyeok.dexdio.model0.DexType;

public class InstSemParameter implements InstSemOperand {
	
	private DexParameter parameter;
	private DexRegister register;
	private boolean instanceRegister;
	
	private DexCodeItem codeitem;
	
	public InstSemParameter(DexCodeItem codeitem, DexParameter parameter) {
		this.codeitem = codeitem;
		
		this.parameter = parameter;
		this.register = null;
		this.instanceRegister = false;
	}
	
	public InstSemParameter(DexCodeItem codeitem, DexRegister register, boolean instanceRegister) {
		this.codeitem = codeitem;
		
		this.register = register;
		this.parameter = null;
		this.instanceRegister = instanceRegister;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public DexParameter getParameter() {
		return parameter;
	}
	
	public DexRegister getRegister() {
		if (parameter != null) {
			return parameter.getRegister();
		} else {
			return register;
		}
	}
	
	public boolean isWide() {
		if (parameter != null) {
			return parameter.isWide();
		} else {
			return false;
		}
	}
	
	public boolean isInstanceRegister() {
		return instanceRegister;
	}
	
	@Override
	public InstSemStatement getStatement() {
		return null;
	}

	@Override
	public void setStatement(InstSemStatement statement) {
		assert false;
		// should not be here
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
		if (parameter != null) {
			return parameter.getStringRepresentation();
		} else {
			return register.getName();
		}
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.PARAMETER;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this };
	}
}
