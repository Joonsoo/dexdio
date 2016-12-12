package com.giyeok.dexdio.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.giyeok.dexdio.MainView;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.widgets.ListWidget;
import com.giyeok.dexdio.widgets.OneSelectionListEventListener;
import com.giyeok.dexdio.widgets.TextColumnListWidget;

public class DexMethodDetailViewer {
	private MainView mainView;
	private DexProgram program;
	
	private DexMethod selected;
	
	private Composite composite;
	private ListWidget codelist;
	
	public DexMethodDetailViewer(MainView mainView, Composite parent, DexProgram program) {
		this.mainView = mainView;
		this.program = program;
		
		composite = new Composite(parent, SWT.NONE);
		
		composite.setLayout(new FillLayout());
		
		codelist = new TextColumnListWidget(composite, SWT.NONE, new String[] {"addr", "instruction"}, 0) {
			
			@Override
			public String[] getItem(int index) {
				DexCodeItem codeitem = selected.getCodeItem();
				DexInstruction inst = codeitem.getInstruction(index);
				return new String[] {
						Integer.toHexString(inst.getAddress()),
						inst.getStringRepresentation()
				};
			}
		};
		codelist.addListClickedListener(new OneSelectionListEventListener());
		codelist.addKeyListener(codelist.new DefaultKeyListener());
	}
	
	public void setSelectedMethod(DexMethod selected) {
		this.selected = selected;
		
		if (selected.getCodeItem() == null) {
			codelist.setListSize(0);
		} else {
			codelist.setListSize(selected.getCodeItem().getInstructionsSize());
		}
	}
	
	public Control getContentControl() {
		return composite;
	}
}
