package com.giyeok.dexdio.views.classdetailview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.giyeok.dexdio.augmentation.DataFlowAnalyzer;
import com.giyeok.dexdio.augmentation.DeadCodeCollector;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer;
import com.giyeok.dexdio.dexreader.value.Value;
import com.giyeok.dexdio.model.DexAccessFlags;
import com.giyeok.dexdio.model.DexAnnotations;
import com.giyeok.dexdio.model.DexAnnotations.DexAnnotationItem;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexInternalClass;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.OccurrenceMarkingClickListener.ColorType;
import com.giyeok.dexdio.widgets.GroupedLabelListWidget;
import com.giyeok.dexdio.widgets.Label;
import com.giyeok.dexdio.widgets.Label.TextLabel;
import com.giyeok.dexdio.widgets.LabelListWidget;

/**
 * 클래스의 내용을 텍스트로 보이는 리스트.
 * 텍스트로 복사(나중에 출력) 기능도 추가!
 * @author joonsoo
 *
 */
public class ClassContentView extends GroupedLabelListWidget {

	private DexClassDetailViewer controller;
	
	private DexClass showingClass;
	
	public ClassContentView(Composite parent, int style, DexClassDetailViewer controller) {
		super(parent, style, "Class");
		
		this.controller = controller;
		
		this.items = new ArrayList<Label>();
		
		this.showingClass = null;
		
		fieldLabels = new HashMap<DexField, Set<Label>>();
		methodLabels = new HashMap<DexMethod, Set<Label>>();
		
		fieldsToGroup = new HashMap<DexField, GroupedLabelListWidget.ItemGroup>();
		methodsToGroup = new HashMap<DexMethod, GroupedLabelListWidget.ItemGroup>();
		
		methodContentViewStyle = MethodContentViewStyle.INSTSEM_REPLACED_ALIVES;
	}
	
	private ArrayList<Label> items;
	private Map<DexField, ItemGroup> fieldsToGroup;
	private Map<DexMethod, ItemGroup> methodsToGroup;
	
	private InstructionSemanticizer semantics;
	private DataFlowAnalyzer constants;
	private DeadCodeCollector deads;
	
	private MethodContentViewStyle methodContentViewStyle;
	public static enum MethodContentViewStyle {
		INSTRUCTIONS,
		INSTSEM_RAW,
		INSTSEM_REPLACED,
		INSTSEM_REPLACED_ALIVES,
		STRUCTURED_INSTSEM_REPLACED_ALIVES
	}
	
	MethodContentViewStyle getMethodContentViewType() {
		return methodContentViewStyle;
	}
	
	void setMethodContentViewStyle(MethodContentViewStyle newStyle) {
		showClass(showingClass, newStyle);
	}
	
	void showClass(DexClass showing) {
		showClass(showing, methodContentViewStyle);
	}
	
	void showClass(DexClass showing, MethodContentViewStyle methodContentViewStyle) {
		if (this.showingClass != showing || this.methodContentViewStyle != methodContentViewStyle) {
			this.showingClass = showing;
			this.methodContentViewStyle = methodContentViewStyle;
			semantics = InstructionSemanticizer.get(showing.getProgram());
			constants = DataFlowAnalyzer.get(showing.getProgram());
			deads = DeadCodeCollector.get(showing.getProgram());
			
			getTitleItems()[0].title = "Class " + showing.getTypeFullNameBeauty();
	
			clearItemGroups();
			items.clear();
			fieldsToGroup.clear();
			methodsToGroup.clear();
			if (showing instanceof DexInternalClass) {
				initForInternalClass((DexInternalClass) showing);
			} else {
				initForExternalClass(showing);
			}
			
			setListSize(items.size());
		}
		clearHighlights();
	}
	
	void addMethodsToGroup(DexMethod method, ItemGroup itemgroup) {
		methodsToGroup.put(method, itemgroup);
	}
	
