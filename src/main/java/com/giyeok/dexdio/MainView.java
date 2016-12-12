package com.giyeok.dexdio;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer;
import com.giyeok.dexdio.augmentation.DataFlowAnalyzer;
import com.giyeok.dexdio.augmentation.OperandTypeInferer;
import com.giyeok.dexdio.augmentation.ReferenceCollector;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer;
import com.giyeok.dexdio.dexreader.DalvikExecutable;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexException;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.views.AugmentationMessagesViewer;
import com.giyeok.dexdio.views.DexClassesViewer;
import com.giyeok.dexdio.views.DexFieldDetailView;
import com.giyeok.dexdio.views.DexHexStructureViewer;
import com.giyeok.dexdio.views.DexMethodDetailViewer;
import com.giyeok.dexdio.views.classdetailview.DexClassDetailViewer;

public class MainView {
	private DexProgram program;
	
	private TabFolder tabFolder;
	
	private DexClassesViewer classes;
	private DexClassDetailViewer classDetail;
	private DexMethodDetailViewer methodDetail;
	private DexFieldDetailView fieldDetail;
	private AugmentationMessagesViewer augmsgViewer;
	
	private Shell shell;
	
	public MainView(DalvikExecutable dex, final Shell shell) {
		this.shell = shell;
		shell.setLayout(new FillLayout());
		shell.setText("Dexdio: " + dex.getFilepath());
		shell.setBounds(100, 100, 800, 600);
		
		tabFolder = new TabFolder(shell, SWT.NONE);
		
		try {
			program = new DexProgram(dex);
		} catch (DexException e) {
			program = null;
			MessageBox msg = new MessageBox(shell);
			msg.setMessage("Failed to load dex file, reason: " + e.getMessage());
			msg.open();
			e.printStackTrace();
		}
		
		addToTab("Dex Structure", new DexHexStructureViewer(tabFolder, this, dex).getTabFolder());
		if (program != null) {
			InstructionSemanticizer.get(program);
			ControlFlowAnalyzer.get(program).visit();
			ReferenceCollector.get(program);
			DataFlowAnalyzer.get(program);
			OperandTypeInferer.get(program);
			ControlFlowStructuralizer.get(program);
			
			classes = new DexClassesViewer(this, tabFolder, program);
			addToTab("Classes", classes.getContentControl());
			
			classDetail = new DexClassDetailViewer(this, tabFolder, program);
			addToTab("Class", classDetail.getContentControl());
			
			methodDetail = new DexMethodDetailViewer(this, tabFolder, program);
			addToTab("Method", methodDetail.getContentControl());
			
			fieldDetail = new DexFieldDetailView(this, tabFolder, program);
			addToTab("Field", fieldDetail.getContentControl());
			
			augmsgViewer = new AugmentationMessagesViewer(this, tabFolder, program);
			addToTab("AugMsg", augmsgViewer);
			
			tabFolder.setSelection(1);
			tabFolder.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (tabFolder.getSelectionIndex() == 5) {
						// currently augmsgViewer is in index 5
						augmsgViewer.update();
					}
				}
			});

			// Initialize augmentations
			InstructionSemanticizer.get(program);
			DataFlowAnalyzer.get(program);
		}
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				tabFolder.dispose();
				shell.dispose();
			}
		});
		shell.open();
	}
	
	private void addToTab(String title, Control content) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(title);
		tabItem.setControl(content);
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public void openClassDetail(DexClass target) {
		classDetail.showClass(target);
		tabFolder.setSelection(2);
	}
	
	public void openMethodDetail(DexMethod target) {
		classDetail.showMethod(target);
		tabFolder.setSelection(2);
	}

	public void openFieldDetail(DexField target) {
		classDetail.showField(target);
		tabFolder.setSelection(2);
	}
}
