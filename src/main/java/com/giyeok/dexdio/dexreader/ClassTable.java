package com.giyeok.dexdio.dexreader;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.structs.annotation_item;
import com.giyeok.dexdio.dexreader.structs.annotation_off_item;
import com.giyeok.dexdio.dexreader.structs.annotation_set_item;
import com.giyeok.dexdio.dexreader.structs.annotation_set_ref_list;
import com.giyeok.dexdio.dexreader.structs.annotation_set_ref_list.annotation_set_ref_item;
import com.giyeok.dexdio.dexreader.structs.annotations_directory_item;
import com.giyeok.dexdio.dexreader.structs.annotations_directory_item.field_annotation;
import com.giyeok.dexdio.dexreader.structs.annotations_directory_item.method_annotation;
import com.giyeok.dexdio.dexreader.structs.annotations_directory_item.parameter_annotation;
import com.giyeok.dexdio.dexreader.structs.class_data_item;
import com.giyeok.dexdio.dexreader.structs.class_def_item;
import com.giyeok.dexdio.dexreader.structs.code_item;
import com.giyeok.dexdio.dexreader.structs.encoded_array_item;
import com.giyeok.dexdio.dexreader.structs.encoded_method;
import com.giyeok.dexdio.dexreader.structs.header_item;
import com.giyeok.dexdio.dexreader.structs.type_list;
import com.giyeok.dexdio.dexreader.value.Array;

public class ClassTable {
	private StringTable stringTable;
	private TypeTable typeTable;
	private ProtoTable protoTable;
	private FieldTable fieldTable;
	private MethodTable methodTable;
	
	public ClassTable(StringTable stringTable, TypeTable typeTable, ProtoTable protoTable, FieldTable fieldTable, MethodTable methodTable) {
		this.stringTable = stringTable;
		this.typeTable = typeTable;
		this.protoTable = protoTable;
		this.fieldTable = fieldTable;
		this.methodTable = methodTable;
	}
	
	public static class AnnotationsInfo {
		public class Annotations {				// annotation_set_item 에 대응됨
			annotation_item annotations[];
			
			public Annotations(long offset, EndianRandomAccessFile file) throws IOException {
				annotation_set_item annotation_set_item = new annotation_set_item();

				file.seek(offset);
				annotation_set_item.read(file);
				
				int size = annotation_set_item.size();
				annotation_off_item entries[] = annotation_set_item.entries();
				
				annotations = new annotation_item[size];
				
				for (int i = 0; i < size; i++) {
					annotation_item annotation_item = new annotation_item();
					
					file.seek(entries[i].annotation_off());
					annotation_item.read(file);
					
					annotations[i] = annotation_item;
				}
			}
			
			public annotation_item[] annotations() {
				return annotations;
			}
		}
		
		public class FieldAnnotations {
			private int field_idx;
			private Annotations annotations;
			
			public FieldAnnotations(field_annotation field_annotation, EndianRandomAccessFile file) throws IOException {
				field_idx = field_annotation.field_idx();
				annotations = new Annotations(field_annotation.annotations_off(), file);
			}
			
			public int field_idx() {
				return field_idx;
			}
			
			public Annotations annotations() {
				return annotations;
			}
		}
		
		public class MethodAnnotations {
			private int method_idx;
			private Annotations annotations;
			
			public MethodAnnotations(method_annotation method_annotation, EndianRandomAccessFile file) throws IOException {
				method_idx = method_annotation.method_idx();
				annotations = new Annotations(method_annotation.annotations_off(), file);
			}
			
			public int method_idx() {
				return method_idx;
			}
			
			public Annotations annotations() {
				return annotations;
			}
		}
		
		public class ParametersAnnotations {
			private int method_idx;
			private Annotations[] annotations;
			
			public ParametersAnnotations(parameter_annotation parameter_annotation, EndianRandomAccessFile file) throws IOException {
				method_idx = parameter_annotation.method_idx();
				
				annotation_set_ref_list annotation_set_ref_list = new annotation_set_ref_list();

				file.seek(parameter_annotation.annotations_off());
				annotation_set_ref_list.read(file);
				
				int size = annotation_set_ref_list.size();
				annotation_set_ref_item list[] = annotation_set_ref_list.list();
				
				annotations = new Annotations[size];
				
				for (int i = 0; i < size; i++) {
					annotations[i] = new Annotations(list[i].annotations_off(), file);
				}
			}
			
