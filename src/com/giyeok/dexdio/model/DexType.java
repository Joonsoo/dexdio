package com.giyeok.dexdio.model;



public abstract class DexType {
	private DexProgram program;
	private int typeId;
	private String typeName;
	
	public DexType(DexProgram program, int typeId, String typeName) {
		this.program = program;
		this.typeId = typeId;
		this.typeName = typeName;
	}
	
	public DexProgram getProgram() {
		return program;
	}
	
	public int getTypeId() {
		return typeId;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public String getTypeFullNameBeauty() {
		return typeName;
	}
	
	public String getTypeShortNameBeauty() {
		return typeName;
	}
	
	public abstract boolean isWide();
}
