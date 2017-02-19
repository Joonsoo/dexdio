package com.giyeok.dexdio.model0;

import java.util.ArrayList;

import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo;
import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo.FieldAnnotations;
import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo.MethodAnnotations;
import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo.ParametersAnnotations;
import com.giyeok.dexdio.dexreader.ClassTable.ClassDef;
import com.giyeok.dexdio.dexreader.structs.class_data_item;
import com.giyeok.dexdio.dexreader.structs.code_item;
import com.giyeok.dexdio.dexreader.structs.encoded_array;
import com.giyeok.dexdio.dexreader.structs.encoded_array_item;
import com.giyeok.dexdio.dexreader.structs.encoded_field;
import com.giyeok.dexdio.dexreader.structs.encoded_method;
import com.giyeok.dexdio.dexreader.structs.encoded_value;
import com.giyeok.dexdio.model0.DexField.FieldType;
import com.giyeok.dexdio.model0.DexMethod.MethodKind;
import com.giyeok.dexdio.util.ArraysUtil;

public class DexInternalClass extends DexClass {
	private DexAccessFlags accessFlags;
	private DexClass superclass;
	private DexClass[] interfaces;
	
	private DexField[] staticFields;
	private DexField[] instanceFields;
	private DexField[] inheritedFields;
	private DexMethod[] directMethods;
	private DexMethod[] virtualMethods;
	private DexMethod[] inheritedMethods;
	
	private DexField[] allFields;
	private DexMethod[] allMethods;
	
	private DexAnnotations annotations;
	
	private ClassDef def;

	DexInternalClass(DexProgram program, ClassDef def) {
		super(program, def.getClassTypeId(), def.getClassTypeName());
		
		this.def = def;
		this.accessFlags = new DexAccessFlags(def.getAccessFlags());
		
		this.staticFields = null;
		this.instanceFields = null;
		this.inheritedFields = null;
		this.directMethods = null;
		this.virtualMethods = null;
		this.inheritedMethods = null;
		
		this.annotations = null;
	}
	
	@Override
	public DexField[] getFields() {
		return allFields;
	}
	
	@Override
	public DexMethod[] getMethods() {
		return allMethods;
	}
	
	public DexClass getSuperClass() {
		return superclass;
	}
	
	public DexClass[] getImplementingInterfaces() {
		return interfaces;
	}
	
	public DexAnnotations getAnnotations() {
		return annotations;
	}
	
	public DexAccessFlags getAccessFlags() {
		return accessFlags;
	}
	
	public boolean isEnum() {
		return accessFlags.isEnum();
	}
	
	public boolean isInterface() {
		return accessFlags.isInterface();
	}
	
	public boolean isClass() {
		return (! isEnum()) && (! isInterface());
	}
	
	private DexField[] bondWithFields(encoded_field[] fields, FieldType fieldType) throws DexException {
		DexField result[];
		
		result = new DexField[fields.length];
		int fieldId = 0;
		for (int i = 0; i < fields.length; i++) {
			fieldId += fields[i].field_idx_diff();
			result[i] = getProgram().getFieldByFieldId(fieldId);
			if (result[i].getBelongedClass() != this) {
				throw new DexException("The belonged class of field " + fieldId + " is inconsistent with class " + getTypeName());
			}
			result[i].setAccessFlags(fields[i].access_flags());
			result[i].setFieldType(fieldType);
		}
		return result;
	}
	
	private DexMethod[] bondWithMethods(encoded_method[] methods, MethodKind methodType) throws DexException {
		DexMethod result[];
		
		result = new DexMethod[methods.length];
		int directMethodId = 0;
		for (int i = 0; i < methods.length; i++) {
			directMethodId += methods[i].method_idx_diff();
			result[i] = getProgram().getMethodByMethodId(directMethodId);
			if (result[i].getBelongedType() != this) {
				throw new DexException("The belonged type of method " + directMethodId + " is inconsistent with class " + getTypeName());
			}
			result[i].setAccessFlags(methods[i].access_flags());
			result[i].setMethodKind(methodType);
			code_item ci = methods[i].code_item();
			if (ci != null) {
				result[i].setCodeItem(new DexCodeItem(getProgram(), result[i], ci));
			}
		}
		return result;
	}