	public void addLabelsForAnnotations(DexAnnotations annotations, String prefix) {
		if (annotations != null) {
			int count = annotations.getAnnotationsCount();
			ArrayList<Label> annotationLabel = new ArrayList<Label>();
			
			for (int i = 0; i < count; i++) {
				DexAnnotationItem annotItem = annotations.getAnnotationItem(i);

				Label annotTitle = Label.newLabel(prefix + "@" + annotItem.getAnnotationType().getTypeShortNameBeauty(), ColorConstants.gray);
				
				annotationLabel.clear();
				annotationLabel.add(annotTitle);
				if (annotItem.getAnnotationSize() > 0) {
					annotationLabel.add(Label.newLabel("(", ColorConstants.gray));
					for (int j = 0; j < annotItem.getAnnotationSize(); j++) {
						if (j > 0) {
							annotationLabel.add(Label.newLabel(", ", ColorConstants.gray));
						}
						annotationLabel.add(Label.newLabel(annotItem.getAnnotationKey(j), ColorConstants.gray));
						Value value = annotItem.getAnnotationValue(j);
						if (value != null) {
							annotationLabel.add(Label.newLabel(" = ", ColorConstants.gray));
							annotationLabel.add(Label.newLabel(value.toString(), ColorConstants.gray));
						}
					}
					annotationLabel.add(Label.newLabel(")", ColorConstants.gray));
				}
				items.add(Label.newLabel(annotationLabel.toArray(new Label[0])));
			}
		}
	}
	
	private Map<DexField, Set<Label>> fieldLabels;
	private Map<DexMethod, Set<Label>> methodLabels;
	
	void addLabelForField(DexField field, Label label) {
		Set<Label> set = fieldLabels.get(field);
		
		if (set == null) {
			set = new HashSet<Label>();
			fieldLabels.put(field, set);
		}
		
		set.add(label);
	}
	
	void addLabelForMethod(DexMethod method, Label label) {
		Set<Label> set = methodLabels.get(method);
		
		if (set == null) {
			set = new HashSet<Label>();
			methodLabels.put(method, set);
		}
		
		set.add(label);
	}
	
	public Set<Label> getLabelsOfField(DexField field) {
		Set<Label> set = fieldLabels.get(field);
		
		if (set == null) {
			set = new HashSet<Label>();
			fieldLabels.put(field, set);
		}
		return set;
	}
	
	public Set<Label> getLabelsOfMethod(DexMethod method) {
		Set<Label> set = methodLabels.get(method);
		
		if (set == null) {
			set = new HashSet<Label>();
			methodLabels.put(method, set);
		}
		return set;
	}
	
	public static class OccurrenceMarkingClickListener<E> implements com.giyeok.dexdio.widgets.LabelClickListener {
		private Color color1;
		private Color color2;
		private Set<Label> labels;
		
		public enum ColorType {
			FieldColor,
			MethodColor,
			RegisterColor
		}
		
		public OccurrenceMarkingClickListener(Label label, Set<Label> labels, ColorType colorType) {
			this.color1 = ((TextLabel) label).getColor();
			switch (colorType) {
			case FieldColor: this.color2 = ColorConstants.cyan; break;
			case MethodColor: this.color2 = ColorConstants.orange; break;
			case RegisterColor: this.color2 = ColorConstants.red; break;
			}
			this.labels = labels;
		}
		
		@Override
		public boolean labelDoubleClicked(LabelListWidget widget, int index,
				Label label, int x, int y, MouseEvent e) {
			// nothing to do
			return false;
		}
		
		@Override
		public boolean labelClicked(LabelListWidget widget, int index, Label label,
				int x, int y, MouseEvent e) {
			setToggled(widget, ((TextLabel) label).getColor() == color1);
			return true;
		}
		
		protected void setToggled(LabelListWidget widget, boolean toggled) {
			for (Label l: labels) {
				((TextLabel) l).setColor((toggled)? color2:color1);
			}
			widget.redraw();
		}
	}
	
