package com.giyeok.dexdio.dexreader.structs;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;
import com.giyeok.dexdio.dexreader.value.Array;
import com.giyeok.dexdio.dexreader.value.Container;
import com.giyeok.dexdio.dexreader.value.Int;
import com.giyeok.dexdio.dexreader.value.Value;

public class annotations_directory_item extends Value {
	private Int class_annotations_off;
	private Int fields_size;
	private Int annotated_methods_size;
	private Int annotated_parameters_size;
	private Array field_annotations;
	private Array method_annotations;
	private Array parameter_annotations;

	@Override
	public void read(EndianRandomAccessFile stream) throws IOException {
		class_annotations_off = new Int();
		fields_size = new Int();
		annotated_methods_size = new Int();
		annotated_parameters_size = new Int();
		
		class_annotations_off.read(stream);
		fields_size.read(stream);
		annotated_methods_size.read(stream);
		annotated_parameters_size.read(stream);
		
		field_annotations = new Array(field_annotation.class, fields_size.getValue());
		method_annotations = new Array(method_annotation.class, annotated_methods_size.getValue());
		parameter_annotations = new Array(parameter_annotation.class, annotated_parameters_size.getValue());
		
		field_annotations.read(stream);
		method_annotations.read(stream);
		parameter_annotations.read(stream);
	}
	
	public long class_annotations_off() {
		return class_annotations_off.getUnsignedValue();
	}
	
	public int fields_size() {
		return fields_size.getValue();
	}
	
	public field_annotation[] field_annotations() {
		return field_annotations.asArray(new field_annotation[0]);
	}
	
	public int annotated_methods_size() {
		return annotated_methods_size.getValue();
	}
	
	public method_annotation[] method_annotations() {
		return method_annotations.asArray(new method_annotation[0]);
	}
	
	public int annotated_parameters_size() {
		return annotated_parameters_size.getValue();
	}
	
	public parameter_annotation[] parameter_annotations() {
		return parameter_annotations.asArray(new parameter_annotation[0]);
	}
	
	public static class field_annotation extends Container {
	
		public field_annotation() {
			super(new NamedValue[] {
					new NamedValue("field_idx", new Int()),
					new NamedValue("annotations_off", new Int())
			});
		}
		
		public int field_idx() {
			return ((Int) find("field_idx")).getValue();
		}
		
		public long annotations_off() {
			return ((Int) find("annotations_off")).getValue();
		}
	}
	
	public static class method_annotation extends Container {
	
		public method_annotation() {
			super(new NamedValue[] {
					new NamedValue("method_idx", new Int()),
					new NamedValue("annotations_off", new Int())
			});
		}

		public int method_idx() {
			return ((Int) find("method_idx")).getValue();
		}
		
		public long annotations_off() {
			return ((Int) find("annotations_off")).getValue();
		}
	}
	
	public static class parameter_annotation extends Container {
	
		public parameter_annotation() {
			super(new NamedValue[] {
					new NamedValue("method_idx", new Int()),
					new NamedValue("annotations_off", new Int())
			});
		}

		public int method_idx() {
			return ((Int) find("method_idx")).getValue();
		}
		
		public long annotations_off() {
			return ((Int) find("annotations_off")).getValue();
		}
	}

	@Override
	public int getByteLength() {
		return class_annotations_off.getByteLength() + fields_size.getByteLength() +
				annotated_methods_size.getByteLength() + annotated_parameters_size.getByteLength() +
				field_annotations.getByteLength() + method_annotations.getByteLength() +
				parameter_annotations.getByteLength();
	}
}
