package com.giyeok.dexdio.model;


public class DexArrayType extends DexMethodContainingType {
	private String elemTypeName;
	private DexType elemType;

	public DexArrayType(DexProgram program, int typeId, String elemTypeName) throws DexException {
		super(program, typeId, "[" + elemTypeName);
		
		this.elemTypeName = elemTypeName;
		
		elemType = program.findTypeByName(elemTypeName);
	}
	
	void setElementType(DexProgram program) throws DexException {
		if (elemType == null) {
			elemType = program.registerExtraType(elemTypeName);
		}
	}
	
	public String getElementTypeName() {
		return elemTypeName;
	}
	
	public DexType getElementType() {
		return elemType;
	}

	@Override
	public String getTypeFullNameBeauty() {
		return elemType.getTypeFullNameBeauty() + "[]";
	}
}
