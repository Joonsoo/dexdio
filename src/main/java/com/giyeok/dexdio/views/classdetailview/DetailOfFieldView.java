package com.giyeok.dexdio.views.classdetailview;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.giyeok.dexdio.augmentation.ReferenceCollector;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.widgets.ListWidget;
import com.giyeok.dexdio.widgets.ListWidgetClickedListener;
import com.giyeok.dexdio.widgets.TextColumnListWidget;

class DetailOfFieldView {
	private Composite composite;
	private Label label;
	private TextColumnListWidget list;
	
	private DexClassDetailViewer controller;

	public DetailOfFieldView(Composite parent, int style, final DexClassDetailViewer controller) {
		composite = new Composite(parent, style);
		
		composite.setLayout(new FillLayout(SWT.VERTICAL));

		label = new Label(composite, SWT.NONE);
		label.setText("Field detail");
		
		list = new TextColumnListWidget(composite, SWT.NONE, new String[] {"Referred"}, 0) {
			
			@Override
			public String[] getItem(int index) {
				DexInstruction instruction = referInstructions.get(index);
				DexMethod method = instruction.getCodeItem().getBelongedMethod();
				return new String [] { method.getBelongedType().getTypeFullNameBeauty() + "." + method.getName() };
			}
		};
		list.addListClickedListener(new ListWidgetClickedListener() {
			
			@Override
			public void itemDoubleClicked(ListWidget widget, int index, int x, int y,
					MouseEvent e) {
				controller.showInstruction(referInstructions.get(index));
			}
			
			@Override
			public void itemClicked(ListWidget widget, int index, int x, int y,
					MouseEvent e) {
				// nothing to do here
			}
		});
		
		this.controller = controller;
	}

	public Control getControl() {
		return composite;
	}
	
	private ArrayList<DexInstruction> referInstructions;

	public void showField(DexField field) {
		label.setText("Detail on field " + field.getName());
		
		referInstructions = ReferenceCollector.get(field.getBelongedClass().getProgram()).getFieldReferences(field);
		list.setListSize(referInstructions.size());
	}
}
