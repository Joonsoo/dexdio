package com.giyeok.dexdio.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.giyeok.dexdio.MainView;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexProgram;

public class DexFieldDetailView {
	private MainView mainView;
	private DexProgram program;
	
	private Composite composite;
	
	public DexFieldDetailView(MainView mainView, Composite parent, DexProgram program) {
		this.mainView = mainView;
		this.program = program;
		
		composite = new Composite(parent, SWT.NONE);
	}
	
	public void setSelectedField(DexField selected) {
	}
	
	public Control getContentControl() {
		return composite;
	}

}
