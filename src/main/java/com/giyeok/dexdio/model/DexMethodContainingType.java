package com.giyeok.dexdio.model;

import java.util.ArrayList;

public class DexMethodContainingType extends DexType {
	private ArrayList<DexMethod> methods;

	public DexMethodContainingType(DexProgram program, int typeId, String typeName) {
		super(program, typeId, typeName);
		
		methods = new ArrayList<DexMethod>();
	}
	
	void addMethod(DexMethod method) {
		methods.add(method);
	}
	
	public DexMethod[] getMethods() {
		return methods.toArray(new DexMethod[0]);
	}

	@Override
	public boolean isWide() {
		return false;
	}
}