	@Override
	void bondWithOthers() throws DexException {
		// superclass
		if (def.getSuperclassTypeId() < 0) {
			superclass = null;
		} else {
			DexType supertype = getProgram().getTypeByTypeId(def.getSuperclassTypeId());
			
			if (! (supertype instanceof DexClass)) {
				throw new DexException("The class " + getTypeName() + " extended from non-class type " + supertype.getTypeName());
			}
			superclass = (DexClass) supertype;
		}
		
		// implementing interfaces
		int[] interfaces = def.getInterfaceTypeIds();
		assert interfaces != null;
		this.interfaces = new DexClass[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			DexType interfaceType = getProgram().getTypeByTypeId(interfaces[i]);
			
			if (! (interfaceType instanceof DexClass)) {
				throw new DexException("The class " + getTypeName() + " implements non-class type " + interfaceType.getTypeName());
			}
			this.interfaces[i] = (DexClass) interfaceType;
		}
		
		class_data_item data = def.class_data();
		
		encoded_field[] fields;
		encoded_method[] methods;

		if (data != null) { 
			// fields
			fields = data.static_fields();
			assert fields.length == data.static_fields_size();
			staticFields = bondWithFields(fields, FieldType.STATIC_FIELD);
			
			fields = data.instance_fields();
			assert fields.length == data.instance_fields_size();
			instanceFields = bondWithFields(fields, FieldType.INSTANCE_FIELD);

			// methods
			methods = data.direct_methods();
			assert methods.length == data.direct_methods_size();
			directMethods = bondWithMethods(methods, MethodKind.DIRECT_METHOD);
			
			methods = data.virtual_methods();
			assert methods.length == data.virtual_methods_size();
			virtualMethods = bondWithMethods(methods, MethodKind.VIRTUAL_METHOD);
		} else {
			staticFields = new DexField[0];
			instanceFields = new DexField[0];
			directMethods = new DexMethod[0];
			virtualMethods = new DexMethod[0];
		}
		
		// find inherited fields
		ArrayList<DexField> inheritedFields = new ArrayList<DexField>();
		for (DexField field: super.getFields()) {
			if (ArraysUtil.indexOf(staticFields, field) < 0 && ArraysUtil.indexOf(instanceFields, field) < 0) {
				inheritedFields.add(field);
			}
		}
		this.inheritedFields = inheritedFields.toArray(new DexField[0]);
		
		// find inherited methods
		ArrayList<DexMethod> inheritedMethods = new ArrayList<DexMethod>();
		for (DexMethod method: super.getMethods()) {
			if (ArraysUtil.indexOf(directMethods, method) < 0 && ArraysUtil.indexOf(virtualMethods, method) < 0) {
				inheritedMethods.add(method);
			}
		}
		this.inheritedMethods = inheritedMethods.toArray(new DexMethod[0]);

		// static values
		encoded_array_item static_values_item = def.static_values();
		if (static_values_item != null) {
			encoded_array static_values = static_values_item.value();
			encoded_value[] values = static_values.values();
			assert values.length == static_values.size();
			assert values.length <= staticFields.length;
			for (int i = 0; i < values.length; i++) {
				staticFields[i].setDefaultValue(values[i].getValue());
			}
		}
		
		// all fields & all methods
		allFields = new DexField[staticFields.length + instanceFields.length + this.inheritedFields.length];
		System.arraycopy(staticFields, 0, allFields, 0, staticFields.length);
		System.arraycopy(instanceFields, 0, allFields, staticFields.length, instanceFields.length);
		System.arraycopy(this.inheritedFields, 0, allFields, staticFields.length + instanceFields.length, this.inheritedFields.length);

		allMethods = new DexMethod[directMethods.length + virtualMethods.length + this.inheritedMethods.length];
		System.arraycopy(directMethods, 0, allMethods, 0, directMethods.length);
		System.arraycopy(virtualMethods, 0, allMethods, directMethods.length, virtualMethods.length);
		System.arraycopy(this.inheritedMethods, 0, allMethods, directMethods.length + virtualMethods.length, this.inheritedMethods.length);
		
		// annotations
		AnnotationsInfo annotations = def.getAnnotations();
		if (annotations != null) {
			if (annotations.getAnnotationsOnClass() == null) {
				this.annotations = null;
			} else {
				this.annotations = new DexAnnotations(getProgram(), annotations.getAnnotationsOnClass());
			}
			
			FieldAnnotations fieldAnnotations[] = annotations.getAnnotationsOnFields();
			for (int i = 0; i < fieldAnnotations.length; i++) {
				FieldAnnotations fa = fieldAnnotations[i];
				DexField targetField = getProgram().getFieldByFieldId(fa.field_idx());
				if (targetField.getBelongedClass() != this) {
					throw new DexException("Annotation info for field" + targetField.getFieldId() + " " + targetField.getName() + " is in wrong place: " + getTypeName());
				}
				targetField.setAnnotations(new DexAnnotations(getProgram(), fa.annotations()));
			}
			
			MethodAnnotations methodAnnotations[] = annotations.getAnnotationsOnMethods();
			for (int i = 0; i < methodAnnotations.length; i++) {
				MethodAnnotations ma = methodAnnotations[i];
				DexMethod targetMethod = getProgram().getMethodByMethodId(ma.method_idx());
				if (targetMethod.getBelongedType() != this) {
					throw new DexException("Annotation info for method " + targetMethod.getMethodId() + " " + targetMethod.getName() + " is in wrong place: " + getTypeName());
				}
				targetMethod.setAnnotations(new DexAnnotations(getProgram(), ma.annotations()));
			}
			
			ParametersAnnotations parameterAnnotations[] = annotations.getAnnotationsOnParameters();
			for (int i = 0; i < parameterAnnotations.length; i++) {
				ParametersAnnotations pa = parameterAnnotations[i];
				DexMethod targetMethod = getProgram().getMethodByMethodId(pa.method_idx());
				if (targetMethod.getBelongedType() != this) {
					throw new DexException("Annotation info for method " + targetMethod.getMethodId() + " " + targetMethod.getName() + " is in wrong place: " + getTypeName());
				}
				DexParameter params[] = targetMethod.getParameters();
				if (params.length != pa.length()) {
					throw new DexException("Parameter annotation for " + targetMethod.getMethodId() + " " + targetMethod.getName() + " has wrong number of info");
				}
				for (int j = 0; j < params.length; j++) {
					params[j].setAnnotations(new DexAnnotations(getProgram(), pa.get(j)));
				}
			}
		}
	}
}
