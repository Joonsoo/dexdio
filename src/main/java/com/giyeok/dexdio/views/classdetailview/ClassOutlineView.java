package com.giyeok.dexdio.views.classdetailview;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

import com.giyeok.dexdio.Utils;
import com.giyeok.dexdio.dexreader.value.Value;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexParameter;
import com.giyeok.dexdio.widgets.Label;
import com.giyeok.dexdio.widgets.LabelHoverListener;
import com.giyeok.dexdio.widgets.LabelListWidget;
import com.giyeok.dexdio.widgets.ListWidget;
import com.giyeok.dexdio.widgets.ListWidgetClickedListener;
import com.giyeok.dexdio.widgets.ListWidgetSelectionListener;
import com.giyeok.dexdio.widgets.OneSelectionListEventListener;

/**
 * 클래스의 아웃라인을 보이는 리스트
 * @author joonsoo
 *
 */
class ClassOutlineView extends LabelListWidget {
	
	private DexClassDetailViewer controller;

	public ClassOutlineView(Composite parent, int style, final DexClassDetailViewer controller) {
		super(parent, style, "Outline");
		
		this.controller = controller;
		
		showing = null;
		fields = null;
		methods = null;
		
		addListClickedListener(new OneSelectionListEventListener());
		addKeyListener(new DefaultKeyListener());
		addListSelectionListener(new ListWidgetSelectionListener() {
			
			@Override
			public void itemSelected(ListWidget widget, int index) {
				if (widget.isHighlighted(index)) {
					if (index < fields.length) {
						controller.showField(fields[index]);
					} else {
						controller.showMethod(methods[index - fields.length]);
					}
				} else {
					controller.showClass(showing);
				}
			}
		});
	}
	
	private DexClass showing;
	private DexField[] fields;
	private DexMethod[] methods;
	
	void showClass(DexClass showing) {
		if (this.showing != showing) {
			this.showing = showing;
			fields = showing.getFields();
			methods = showing.getMethods();
			
			setListSize(fields.length + methods.length);
		}
		clearHighlights();
	}

	@Override
	public Label getItem(int index) {
		assert index < fields.length + methods.length;
		
		if (index < fields.length) {
			DexField field = fields[index];
			Value defaultValue = field.getDefaultValue();
			if (defaultValue == null) {
				Label labels[] = new Label[4];
				
				labels[0] = field.getAccessFlags().labelizeForFields();
				labels[1] = Label.newLabel(field.getName(), ColorConstants.black);
				labels[2] = Label.newLabel(" : ", ColorConstants.orange);
				labels[3] = Label.newLabel(field.getType().getTypeShortNameBeauty(), ColorConstants.orange);
				// labels[3]에 hover event 및 click 이벤트 추가
				return Label.newLabel(labels);
			} else {
				Label labels[] = new Label[6];
				
				labels[0] = field.getAccessFlags().labelizeForFields();
				labels[1] = Label.newLabel(field.getName(), ColorConstants.black);
				labels[2] = Label.newLabel(" : ", ColorConstants.orange);
				labels[3] = Label.newLabel(field.getType().getTypeShortNameBeauty(), ColorConstants.orange);
				// labels[3]에 hover event 및 click 이벤트 추가
				labels[4] = Label.newLabel(" = ", ColorConstants.black);
				labels[5] = Label.newLabel(defaultValue.toString(), ColorConstants.black);
				return Label.newLabel(labels);
			}
		} else {
			DexMethod method = methods[index - fields.length];
			final DexParameter params[] = method.getParameters();
			
			if (params.length == 0) {
				Label labels[] = new Label[5];
				
				labels[0] = method.getAccessFlags().labelizeForMethods();
				labels[1] = Label.newLabel(method.getName(), ColorConstants.black);
				labels[2] = Label.newLabel("()", ColorConstants.black);
				labels[3] = Label.newLabel(" : ", ColorConstants.orange);
				labels[4] = Label.newLabel(method.getReturnType().getTypeShortNameBeauty(), ColorConstants.orange);
				return Label.newLabel(labels);
			} else {
				Label labels[] = new Label[5 + params.length * 2];
				
				labels[0] = method.getAccessFlags().labelizeForMethods();
				labels[1] = Label.newLabel(method.getName(), ColorConstants.black);
				labels[2] = Label.newLabel("(", ColorConstants.black);
				for (int i = 0; i < params.length; i++) {
					if (i > 0) {
						labels[2 + i * 2] = Label.newLabel(", ", ColorConstants.black);
					}
					Label paramLabel = Label.newLabel(params[i].getType().getTypeShortNameBeauty(), ColorConstants.black);
					labels[3 + i * 2] = paramLabel;
					paramLabel.addHoverListener(new LabelHoverListener() {

						private DexParameter param;
						
						LabelHoverListener setType(DexParameter param) {
							this.param = param;
							return this;
						}
						
						@Override
						public boolean labelHovered(LabelListWidget widget, int index, Label label,
								int x, int y, MouseEvent e) {
							System.out.println(param.getType().getTypeFullNameBeauty());
							return false;
						}
					}.setType(params[i]));
					// paramLabel에 hover event 및 click 이벤트 추가
				}
				labels[2 + params.length * 2] = Label.newLabel(")", ColorConstants.black);
				labels[3 + params.length * 2] = Label.newLabel(" : ", ColorConstants.orange);
				labels[4 + params.length * 2] = Label.newLabel(method.getReturnType().getTypeShortNameBeauty(), ColorConstants.orange);
				return Label.newLabel(labels);
			}
		}
	}

	public void addHighlight(DexField field) {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] == field) {
				boundScroll(i);
				addHighlight(i);
			}
		}
	}

	public void addHighlight(DexMethod method) {
		for (int i = 0; i < methods.length; i++) {
			if (methods[i] == method) {
				boundScroll(fields.length + i);
				addHighlight(fields.length + i);
			}
		}
	}
}