	private void initForInternalClass(DexInternalClass showing) {
		// package
		Label pack[] = new Label[4];
		pack[0] = Label.newLabel("package", ColorConstants.darkGreen);
		pack[1] = Label.newLabel(" ");
		pack[2] = Label.newLabel(showing.getPackageName());
		pack[3] = Label.newLabel(";");
		items.add(Label.newLabel(pack));
		
		items.add(Label.newEmptyLabel());
		
		// classAnnotations 추가해야 함
		addLabelsForAnnotations(showing.getAnnotations(), "");
		
		// title
		ArrayList<Label> title = new ArrayList<Label>();
		DexAccessFlags accessflags = showing.getAccessFlags();
		title.add(Label.newLabel(accessflags.stringifyForClasses(), ColorConstants.darkGreen));
		if (accessflags.isInterface() && accessflags.isEnum()) {
			if ((! accessflags.isInterface()) && (! accessflags.isEnum())) {
				title.add(Label.newLabel("class ", ColorConstants.darkGreen));
			} else {
				title.add(Label.newLabel(" class ", ColorConstants.darkGreen));
			}
		} else if (accessflags.isZero()) {
			title.add(Label.newLabel(" "));
		}
		title.add(Label.newLabel(showing.getClassName()));
		items.add(Label.newLabel(title.toArray(new Label[0])));
		
		if (showing.getSuperClass() != null) {
			Label superclassLabel = Label.newLabel(showing.getSuperClass().getTypeShortNameBeauty());
			// superclassLabel에 이벤트 추가
			items.add(Label.newLabel(new Label[] {
					Label.newLabel("        extends ", ColorConstants.darkGreen),
					superclassLabel
			}));
		}
		
		DexClass interfaces[] = showing.getImplementingInterfaces();
		if (interfaces.length > 0) {
			for (int i = 0; i < interfaces.length; i++) {
				Label interfaceLabel = Label.newLabel(interfaces[i].getTypeShortNameBeauty());
				// interfaceLabel에 이벤트 추가
				
				if (i < interfaces.length - 1) {
					interfaceLabel = Label.newLabel(new Label[] {
							interfaceLabel,
							Label.newLabel(", ")
					});
				}
				if (i == 0) {
					items.add(Label.newLabel(new Label[] {
							Label.newLabel("        implements ", ColorConstants.darkGreen),
							interfaceLabel
					}));
				} else {
					items.add(Label.newLabel(new Label[] {
							Label.newLabel("            "),
							interfaceLabel
					}));
				}
			}
		}
		
		items.add(Label.newLabel("{"));
		
		ArrayList<Label> fieldDef = new ArrayList<Label>();
		for (final DexField field: showing.getFields()) {
			String descriptorInfo = "    // field descriptor " + field.getFieldId();
			switch (field.getFieldType()) {
			case INHERITED_FIELD:
				descriptorInfo += " inherited field";
				break;
			case INSTANCE_FIELD:
				descriptorInfo += " instance field";
				break;
			case STATIC_FIELD:
				descriptorInfo += " static field";
				break;
			}
			items.add(Label.newLabel(descriptorInfo, ColorConstants.darkBlue));
			addLabelsForAnnotations(field.getAnnotations(), "    ");
			
			fieldDef.clear();
			String accessFlags = field.getAccessFlags().stringifyForFields();
			fieldDef.add(Label.newLabel("    " + accessFlags, ColorConstants.darkGreen));
			if (! accessFlags.isEmpty()) {
				fieldDef.add(Label.newLabel(" "));
			}
			fieldDef.add(Label.newLabel(field.getType().getTypeShortNameBeauty()));
			fieldDef.add(Label.newLabel(" "));
			Label label = Label.newLabel(field.getBelongedClass().getTypeFullNameBeauty() + "." + field.getName());
			label.addClickListener(new OccurrenceMarkingClickListener<DexField>(label, getLabelsOfField(field), ColorType.FieldColor));
			fieldDef.add(label);
			if (field.getDefaultValue() != null) {
				fieldDef.add(Label.newLabel(" = "));
				fieldDef.add(Label.newLabel(field.getDefaultValue().toString()));
			}
			fieldDef.add(Label.newLabel(";"));
			
			ItemGroup ig = new ItemGroup(items.size() - 1, items.size(), items.size() + 1, new ItemGroupSelectionListener() {
				
				@Override
				public void selected() {
					controller.showField(field);
				}

				@Override
				public void deselected() {
					controller.showClass(ClassContentView.this.showingClass);
				}

				@Override
				public void deselectedToMove() {
					// nothing to do
				}
			});
			fieldsToGroup.put(field, ig);
			addItemGroup(ig);
			
			items.add(Label.newLabel(fieldDef.toArray(new Label[0])));
			
			items.add(Label.newEmptyLabel());
		}
		
		for (DexMethod method: showing.getMethods()) {
			new MethodContentView(showing, method, items, this, methodContentViewStyle, controller);
		}

		items.add(Label.newLabel("}"));
	}
	
	private void initForExternalClass(DexClass showing) {
		// TODO
	}
	
	void showField(DexField field) {
		showClass(field.getBelongedClass());
		
		ItemGroup ig = fieldsToGroup.get(field);
		if (ig != null) {
			highlightItemGroup(ig);
		}
	}
	
	void showMethod(DexMethod method) {
		showClass((DexClass) method.getBelongedType());
		
		ItemGroup ig = methodsToGroup.get(method);
		if (ig != null) {
			highlightItemGroup(ig);
		}
	}

	@Override
	public Label getItem(int index) {
		return items.get(index);
	}

}
