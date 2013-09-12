package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;

public interface InstructionSemantic {
	
	public static interface InstSemOperand extends InstructionSemantic {
		public void setStatement(InstSemStatement statement);
		
		public void setOperandTypeData(DexType operandType);
		public DexType getOperandTypeData();
	}

	public static interface InstSemLHS extends InstSemOperand {
		// array reference, instance field, static field, and register
		public boolean mayReferSamePlace(InstSemLHS other);
	}
	
	public static interface InstSemElement extends InstSemOperand {
		// only registers and const values are element
	}
	
	public String getStringRepresentation();
	
	public DexCodeItem getBelongedCodeItem();
	
	/**
	 * returns the operands which are contained in this operand(including itself)
	 * @return
	 */
	public InstSemOperand[] getContainingOperands();

	/**
	 * returns the instruction semantic statement which this item belongs to
	 * This method returns null if this item initialized before the first instruction of the method executed, mainly parameters or "this" variable
	 * @return
	 */
	public InstSemStatement getStatement();
	
	/**
	 * returns the type of this instruction semantic
	 * @return
	 */
	public InstSemType getInstSemType();
	
	public static enum InstSemType {
		ARRAY_REFERENCE,
		ASSIGNMENT_STATEMENT,
		BINARY_OP,
		CONST,
		ETC_OPERAND,
		ETC_STATEMENT,
		INSTANCE_FIELD,
		INSTANCE_METHOD_INVOKE,
		NEW_ARRAY,
		NEW_INSTANCE,
		PARAMETER,
		REGISTER,
		REGISTER_PAIR,
		STATIC_FIELD,
		STATIC_METHOD_INVOKE,
		UNARY_OP
	}
}