			public int method_idx() {
				return method_idx;
			}
			
			public int length() {
				return annotations.length;
			}
			
			public Annotations get(int i) {
				return annotations[i];
			}
		}
		
		Annotations annotationsOnClass;
		FieldAnnotations annotationsOnFields[];
		MethodAnnotations annotationsOnMethod[];
		ParametersAnnotations annotationsOnParameters[];
		
		public AnnotationsInfo(annotations_directory_item adi, EndianRandomAccessFile file) throws IOException {
			if (adi.class_annotations_off() == 0) {
				annotationsOnClass = null;
			} else {
				annotationsOnClass = new Annotations(adi.class_annotations_off(), file);
			}
			
			int fields_size = adi.fields_size();
			field_annotation field_annotations[] = adi.field_annotations();
			annotationsOnFields = new FieldAnnotations[fields_size];
			for (int i = 0; i < fields_size; i++) {
				annotationsOnFields[i] = new FieldAnnotations(field_annotations[i], file);
			}

			int annotated_methods_size = adi.annotated_methods_size();
			method_annotation method_annotations[] = adi.method_annotations();
			annotationsOnMethod = new MethodAnnotations[annotated_methods_size];
			for (int i = 0; i < annotated_methods_size; i++) {
				annotationsOnMethod[i] = new MethodAnnotations(method_annotations[i], file);
			}
			
			int annotated_parameters_size = adi.annotated_parameters_size();
			parameter_annotation parameter_annotations[] = adi.parameter_annotations();
			annotationsOnParameters = new ParametersAnnotations[annotated_parameters_size];
			for (int i = 0; i < annotated_parameters_size; i++) {
				annotationsOnParameters[i] = new ParametersAnnotations(parameter_annotations[i], file);
			}
		}
		
		public Annotations getAnnotationsOnClass() {
			return annotationsOnClass;
		}
		
		public FieldAnnotations[] getAnnotationsOnFields() {
			return annotationsOnFields;
		}
		
		public MethodAnnotations[] getAnnotationsOnMethods() {
			return annotationsOnMethod;
		}
		
		public ParametersAnnotations[] getAnnotationsOnParameters() {
			return annotationsOnParameters;
		}
	}
	
	public class ClassDef {
		
		private int class_idx;
		private int access_flags;
		private int superclass_idx;
		private int[] interfaces;
		private int source_file_idx;
		private AnnotationsInfo annotations;
		private class_data_item class_data;
		private encoded_array_item static_values;
		
		public ClassDef(int class_idx, int access_flags, int superclass_idx, type_list interfaces, int source_file_idx, 
				AnnotationsInfo annotations, class_data_item class_data, encoded_array_item static_values) {
			this.class_idx = class_idx;
			this.access_flags = access_flags;
			this.superclass_idx = superclass_idx;
			if (interfaces == null) {
				this.interfaces = new int[0];
			} else {
				this.interfaces = interfaces.asIntArray();
			}
			this.source_file_idx = source_file_idx;
			this.annotations = annotations;
			this.class_data = class_data;
			this.static_values = static_values;
		}
		
		public int getClassTypeId() {
			return class_idx;
		}
		
		public String getClassTypeName() {
			return typeTable.getTypeName(class_idx);
		}
		
		public int getAccessFlags() {
			return access_flags;
		}
		
		public int getSuperclassTypeId() {
			return superclass_idx;
		}
		
		public String getSuperclassName() {
			if (superclass_idx < 0) {
				return null;
			}
			return typeTable.getTypeName(superclass_idx);
		}
		
		public int[] getInterfaceTypeIds() {
			return interfaces;
		}
		
		public int getSourceFileNameStringIdx() {
			return source_file_idx;
		}
		
		public String getSourceFileName() {
			if (source_file_idx < 0) {
				return null;
			}
			return stringTable.get(source_file_idx);
		}
		
