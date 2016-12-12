package com.giyeok.dexdio.views.classdetailview;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.giyeok.dexdio.augmentation.ReferenceCollector;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.widgets.ListWidget;
import com.giyeok.dexdio.widgets.ListWidgetClickedListener;
import com.giyeok.dexdio.widgets.TextColumnListWidget;

class DetailOfMethodView {
	private Composite composite;
	private Label label;
	
	private ReferenceCollector referenceCollector;
	private ArrayList<DexInstruction> callerInstructions;
	private TextColumnListWidget callersList;

	private DexClassDetailViewer controller;
	
	public DetailOfMethodView(Composite parent, int style, final DexClassDetailViewer controller, DexProgram program) {
		composite = new Composite(parent, style);
		
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		
		label = new Label(composite, SWT.NONE);
		label.setText("Method detail");
		
		referenceCollector = ReferenceCollector.get(program);
		callerInstructions = null;
		callersList = new TextColumnListWidget(composite, SWT.NONE, new String[] { "Caller" }, 0) {
			
			@Override
			public String[] getItem(int index) {
				DexInstruction instruction = callerInstructions.get(index);
				DexMethod method = instruction.getCodeItem().getBelongedMethod();
				return new String [] { method.getBelongedType().getTypeFullNameBeauty() + "." + method.getName() };
			}
		};
		callersList.addListClickedListener(new ListWidgetClickedListener() {
			
			@Override
			public void itemDoubleClicked(ListWidget widget, int index, int x, int y,
					MouseEvent e) {
				controller.showInstruction(callerInstructions.get(index));
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

	public void showMethod(DexMethod method) {
		label.setText("Detail on method " + method.getName());
		callerInstructions = referenceCollector.getMethodCallers(method);
		if (callerInstructions == null) {
			callersList.setListSize(0);
		} else {
			callersList.setListSize(callerInstructions.size());
		}
	}
}
