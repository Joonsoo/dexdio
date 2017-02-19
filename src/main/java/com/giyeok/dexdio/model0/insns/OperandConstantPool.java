package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexField;
import com.giyeok.dexdio.model0.DexMethod;
import com.giyeok.dexdio.model0.DexType;

public class OperandConstantPool extends Operand {
	private int index;
	private ConstantPoolKind constantKind;

	public OperandConstantPool(DexCodeItem codeitem, int index) {
		super(codeitem);
		
		this.index = index;
		this.constantKind = ConstantPoolKind.UNDEFINED;
	}
	
	public int getValue() {
		return index;
	}
	
	public enum ConstantPoolKind {
		UNDEFINED,
		STRING,
		TYPE,
		FIELD,
		METHOD
	}
	
	void setConstantKind(ConstantPoolKind type) {
		this.constantKind = type;
	}
	
	public ConstantPoolKind getConstantKind() {
		return constantKind;
	}

	@Override
	public String getStringRepresentation() {
		switch (constantKind) {
		case STRING:
			String string = getBelongedCodeItem().getProgram().getStringByStringId(index);
			return "\"" + string + "\"";
		case TYPE:
			DexType dextype = getBelongedCodeItem().getProgram().getTypeByTypeId(index);
			return "type@" + dextype.getTypeFullNameBeauty();
		case FIELD:
			DexField field = getBelongedCodeItem().getProgram().getFieldByFieldId(index);
			return "field@" + field.getBelongedClass().getTypeFullNameBeauty() + "." + field.getName();
		case METHOD:
			DexMethod method = getBelongedCodeItem().getProgram().getMethodByMethodId(index);
			return "method@" + method.getBelongedType().getTypeFullNameBeauty() + "." + method.getName();
		default:
			assert constantKind == ConstantPoolKind.UNDEFINED;
			return "kind@" + Integer.toHexString(index);
		}
	}
}
