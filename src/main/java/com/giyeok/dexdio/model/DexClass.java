package com.giyeok.dexdio.model;

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
	 * �� Ŭ������ descendant�� �躸�� ���Ե� ancestor Ŭ�����̸� true�� ��ȯ�Ѵ�.
	 * ��� Ŭ������ �������� ancestor�̰�, Ŭ���� �躸������ �������̽��� ����ȴ�
	 * external class������ �񱳵� �����ϵ��� �����ؾ���
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
		// TODO ��Ű�� �̸��� ���� Ŭ������ ��� �Ǵ��� Ȯ��
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
		// TODO ��Ű�� �̸��� ���� Ŭ������ ��� �Ǵ��� Ȯ��
		return getPackageName() + "." + getClassName();
	}

	@Override
	public String getTypeShortNameBeauty() {
		return getClassName();
	}
	
	abstract void bondWithOthers() throws DexException;
}
