package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexArrayType;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.util.ArraysUtil;

public class InstSemNewArray implements InstSemOperand {
	
	private DexType type;
	private InstSemRegister size;
	private InstSemRegister[] items;
	
	private DexCodeItem codeitem;

	public InstSemNewArray(DexCodeItem codeitem, DexType type, InstSemRegister size) {
		assert type instanceof DexArrayType;
		
		this.codeitem = codeitem;
		
		this.type = type;
		this.size = size;
		this.items = null;
	}
	
	public InstSemNewArray(DexCodeItem codeitem, DexType type, InstSemRegister items[]) {
		assert type instanceof DexArrayType;
		
		this.codeitem = codeitem;
		
		this.type = type;
		this.size = null;
		this.items = items;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public DexArrayType getType() {
		return (DexArrayType) type;
	}
	
	public InstSemRegister getSize() {
		return size;
	}
	
	public InstSemRegister[] getItems() {
		return items;
	}

	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
		
		if (size == null) {
			for (InstSemRegister item: items) {
				item.setStatement(statement);
			}
		} else {
			size.setStatement(statement);
		}
	}

	@Override
	public InstSemStatement getStatement() {
		return this.statement;
	}
	
	private DexType operandType;

	@Override
	public void setOperandTypeData(DexType operandType) {
		this.operandType = operandType;
	}

	@Override
	public DexType getOperandTypeData() {
		return operandType;
	}

	@Override
	public String getStringRepresentation() {
		if (size == null) {
			StringBuffer buf = new StringBuffer();
			
			buf.append("new ");
			buf.append(type.getTypeFullNameBeauty());
			buf.append("[] {");
			for (int i = 0; i < items.length; i++) {
				if (i > 0) {
					buf.append(", ");
				}
				buf.append(items[i].getStringRepresentation());
			}
			buf.append("}");
			return buf.toString();
		} else {
			return "new " + type.getTypeFullNameBeauty() + "[" + size.getStringRepresentation() + "]";
		}
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.NEW_ARRAY;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		if (size != null) {
			return new InstSemOperand[] { this, size };
		} else {
			return ArraysUtil.concat(new InstSemOperand[] { this }, items);
		}
	}
}
