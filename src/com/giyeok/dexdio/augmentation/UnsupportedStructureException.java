package com.giyeok.dexdio.augmentation;

public class UnsupportedStructureException extends Exception {
	private static final long serialVersionUID = -6620083850456365668L;
	
	private String message;

	public UnsupportedStructureException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
