package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;

public class InstSemUnaryOp implements InstSemOperand {
	
	public static enum UnaryOperator {
		NEG_INT,
		NOT_INT,
		NEG_LONG,
		NOT_LONG,
		NEG_FLOAT,
		NEG_DOUBLE,
		INT_TO_LONG,
		INT_TO_FLOAT,
		INT_TO_DOUBLE,
		LONG_TO_INT,
		LONG_TO_FLOAT,
		LONG_TO_DOUBLE,
		FLOAT_TO_INT,
		FLOAT_TO_LONG,
		FLOAT_TO_DOUBLE,
		DOUBLE_TO_INT,
		DOUBLE_TO_LONG,
		DOUBLE_TO_FLOAT,
		INT_TO_BYTE,
		INT_TO_CHAR,
		INT_TO_SHORT
	}
	
	private UnaryOperator operator;
	private InstSemRegister operand;
	
	private DexCodeItem codeitem;

	public InstSemUnaryOp(DexCodeItem codeitem, UnaryOperator operator, InstSemRegister operand) {
		this.codeitem = codeitem;
		
		this.operator = operator;
		this.operand = operand;
	}
	
	public InstSemUnaryOp(DexCodeItem codeitem, int opcode, InstSemRegister operand) {
		this.codeitem = codeitem;

		this.operand = operand;
		switch (opcode) {
		case 0x7b: this.operator = UnaryOperator.NEG_INT; break;
		case 0x7c: this.operator = UnaryOperator.NOT_INT; break;
		case 0x7d: this.operator = UnaryOperator.NEG_LONG; break;
		case 0x7e: this.operator = UnaryOperator.NOT_LONG; break;
		case 0x7f: this.operator = UnaryOperator.NEG_FLOAT; break;
		case 0x80: this.operator = UnaryOperator.NEG_DOUBLE; break;
		case 0x81: this.operator = UnaryOperator.INT_TO_LONG; break;
		case 0x82: this.operator = UnaryOperator.INT_TO_FLOAT; break;
		case 0x83: this.operator = UnaryOperator.INT_TO_DOUBLE; break;
		case 0x84: this.operator = UnaryOperator.LONG_TO_INT; break;
		case 0x85: this.operator = UnaryOperator.LONG_TO_FLOAT; break;
		case 0x86: this.operator = UnaryOperator.LONG_TO_DOUBLE; break;
		case 0x87: this.operator = UnaryOperator.FLOAT_TO_INT; break;
		case 0x88: this.operator = UnaryOperator.FLOAT_TO_LONG; break;
		case 0x89: this.operator = UnaryOperator.FLOAT_TO_DOUBLE; break;
		case 0x8a: this.operator = UnaryOperator.DOUBLE_TO_INT; break;
		case 0x8b: this.operator = UnaryOperator.DOUBLE_TO_LONG; break;
		case 0x8c: this.operator = UnaryOperator.DOUBLE_TO_FLOAT; break;
		case 0x8d: this.operator = UnaryOperator.INT_TO_BYTE; break;
		case 0x8e: this.operator = UnaryOperator.INT_TO_CHAR; break;
		case 0x8f: this.operator = UnaryOperator.INT_TO_SHORT; break;
		}
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public UnaryOperator getOperator() {
		return operator;
	}
	
	public InstSemRegister getOperand() {
		return operand;
	}

	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
		operand.setStatement(statement);
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
		return getOperatorStringRepresentation() + " " + operand.getStringRepresentation();
	}
	
	public String getOperatorStringRepresentation() {
		switch (operator) {
		case NEG_DOUBLE:
		case NEG_FLOAT:
		case NEG_INT:
		case NEG_LONG:
			return "-";
		case NOT_INT:
		case NOT_LONG:
			return "~";
		}
		return operator.toString();
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.UNARY_OP;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this, operand };
	}
}