		public AnnotationsInfo getAnnotations() {
			return annotations;
		}
		
		public class_data_item class_data() {
			return class_data;
		}
		
		public encoded_array_item static_values() {
			return static_values;
		}
	}
	
	public ClassDef getClassDefByTypeId(int typeId) {
		for (ClassDef def: classTable) {
			if (def.getClassTypeId() == typeId) {
				return def;
			}
		}
		return null;
	}
	
	public ClassDef getClassByClassId(int classId) {
		return classTable[classId];
	}
	
	public int size() {
		return classTable.length;
	}
	
	public TypeTable getTypeTable() {
		return typeTable;
	}
	
	public FieldTable getFieldTable() {
		return fieldTable;
	}
	
	public MethodTable getMethodTable() {
		return methodTable;
	}
	
	private ClassDef classTable[];
	
	boolean loadClasses(header_item header, EndianRandomAccessFile file) throws IOException {
		int class_defs_size = (int) header.class_defs_size();
		long class_defs_off = header.class_defs_off();
		
		file.seek(class_defs_off);
		
		Array class_defs = new Array(class_def_item.class, class_defs_size);
		classTable = new ClassDef[class_defs_size];
		
		class_defs.read(file);
		for (int i = 0; i < class_defs_size; i++) {
			class_def_item class_def_item = (class_def_item) class_defs.item(i);
			
			System.out.println("Class #" + i + " " + typeTable.getTypeName((int) class_def_item.class_idx()));

			long interfaces_off = class_def_item.interfaces_off();
			type_list interfaces;
			if (interfaces_off == 0) {
				interfaces = null;
			} else {
				file.seek(interfaces_off);
				interfaces = new type_list();
				interfaces.read(file);
			}
			
			long annotations_off = class_def_item.annotations_off();
			AnnotationsInfo annotations;
			if (annotations_off == 0) {
				annotations = null;
			} else {
				file.seek(annotations_off);
				
				annotations_directory_item annotations_directory_item;
				
				annotations_directory_item = new annotations_directory_item();
				annotations_directory_item.read(file);
				
				annotations = new AnnotationsInfo(annotations_directory_item, file);
			}
			
			long class_data_off = class_def_item.class_data_off();
			class_data_item class_data;
			if (class_data_off == 0) {
				class_data = null;
			} else {
				file.seek(class_data_off);
				class_data = new class_data_item();
				class_data.read(file);
				
				code_item code_item;
				int method_idx;
				method_idx = 0;
				for (encoded_method direct_method: class_data.direct_methods()) {
					method_idx += direct_method.method_idx_diff();
					System.out.println(methodTable.get(method_idx).getClassTypeName() + " " + methodTable.get(method_idx).getName() + " " + direct_method.code_off());

					if (direct_method.code_off() > 0) {
						file.seek(direct_method.code_off());
						code_item = new code_item();
						code_item.read(file);
						direct_method.code_item(code_item);
					}
				}
				method_idx = 0;
				for (encoded_method virtual_method: class_data.virtual_methods()) {
					method_idx += virtual_method.method_idx_diff();
					System.out.println(methodTable.get(method_idx).getClassTypeName() + " " + method_idx + " " + methodTable.get(method_idx).getName() + " " + virtual_method.code_off());
					
					if (virtual_method.code_off() > 0) {
						file.seek(virtual_method.code_off());
						code_item = new code_item();
						code_item.read(file);
						virtual_method.code_item(code_item);
					}
				}
				// TODO debug_info_item 처리
			}
			
			long static_values_off = class_def_item.static_values_off();
			encoded_array_item static_values;
			if (static_values_off == 0) {
				static_values = null;
			} else {
				file.seek(static_values_off);
				static_values = new encoded_array_item();
				static_values.read(file);
			}
			
			classTable[i] = new ClassDef(
					(int) class_def_item.class_idx(),
					(int) class_def_item.access_flags(),
					(int) class_def_item.superclass_idx(),
					interfaces,
					(int) class_def_item.source_file_idx(),
					annotations,
					class_data,
					static_values
					);
		}
		return true;
	}
}

