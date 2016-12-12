package com.giyeok.dexdio.model;


public class DexExternalClass extends DexClass {
	public DexExternalClass(DexProgram program, int typeId, String typeName) {
		super(program, typeId, typeName);
	}
	
	@Override
	void bondWithOthers() {
	}
}
