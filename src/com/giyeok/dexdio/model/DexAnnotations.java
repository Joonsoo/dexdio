package com.giyeok.dexdio.model;

import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo.Annotations;
import com.giyeok.dexdio.dexreader.structs.annotation_item;
import com.giyeok.dexdio.dexreader.value.Value;

public class DexAnnotations {

	private DexProgram program;
	private DexAnnotationItem items[];
	
	public DexAnnotations(DexProgram program, Annotations annotations) {
		annotation_item items[] = annotations.annotations();
		
		this.program = program;
		this.items = new DexAnnotationItem[items.length];
		
		for (int i = 0; i < items.length; i++) {
			this.items[i] = new DexAnnotationItem(items[i]);
			
			/**
			System.out.print(items[i].visibility() + " ");
			
			encoded_annotation annotation = items[i].annotation();
			
			System.out.println(program.getTypeNameByTypeId(annotation.type_idx()) + " " + annotation.size());
			for (annotation_element element: annotation.elements()) {
				System.out.println(program.getStringByStringId(element.name_idx()) + ":" + element.value().getValue());
			}
			*/
		}
	}
	
	public int getAnnotationsCount() {
		return items.length;
	}
	
	public DexAnnotationItem getAnnotationItem(int i) {
		return items[i];
	}
	
	public class DexAnnotationItem {
		private annotation_item item;
		
		public DexAnnotationItem(annotation_item item) {
			this.item = item;
		}
		
		public int getVisibility() {
			return item.visibility();
		}
		
		public DexType getAnnotationType() {
			return program.getTypeByTypeId(item.annotation().type_idx());
		}
		
		public int getAnnotationSize() {
			assert item.annotation().elements().length == item.annotation().size();
			return item.annotation().size();
		}
		
		public String getAnnotationKey(int i) {
			return program.getStringByStringId(item.annotation().elements()[i].name_idx());
		}
		
		public Value getAnnotationValue(int i) {
			return item.annotation().elements()[i].value().getValue();
		}
	}
}
