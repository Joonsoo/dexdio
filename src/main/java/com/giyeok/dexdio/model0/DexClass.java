package com.giyeok.dexdio.model0;

import java.util.ArrayList;

import com.giyeok.dexdio.Utils;


public abstract class DexClass extends DexMethodContainingType {
	private ArrayList<DexField> fields;
	
	public DexClass(DexProgram program, int typeId, String typeName) {
		super(program, typeId, typeName);
		
		fields = new ArrayList<DexField>();
	}
	
	void addField(DexField field) {
		fields.add(field);
	}
	
	public DexField[] getFields() {
		return fields.toArray(new DexField[0]);
	}
	
	public boolean isJavaLangObject() {
		return getTypeName().equals("Ljava/lang/Object;");
	}
	
	/**
	 * 이 클래스가 descendant의 계보에 포함된 ancestor 클래스이면 true를 반환한다.
	 * 모든 클래스는 스스로의 ancestor이고, 클래스 계보도에는 인터페이스도 고려된다
	 * external class끼리의 비교도 가능하도록 수정해야함
	 * @param descendant
	 * @return
	 */
	public boolean isAncestorOf(DexInternalClass descendant) {
		if (descendant == this) {
			return true;
		}
		DexClass parent = descendant.getSuperClass();
		if (parent == this) {
			return true;
		} else if (parent instanceof DexInternalClass) {
			if (this.isAncestorOf((DexInternalClass) parent)) {
				return true;
			} else {
				/*
				DexClass impls[] = ((DexInternalClass) parent).getImplementingInterfaces();
				for (DexClass impl: impls) {
					if (impl.isAncestorOf(descendant)) {
						return true;
					}
				}
				*/
				return false;
			}
		} else {
			return false;
		}
	}
	
	private String packageName = null, className = null;
	
	public String getPackageName() {
		// TODO 패키지 이름이 없는 클래스는 어떻게 되는지 확인
		if (packageName == null) {
			String typeName = getTypeName();
			packageName = Utils.joinStrings(".", typeName.substring(1, typeName.lastIndexOf('/')).split("/"));
		}
		return packageName;
	}
	
	public String getClassName() {
		if (className == null) {
			String typeName = getTypeName();
			className = typeName.substring(typeName.lastIndexOf('/') + 1, typeName.length() - 1);
		}
		return className;
	}
	
	@Override
	public String getTypeFullNameBeauty() {
		// TODO 패키지 이름이 없는 클래스는 어떻게 되는지 확인
		return getPackageName() + "." + getClassName();
	}

	@Override
	public String getTypeShortNameBeauty() {
		return getClassName();
	}
	
	abstract void bondWithOthers() throws DexException;
}
