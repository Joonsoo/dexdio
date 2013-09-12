package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;

public class InstSemBinaryOp implements InstSemOperand {
	
	public static enum BinaryOperator {
		CMPL_FLOAT,
		CMPG_FLOAT,
		CMPL_DOUBLE,
		CMPG_DOUBLE,
		CMP_LONG,
		
		INSTANCE_OF,
		
		ADD_INT,
		SUB_INT,
		MUL_INT,
		DIV_INT,
		REM_INT,
		AND_INT,
		OR_INT,
		XOR_INT,
		SHL_INT,
		SHR_INT,
		USHR_INT,
		ADD_LONG,
		SUB_LONG,
		MUL_LONG,
		DIV_LONG,
		REM_LONG,
		AND_LONG,
		OR_LONG,
		XOR_LONG,
		SHL_LONG,
		SHR_LONG,
		USHR_LONG,
		ADD_FLOAT,
		SUB_FLOAT,
		MUL_FLOAT,
		DIV_FLOAT,
		REM_FLOAT,
		ADD_DOUBLE,
		SUB_DOUBLE,
		MUL_DOUBLE,
		DIV_DOUBLE,
		REM_DOUBLE
	}
	
	private InstSemElement op1;
	private InstSemElement op2;
	private BinaryOperator operator;
	
	private DexCodeItem codeitem;

	public InstSemBinaryOp(DexCodeItem codeitem, InstSemElement op1, BinaryOperator operator, InstSemElement op2) {
		this.codeitem = codeitem;
		
		this.op1 = op1;
		this.operator = operator;
		this.op2 = op2;
	}

	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public InstSemBinaryOp(DexCodeItem codeitem, InstSemElement op1, int opcode, InstSemElement op2) {
		this.codeitem = codeitem;
		
		this.op1 = op1;
		this.op2 = op2;
		switch (opcode) {
		case 0x90: case 0xb0: case 0xd0: case 0xd8: this.operator = BinaryOperator.ADD_INT; break;
		case 0x91: case 0xb1: this.operator = BinaryOperator.SUB_INT; break;
		case 0xd1: case 0xd9: this.operator = BinaryOperator.SUB_INT; this.op1 = op2; this.op2 = op1; break;		// rsub
		case 0x92: case 0xb2: case 0xd2: case 0xda: this.operator = BinaryOperator.MUL_INT; break;
		case 0x93: case 0xb3: case 0xd3: case 0xdb: this.operator = BinaryOperator.DIV_INT; break;
		case 0x94: case 0xb4: case 0xd4: case 0xdc: this.operator = BinaryOperator.REM_INT; break;
		case 0x95: case 0xb5: case 0xd5: case 0xdd: this.operator = BinaryOperator.AND_INT; break;
		case 0x96: case 0xb6: case 0xd6: case 0xde: this.operator = BinaryOperator.OR_INT; break;
		case 0x97: case 0xb7: case 0xd7: case 0xdf: this.operator = BinaryOperator.XOR_INT; break;
		case 0x98: case 0xb8: case 0xe0: this.operator = BinaryOperator.SHL_INT; break;
		case 0x99: case 0xb9: case 0xe1: this.operator = BinaryOperator.SHR_INT; break;
		case 0x9a: case 0xba: case 0xe2: this.operator = BinaryOperator.USHR_INT; break;
		
		case 0x9b: case 0xbb: this.operator = BinaryOperator.ADD_LONG; break;
		case 0x9c: case 0xbc: this.operator = BinaryOperator.SUB_LONG; break;
		case 0x9d: case 0xbd: this.operator = BinaryOperator.MUL_LONG; break;
		case 0x9e: case 0xbe: this.operator = BinaryOperator.DIV_LONG; break;
		case 0x9f: case 0xbf: this.operator = BinaryOperator.REM_LONG; break;
		case 0xa0: case 0xc0: this.operator = BinaryOperator.AND_LONG; break;
		case 0xa1: case 0xc1: this.operator = BinaryOperator.OR_LONG; break;
		case 0xa2: case 0xc2: this.operator = BinaryOperator.XOR_LONG; break;
		case 0xa3: case 0xc3: this.operator = BinaryOperator.SHL_LONG; break;
		case 0xa4: case 0xc4: this.operator = BinaryOperator.SHR_LONG; break;
		case 0xa5: case 0xc5: this.operator = BinaryOperator.USHR_LONG; break;
		
		case 0xa6: case 0xc6: this.operator = BinaryOperator.ADD_FLOAT; break;
		case 0xa7: case 0xc7: this.operator = BinaryOperator.SUB_FLOAT; break;
		case 0xa8: case 0xc8: this.operator = BinaryOperator.MUL_FLOAT; break;
		case 0xa9: case 0xc9: this.operator = BinaryOperator.DIV_FLOAT; break;
		case 0xaa: case 0xca: this.operator = BinaryOperator.REM_FLOAT; break;

		case 0xab: case 0xcb: this.operator = BinaryOperator.ADD_DOUBLE; break;
		case 0xac: case 0xcc: this.operator = BinaryOperator.SUB_DOUBLE; break;
		case 0xad: case 0xcd: this.operator = BinaryOperator.MUL_DOUBLE; break;
		case 0xae: case 0xce: this.operator = BinaryOperator.DIV_DOUBLE; break;
		case 0xaf: case 0xcf: this.operator = BinaryOperator.REM_DOUBLE; break;
		
		case 0x2d: this.operator = BinaryOperator.CMPL_FLOAT; break;
		case 0x2e: this.operator = BinaryOperator.CMPG_FLOAT; break;
		case 0x2f: this.operator = BinaryOperator.CMPL_DOUBLE; break;
		case 0x30: this.operator = BinaryOperator.CMPG_DOUBLE; break;
		case 0x31: this.operator = BinaryOperator.CMP_LONG; break;
		
		default: assert false;
		}
	}
	
	public InstSemElement getFirstOperand() {
		return op1;
	}
	
	public InstSemElement getSecondOperand() {
		return op2;
	}
	
	public BinaryOperator getOperator() {
		return operator;
	}

	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
		op1.setStatement(statement);
		op2.setStatement(statement);
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
		return op1.getStringRepresentation() + " " + getOperatorStringRepresentation() + " " + op2.getStringRepresentation();
	}
	
	public String getOperatorStringRepresentation() {
		switch (operator) {
		case ADD_DOUBLE:
		case ADD_FLOAT:
		case ADD_INT:
		case ADD_LONG:
			return "+";
		case SUB_DOUBLE:
		case SUB_FLOAT:
		case SUB_INT:
		case SUB_LONG:
			return "-";
		case MUL_DOUBLE:
		case MUL_FLOAT:
		case MUL_INT:
		case MUL_LONG:
			return "*";
		case DIV_DOUBLE:
		case DIV_FLOAT:
		case DIV_INT:
		case DIV_LONG:
			return "/";
		case REM_DOUBLE:
		case REM_FLOAT:
		case REM_INT:
		case REM_LONG:
			return "%";
		case AND_INT:
		case AND_LONG:
			return "&";
		case OR_INT:
		case OR_LONG:
			return "|";
		case XOR_INT:
		case XOR_LONG:
			return "^";
		case SHL_INT:
		case SHL_LONG:
			return "<<";
		case SHR_INT:
		case SHR_LONG:
			return ">>";
		}
		return operator.toString();
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.BINARY_OP;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this, op1, op2 };
	}
}
