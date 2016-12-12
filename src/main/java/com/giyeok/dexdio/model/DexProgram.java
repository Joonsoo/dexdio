package com.giyeok.dexdio.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.giyeok.dexdio.dexreader.ClassTable;
import com.giyeok.dexdio.dexreader.ClassTable.ClassDef;
import com.giyeok.dexdio.dexreader.DalvikExecutable;
import com.giyeok.dexdio.dexreader.FieldTable;
import com.giyeok.dexdio.dexreader.MethodTable;
import com.giyeok.dexdio.dexreader.ProtoTable;
import com.giyeok.dexdio.dexreader.StringTable;
import com.giyeok.dexdio.dexreader.TypeTable;


public class DexProgram {

	private int typeCount;
	private DexType[] types;
	private DexField[] fields;
	private DexMethod[] methods;
	
	private StringTable stringTable;
	private TypeTable typeTable;
	private ProtoTable protoTable;
	private FieldTable fieldTable;
	private MethodTable methodTable;
	private ClassTable classTable;
	
	public DexProgram(DalvikExecutable dex) throws DexException {
		this.stringTable = dex.getStringTable();
		this.typeTable = dex.getTypeTable();
		this.protoTable = dex.getProtoTable();
		this.fieldTable = dex.getFieldTable();
		this.methodTable = dex.getMethodTable();
		this.classTable = dex.getClassTable();
		
		primTypes = new HashMap<Character, DexPrimitiveType>();
		
		// 타입 등록
		typeCount = typeTable.size();
		types = new DexType[typeCount];
		for (int i = 0; i < typeCount; i++) {
			String typeName = typeTable.getTypeName(i);
			char typeChar = typeName.charAt(0);
			switch (typeChar) {
			case 'V':
			case 'Z': case 'B': case 'S': case 'C':
			case 'I': case 'J': case 'F': case 'D':
				assert typeName.length() == 1;
				types[i] = new DexPrimitiveType(this, i, typeName);
				primTypes.put(typeChar, (DexPrimitiveType) types[i]);
				break;
			case 'L':
				ClassDef def = classTable.getClassDefByTypeId(i);
				
				if (def != null) {
					assert def.getClassTypeId() == i;
					assert typeTable.getTypeName(def.getClassTypeId()).equals(typeName);
					types[i] = new DexInternalClass(this, def);
				} else {
					types[i] = new DexExternalClass(this, i, typeName);
				}

				registerDefaultClasses(typeName, types[i]);
				break;
			case '[':
				DexArrayType arrayType = new DexArrayType(this, i, typeName.substring(1));
				types[i] = arrayType;
				break;
			default:
				throw new DexException("Unknown type: " + typeName);
			}
		}
		
		int k = 0;
		for (k = 0; k < types.length; k++) {
			if (types[k] instanceof DexArrayType) {
				((DexArrayType) types[k]).setElementType(this);
			}
		}
		assert k == types.length;
		
		// 필드 등록
		fields = new DexField[fieldTable.size()];
		for (int i = 0; i < fieldTable.size(); i++) {
			fields[i] = new DexField(this, i, fieldTable.get(i));
		}
		
		// 메소드 등록
		methods = new DexMethod[methodTable.size()];
		for (int i = 0; i < methodTable.size(); i++) {
			methods[i] = new DexMethod(this, i, methodTable.get(i));
		}
		
		// 클래스로부터 각 요소 연결
		for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof DexClass) {
				((DexClass) types[i]).bondWithOthers();
			}
		}
	}
	
	public String getTypeNameByTypeId(int typeId) {
		return typeTable.getTypeName(typeId);
	}
	
	public String getStringByStringId(int stringId) {
		return stringTable.get(stringId);
	}
	
	public int getTypesCount() {
		return typeCount;
	}
	
	public DexType getTypeByTypeId(int typeId) {
		return types[typeId];
	}
	
	public DexType findTypeByName(String typeName) {
		for (int i = 0; i < types.length; i++) {
			if (types[i] != null) {
				if (types[i].getTypeName().equals(typeName)) {
					return types[i];
				}
			}
		}
		return null;
	}
	
	private void registerDefaultClasses(String typeName, DexType type) {
		// 상시 사용 클래스 등록
		if (typeName.equals("Ljava/lang/String;")) {
			stringClass = (DexClass) type;
		}
	}

	public DexType registerExtraType(String typeName) throws DexException {
		types = Arrays.copyOf(types, types.length + 1);
		int i = types.length - 1;
		char typeChar = typeName.charAt(0);
		switch (typeChar) {
		case 'V':
		case 'Z': case 'B': case 'S': case 'C':
		case 'I': case 'J': case 'F': case 'D':
			assert typeName.length() == 1;
			types[i] = new DexPrimitiveType(this, i, typeName);
			primTypes.put(typeChar, (DexPrimitiveType) types[i]);
			return types[i];
		case 'L':
			types[i] = new DexExternalClass(this, i, typeName);

			registerDefaultClasses(typeName, types[i]);
			return types[i];
		case '[':
			types[i] = new DexArrayType(this, i, typeName.substring(1));
			return types[i];
		default:
			throw new DexException("Unknown type: " + typeName);
		}
	}
	
	public DexField getFieldByFieldId(int fieldId) {
		return fields[fieldId];
	}
	
	public int getMethodsCount() {
		return methods.length;
	}
	
	public DexMethod getMethodByMethodId(int methodId) {
		return methods[methodId];
	}
	
	private DexClass stringClass;
	private Map<Character, DexPrimitiveType> primTypes;
	
	public DexClass getStringClassType() {
		return stringClass;
	}

	private DexPrimitiveType getPrimitiveType(char type) {
		DexPrimitiveType primType = primTypes.get(type);
		
		if (primType == null) {
			try {
				return (DexPrimitiveType) registerExtraType("" + type);
			} catch (DexException e) {
				assert false;
			}
		}
		return primType;
	}
	
	public DexPrimitiveType getIntegerPrimitiveType() {
		return getPrimitiveType('I');
	}

	public DexType getLongPrimitiveType() {
		return getPrimitiveType('J');
	}
	
	public DexType getFloatPrimitiveType() {
		return getPrimitiveType('F');
	}
	
	public DexType getDoublePrimitiveType() {
		return getPrimitiveType('D');
	}

	public DexType getBooleanPrimitiveType() {
		return getPrimitiveType('Z');
	}

	public DexType getShortPrimitiveType() {
		return getPrimitiveType('S');
	}
	
	public DexType getBytePrimitiveType() {
		return getPrimitiveType('B');
	}

	public DexType getCharPrimitiveType() {
		return getPrimitiveType('C');
	}
}
