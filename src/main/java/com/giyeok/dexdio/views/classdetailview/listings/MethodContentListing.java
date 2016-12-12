package com.giyeok.dexdio.views.classdetailview.listings;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;

import com.giyeok.dexdio.Utils;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexParameter;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.views.classdetailview.ClassContentView;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.MethodContentViewStyle;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.OccurrenceMarkingClickListener;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.OccurrenceMarkingClickListener.ColorType;
import com.giyeok.dexdio.views.classdetailview.DexClassDetailViewer;
import com.giyeok.dexdio.widgets.Label;

public abstract class MethodContentListing {
	protected DexClass showing;
	protected DexMethod method;
	protected DexCodeItem codeitem;
	protected ArrayList<Label> items;
	protected ClassContentView cdv;
	protected MethodContentViewStyle viewStyle;
	protected DexClassDetailViewer controller;
	
	public MethodContentListing(DexClass showing, DexMethod method, DexCodeItem codeitem, ArrayList<Label> items, 
			ClassContentView cdv, MethodContentViewStyle viewStyle, DexClassDetailViewer controller) {
		this.showing = showing;
		this.method = method;
		this.codeitem = codeitem;
		this.items = items;
		this.cdv = cdv;
		this.viewStyle = viewStyle;
		this.controller = controller;
	}
	
	public void addTitle() {
		// method definition
		ArrayList<Label> methodDef = new ArrayList<Label>();

		String descriptorInfo = "    // method descriptor " + method.getMethodId();
		switch (method.getMethodKind()) {
		case DIRECT_METHOD:
			descriptorInfo += " direct method";
			break;
		case VIRTUAL_METHOD:
			descriptorInfo += " virtual method";
			break;
		}
		items.add(Label.newLabel(descriptorInfo, ColorConstants.darkBlue));
		cdv.addLabelsForAnnotations(method.getAnnotations(), "    ");
		
		methodDef.clear();
		String accessFlags = method.getAccessFlags().stringifyForMethods();
		methodDef.add(Label.newLabel("    " + accessFlags, ColorConstants.darkGreen));
		if (! accessFlags.isEmpty()) {
			methodDef.add(Label.newLabel(" "));
		}
		methodDef.add(Label.newLabel(method.getReturnType().getTypeShortNameBeauty()));
		methodDef.add(Label.newLabel(" "));
		methodDef.add(getNameLabelForMethod(method, true));
		methodDef.add(Label.newLabel("("));
		DexParameter params[] = method.getParameters();
		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				methodDef.add(Label.newLabel(", "));
			}
			if (params[i].getAnnotations() != null) {
				items.add(Label.newLabel(methodDef.toArray(new Label[0])));
				methodDef.clear();
				cdv.addLabelsForAnnotations(params[i].getAnnotations(), "            ");
			}
			methodDef.add(Label.newLabel(params[i].getType().getTypeShortNameBeauty()));
			if (params[i].getRegister() != null) {
				methodDef.add(Label.newLabel(" "));
				methodDef.add(getParameterLabel(params[i]));
			}
		}
		methodDef.add(Label.newLabel(")"));

		if (codeitem != null) {
			methodDef.add(Label.newLabel(" {"));
			items.add(Label.newLabel(methodDef.toArray(new Label[0])));
		} else {
			methodDef.add(Label.newLabel(";"));
			items.add(Label.newLabel(methodDef.toArray(new Label[0])));
		}
	}
	
	void instructionBlockLabel(ArrayList<Label> instructionLabel, DexInstruction instruction, ControlFlowAnalysis cfg) {
		BasicBlock bb = cfg.findBasicBlockOfInstruction(instruction);
		String address = Integer.toHexString(instruction.getAddress());
		if (bb != null) {
			boolean isFirst = bb.isFirstInstruction(instruction);
			boolean isLast = bb.isLastInstruction(instruction);
			if (isFirst && isLast) {
				instructionLabel.add(Label.newLabel(String.format("%5s", bb.getName()) + " 式式" + address + " : "));
			} else if (isFirst) {
				instructionLabel.add(Label.newLabel(String.format("%5s", bb.getName()) + " 忙式" + address + " : "));
			} else if (isLast) {
				instructionLabel.add(Label.newLabel("      戌式" + address + " : "));
			} else {
				instructionLabel.add(Label.newLabel("      弛 " + address + " : "));
			}
		} else {
			instructionLabel.add(Label.newLabel("        " + address + " : "));
		}
	}

	Label getNameLabelForMethod(DexMethod method, boolean longname) {
		Label label;
		if (longname) {
			label = Label.newLabel(method.getBelongedType().getTypeFullNameBeauty() + "." + method.getName());
		} else {
			label = Label.newLabel(method.getName());
		}
		Set<Label> labels = cdv.getLabelsOfMethod(method);
		label.addClickListener(new OccurrenceMarkingClickListener<DexMethod>(label, labels, ColorType.MethodColor));
		labels.add(label);
		return label;
	}
	
	Label getNameLabelForType(DexType type) {
		// TODO this is temporary
		return Label.newLabel(type.getTypeFullNameBeauty());
	}
	
	Label getNameLabelForField(DexField field, boolean longname) {
		Label label;
		if (longname) {
			label = Label.newLabel(field.getBelongedClass().getTypeFullNameBeauty() + "." + field.getName());
		} else {
			label = Label.newLabel(field.getName());
		}
		Set<Label> labels = cdv.getLabelsOfField(field);
		label.addClickListener(new OccurrenceMarkingClickListener<DexField>(label, labels, ColorType.FieldColor));
		labels.add(label);
		return label;
	}
	
	abstract Label getParameterLabel(DexParameter parameter);
	abstract public Label getInstanceRegisterLabel();
	
	public abstract void addContent();
}
