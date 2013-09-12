package com.giyeok.dexdio.views.classdetailview;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import com.giyeok.dexdio.MainView;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.MethodContentViewStyle;

public class DexClassDetailViewer {
	private MainView mainView;
	private DexProgram program;
	
	private Composite composite;
	
	private Combo classCombo;
	private ArrayList<DexClass> classArray;
	
	private SashForm classComposite;
	private ClassOutlineView classOutline;
	private ClassContentView classContent;
	
	// itemDetail: 현재 classDetail에서 선택된 아이템에 대한 디테일을 보여주는 위젯
	// 기본적으로 선택된 현재 클래스, 필드, 메소드 외에, 다른 타입(클래스를 포함한), 어노테이션, 변수, 파라메터 등이 있을 수 있음
	private Composite itemDetail;
	private StackLayout detailLayout;
	private DetailOfClassView classDetail;
	private DetailOfFieldView fieldDetail;
	private DetailOfMethodView methodDetail;
	private DetailOfInstructionView instDetail;
	
	// method content view style 설정 버튼
	private Combo comboMethodContentViewStyle;
	
	public DexClassDetailViewer(MainView mainView, Composite parent, final DexProgram program) {
		this.mainView = mainView;
		this.program = program;
		
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new Layout() {
			private Point comboSize = null;
			private Point styleComboSize = null;
			
			@Override
			protected void layout(Composite composite, boolean flushCache) {
				if (comboSize == null || styleComboSize == null || flushCache) {
					comboSize = classCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
					styleComboSize = comboMethodContentViewStyle.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				}
				
				Point compositeSize = composite.getSize();
				int comboY = Math.max(comboSize.y, styleComboSize.y);
				
				classCombo.setLocation(0, 0);
				classCombo.setSize(compositeSize.x - styleComboSize.x, comboSize.y);
				
				comboMethodContentViewStyle.setLocation(compositeSize.x - styleComboSize.x, 0);
				comboMethodContentViewStyle.setSize(styleComboSize.x, styleComboSize.y);
				
				classComposite.setLocation(0, comboY);
				classComposite.setSize(compositeSize.x, compositeSize.y - comboY);
			}
			
			@Override
			protected Point computeSize(Composite arg0, int arg1, int arg2, boolean arg3) {
				return null;
			}
		});
		
		classCombo = new Combo(composite, SWT.READ_ONLY);
		classArray = new ArrayList<DexClass>();
		for (int i = 0; i < program.getTypesCount(); i++) {
			DexType type = program.getTypeByTypeId(i);
			if (type instanceof DexClass) {
				classArray.add((DexClass) type);
				classCombo.add(program.getTypeByTypeId(i).getTypeFullNameBeauty());
			}
		}
		classCombo.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				showClass(classArray.get(classCombo.getSelectionIndex()));
			}
		});
		
		comboMethodContentViewStyle = new Combo(composite, SWT.READ_ONLY);
		comboMethodContentViewStyle.add("Instructions");
		comboMethodContentViewStyle.add("Instruction Semtnaics");
		comboMethodContentViewStyle.add("Instruction Semantics - replaced");
		comboMethodContentViewStyle.add("Instruction Semantics - replaced, alives");
		comboMethodContentViewStyle.add("Instruction Semantics - replaced, alives, structured");
		comboMethodContentViewStyle.select(4);
		comboMethodContentViewStyle.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				switch (comboMethodContentViewStyle.getSelectionIndex()) {
				case 1:
					classContent.setMethodContentViewStyle(MethodContentViewStyle.INSTSEM_RAW);
					break;
				case 2:
					classContent.setMethodContentViewStyle(MethodContentViewStyle.INSTSEM_REPLACED);
					break;
				case 3:
					classContent.setMethodContentViewStyle(MethodContentViewStyle.INSTSEM_REPLACED_ALIVES);
					break;
				case 4:
					classContent.setMethodContentViewStyle(MethodContentViewStyle.STRUCTURED_INSTSEM_REPLACED_ALIVES);
					break;
				default:
					classContent.setMethodContentViewStyle(MethodContentViewStyle.INSTRUCTIONS);
					break;
				}
			}
		});
		
		classComposite = new SashForm(composite, SWT.NONE);
		
		classOutline = new ClassOutlineView(classComposite, SWT.NONE, this);
		classContent = new ClassContentView(classComposite, SWT.NONE, this);
		
		itemDetail = new Composite(classComposite, SWT.NONE);
		classDetail = new DetailOfClassView(itemDetail, SWT.NONE, this);
		fieldDetail = new DetailOfFieldView(itemDetail, SWT.NONE, this);
		methodDetail = new DetailOfMethodView(itemDetail, SWT.NONE, this, program);
		instDetail = new DetailOfInstructionView(itemDetail, SWT.NONE, this);
		
		detailLayout = new StackLayout();
		itemDetail.setLayout(detailLayout);
		
		classComposite.setWeights(new int[] { 1, 2, 1 });
	}
	
	private void setClassCombo(DexClass showing) {
		if (classCombo.getSelectionIndex() < 0 ||
				classArray.get(classCombo.getSelectionIndex()) != showing) {
			classCombo.select(classArray.indexOf(showing));
		}
	}
	
	public void showClass(DexClass showing) {
		// System.out.println(showing.getTypeFullNameBeauty());
		setClassCombo(showing);
		classOutline.showClass(showing);
		classContent.showClass(showing);
		
		classDetail.showClass(showing);
		detailLayout.topControl = classDetail.getControl();
		itemDetail.layout();
	}
	
	public void showField(DexField field) {
		setClassCombo(field.getBelongedClass());
		classOutline.showClass(field.getBelongedClass());
		classContent.showField(field);
		classOutline.addHighlight(field);
		
		fieldDetail.showField(field);
		detailLayout.topControl = fieldDetail.getControl();
		itemDetail.layout();
	}
	
	public void showMethod(DexMethod method) {
		assert method.getBelongedType() instanceof DexClass;
		
		setClassCombo((DexClass) method.getBelongedType());
		classOutline.showClass((DexClass) method.getBelongedType());
		classContent.showMethod(method);
		classOutline.addHighlight(method);
		
		methodDetail.showMethod(method);
		detailLayout.topControl = methodDetail.getControl();
		itemDetail.layout();
	}
	
	public void showInstruction(DexInstruction instruction) {
		DexMethod belongedMethod = instruction.getCodeItem().getBelongedMethod();
		
		assert belongedMethod.getBelongedType() instanceof DexClass;
		
		setClassCombo((DexClass) belongedMethod.getBelongedType());
		classOutline.showClass((DexClass) belongedMethod.getBelongedType());
		classContent.showMethod(belongedMethod);
		classOutline.addHighlight(belongedMethod);
		
		instDetail.showInstruction(instruction);
		detailLayout.topControl = instDetail.getControl();
		itemDetail.layout();
	}
	
	public Control getContentControl() {
		return composite;
	}
}

