package com.giyeok.dexdio.views.classdetailview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.giyeok.dexdio.augmentation.ReferenceCollector;
import com.giyeok.dexdio.model.DexClass;

class DetailOfClassView {
	private DexClassDetailViewer controller;

	private Composite composite;
	private Label label;
	
	private DexClass showing;

	public DetailOfClassView(Composite parent, int style, DexClassDetailViewer controller) {
		composite = new Composite(parent, style);
		
		composite.setLayout(new FillLayout());

		label = new Label(composite, SWT.NONE);
		label.setText("Class detail");
		
		this.controller = controller;
		
		showing = null;
	}
	
	public Control getControl() {
		return composite;
	}
	
	private ReferenceCollector referenceColletor;

	public void showClass(DexClass showing) {
		if (this.showing != showing) {
			this.showing = showing;
			
			referenceColletor = ReferenceCollector.get(showing.getProgram());
			
			label.setText("Detail on class " + showing.getClassName());
			// TODO
		}
	}
}
