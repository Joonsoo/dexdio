package com.giyeok.dexdio.model0;

import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;


public class DexParameter {
	private DexType type;
	private DexAnnotations annotations;
	
	private DexRegister register;

	public DexParameter(DexType type) {
		this.type = type;
		this.annotations = null;
		
		this.register = null;
	}
	
	public boolean isWide() {
		return type.isWide();
	}
	
	void setAnnotations(DexAnnotations annotations) {
		this.annotations = annotations;
	}
	
	public DexType getType() {
		return type;
	}
	
	public DexAnnotations getAnnotations() {
		return annotations;
	}
	
	void setRegister(DexRegister register) {
		this.register = register;
	}
	
	public DexRegister getRegister() {
		return register;
	}

	public String getStringRepresentation() {
		if (isWide()) {
			return "{" + register.getName() + ":" + register.getNextRegister().getName() + "}";
		} else {
			return register.getName();
		}
	}
}
