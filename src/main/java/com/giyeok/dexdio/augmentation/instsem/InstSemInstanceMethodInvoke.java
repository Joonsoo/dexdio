package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.util.ArraysUtil;

public class InstSemInstanceMethodInvoke extends InstSemStatement implements InstSemOperand {
	
	private InstSemRegister instance;
	private DexMethod method;
	private InstSemRegister[] args;
	
	private DexCodeItem codeitem;

	public InstSemInstanceMethodInvoke(DexCodeItem codeitem, InstSemRegister instance, DexMethod method, InstSemRegister[] args) {
		this.codeitem = codeitem;
		
		this.instance = instance;
		this.method = method;
		this.args = args;
		
		// 이 객체가 statement가 아닌 operand로 사용된 것이면 뒤에 setStatement가 호출될 때 statement가 다시 설정된다
		this.statement = this;
		instance.setStatement(this);
		for (InstSemRegister reg: args) {
			reg.setStatement(this);
		}
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public InstSemRegister getInstance() {
		return instance;
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

		instance.setStatement(statement);
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
		
		buf.append("((");
		buf.append(method.getBelongedType().getTypeFullNameBeauty());
		buf.append(")");
		buf.append(instance.getStringRepresentation());
		buf.append(").");
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
		return InstSemType.INSTANCE_METHOD_INVOKE;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return ArraysUtil.concat(new InstSemOperand[] { this, instance }, args);
	}
}
