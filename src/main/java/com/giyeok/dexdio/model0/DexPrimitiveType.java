package com.giyeok.dexdio.model0;


public class DexPrimitiveType extends DexType {
	private char typeChar;

	public DexPrimitiveType(DexProgram program, int typeId, String typeName) {
		super(program, typeId, typeName);
		
		assert typeName.length() == 1;
		typeChar = typeName.charAt(0);
	}

	@Override
	public String getTypeFullNameBeauty() {
		switch (typeChar) {
		case 'V':
			return "void";
		case 'Z':
			return "boolean";
		case 'B':
			return "byte";
		case 'S':
			return "short";
		case 'C':
			return "char";
		case 'I':
			return "int";
		case 'J':
			return "long";
		case 'F':
			return "float";
		case 'D':
			return "double";
		}
		assert false;
		return null;
	}
	
	@Override
	public String getTypeShortNameBeauty() {
		return getTypeFullNameBeauty();
	}

	@Override
	public boolean isWide() {
		return typeChar == 'J' || typeChar == 'D';
	}
}
