package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.util.ArraysUtil;

public class InstSemStaticMethodInvoke extends InstSemStatement implements InstSemOperand {
	
	private DexMethod method;
	private InstSemRegister[] args;
	
	private DexCodeItem codeitem;

	public InstSemStaticMethodInvoke(DexCodeItem codeitem, DexMethod method, InstSemRegister[] args) {
		this.codeitem = codeitem;
		
		this.method = method;
		this.args = args;

		// �� ��ü�� statement�� �ƴ� operand�� ���� ���̸� �ڿ� setStatement�� ȣ��� �� statement�� �ٽ� �����ȴ�
		for (InstSemRegister reg: args) {
			reg.setStatement(this);
		}
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public DexMethod getMethod() {
		return method;
	}
	
	public InstSemRegister[] getArguments() {
		return args;
	}

	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
		for (InstSemRegister reg: args) {
			reg.setStatement(statement);
		}
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
		StringBuffer buf = new StringBuffer();

		buf.append(method.getBelongedType().getTypeFullNameBeauty());
		buf.append(".");
		buf.append(method.getName());
		buf.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(args[i].getStringRepresentation());
		}
		buf.append(")");
		return buf.toString();
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.STATIC_METHOD_INVOKE;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return ArraysUtil.concat(new InstSemOperand[] { this }, args);
	}
}
