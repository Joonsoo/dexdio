package com.giyeok.dexdio.model;

import com.giyeok.dexdio.dexreader.value.Value;

public class DexField {
	private DexProgram program;

	private int fieldId;
	private DexClass belongedClass;
	private DexType type;
	private String name;

	private Value defaultValue;
	private DexAnnotations annotations;
	private DexAccessFlags accessFlags;

	public static enum FieldType {
		INHERITED_FIELD, STATIC_FIELD, INSTANCE_FIELD
	}

	private FieldType fieldType;

	public DexField(DexProgram program, int fieldId, com.giyeok.dexdio.dexreader.FieldTable.Field field)
			throws DexException {
		this.program = program;
		this.belongedClass = null;

		DexType belongedType = program.getTypeByTypeId(field.getClassIdx());

		if (!(belongedType instanceof DexClass)) {
			throw new DexException("Dex field " + field.getName() + " is not belonged to a class");
		}
		this.fieldId = fieldId;
		belongedClass = (DexClass) belongedType;
		type = program.getTypeByTypeId(field.getTypeIdx());
		name = field.getName();

		defaultValue = null;
		annotations = null;

		accessFlags = new DexAccessFlags();
		fieldType = FieldType.INHERITED_FIELD;

		belongedClass.addField(this);
	}

	public DexAccessFlags getAccessFlags() {
		return accessFlags;
	}

	public String getVisibility() {
		StringBuffer buf = new StringBuffer();
		if (accessFlags.isStatic()) {
			buf.append("_");
		}
		if (accessFlags.isPublic()) {
			buf.append("+");
		} else if (accessFlags.isPrivate()) {
			buf.append("-");
		} else if (accessFlags.isProtected()) {
			buf.append("#");
		} else {
			buf.append("~");
		}
		return buf.toString();
	}

	void setDefaultValue(Value value) {
		this.defaultValue = value;
	}

	void setAnnotations(DexAnnotations annotations) {
		this.annotations = annotations;
	}

	void setAccessFlags(int accessFlags) {
		this.accessFlags = new DexAccessFlags(accessFlags);
	}

	void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public int getFieldId() {
		return fieldId;
	}

	public DexClass getBelongedClass() {
		return belongedClass;
	}

	public DexType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public DexAnnotations getAnnotations() {
		return annotations;
	}

	/**
	 * returns field type
	 * 
	 * @return
	 */
	public FieldType getFieldType() {
		return fieldType;
	}

	/**
	 * static_values에서 설정된 기본값을 반환한다. 기본값이 설정되지 않았으면 null을 반환한다.
	 * 
	 * @return
	 */
	public Value getDefaultValue() {
		return defaultValue;
	}
}
