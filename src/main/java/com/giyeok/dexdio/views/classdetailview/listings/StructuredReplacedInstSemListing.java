package com.giyeok.dexdio.views.classdetailview.listings;

import java.util.ArrayList;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.ControlFlowStructure;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.ControlStatement;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.FlatStructure;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.IfStructure;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.SwitchStructure;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.SynchronizedStructure;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.TryCatchStructure;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.WhileStructure;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstSwitch;
import com.giyeok.dexdio.util.Pair;
import com.giyeok.dexdio.views.classdetailview.ClassContentView;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.MethodContentViewStyle;
import com.giyeok.dexdio.views.classdetailview.DexClassDetailViewer;
import com.giyeok.dexdio.widgets.Label;

public class StructuredReplacedInstSemListing extends ReplacedAliveInstSemListing {

	public StructuredReplacedInstSemListing(DexClass showing, DexMethod method, DexCodeItem codeitem, ArrayList<Label> items,
			ClassContentView cdv, MethodContentViewStyle viewStyle, DexClassDetailViewer controller) {
		super(showing, method, codeitem, items, cdv, viewStyle, controller);
	}

	private ControlFlowAnalysis cfg;
	
	@Override
	public void addContent() {
		ControlFlowStructure struct = ControlFlowStructuralizer.get(codeitem.getProgram()).getStructure(codeitem);
		cfg = ControlFlowAnalyzer.get(codeitem.getProgram()).getControlFlowForMethod(codeitem);
		
		if (struct == null) {
			super.addContent();
		} else {
			addStructure(struct, 0);
		}
	}
	
	private void addStructure(ControlFlowStructure struct, int indent) {
		switch (struct.getStructureType()) {
		case FLAT: {
			FlatStructure flat = (FlatStructure) struct;
			
			addBasicBlock(flat.getBlock(), indent);
			if (flat.getNext() != null) {
				addStructure(flat.getNext(), indent);
			}
			break;
		}
		case IF: {
			IfStructure ifs = (IfStructure) struct;
			ControlFlowStructure thenpart = ifs.getThenPart();
			ControlFlowStructure elsepart = ifs.getElsePart();
			
			addBasicBlock(ifs.getConditionBlock(), indent);
			if (ifs.isNegated()) {
				addLine(indent, "if (! ) {");
			} else {
				addLine(indent, "if () {");
			}
			addStructure(thenpart, indent + 1);
			if (elsepart == null) {
				addLine(indent, "}");
			} else {
				addLine(indent, "} else {");
				addStructure(elsepart, indent + 1);
				addLine(indent, "}");
			}
			
			ControlFlowStructure next = ifs.getNext();
			if (next != null) {
				addStructure(next, indent);
			}
			break;
		}
		case SWITCH: {
			SwitchStructure swit = (SwitchStructure) struct;
			BasicBlock branch = swit.getBranchBlock();
			assert branch.getLastInstruction() instanceof DexInstSwitch;
			DexInstSwitch switinst = (DexInstSwitch) branch.getLastInstruction();
			ArrayList<Pair<Integer, Pair<ControlFlowStructure, Boolean>>> cases = swit.getCases();
			ControlFlowStructure deflt = swit.getDefault();
			ControlFlowStructure next = swit.getNext();
			
			addBasicBlock(branch, indent);
			addLine(indent, "switch () {");
			for (Pair<Integer, Pair<ControlFlowStructure, Boolean>> c: cases) {
				addLine(indent, "case " + c.getKey() + ":      // " + cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(switinst.getBranchAddressForValue(c.getKey()))).getName());
				addStructure(c.getValue().getKey(), indent + 1);
				if (c.getValue().getValue()) {
					addLine(indent + 1, "break;");
				}
			}
			if (deflt != null) {
				addLine(indent, "default:");
				addStructure(deflt, indent + 1);
			}
			if (next != null) {
				addLine(indent, "}");
				addStructure(next, indent);
			}
			break;
		}
		case WHILE: {
			WhileStructure whil = (WhileStructure) struct;
			BasicBlock condition = whil.getConditionBlock();
			ControlFlowStructure body = whil.getBody();
			ControlFlowStructure next = whil.getNext();
			
			addBasicBlock(condition, indent);
			addLine(indent, "while () {");
			addStructure(body, indent + 1);
			addLine(indent, "}");
			addStructure(next, indent);
			break;
		}
		case DOWHILE:
			break;
		case TRYCATCH: {
			TryCatchStructure tryc = (TryCatchStructure) struct;
			addLine(indent, "try {");
			addStructure(tryc.getTryBody(), indent + 1);
			for (Pair<DexType, ControlFlowStructure> handler: tryc.getHandlers()) {
				addLine(indent, "} catch (" + handler.getKey().getTypeShortNameBeauty() + ") {");
				addStructure(handler.getValue(), indent + 1);
			}
			if (tryc.getFinally() != null) {
				addLine(indent, "} finally {");
				addStructure(tryc.getFinally(), indent + 1);
			}
			addLine(indent, "}");
			if (tryc.getNext() != null) {
				addStructure(tryc.getNext(), indent);
			}
			break;
		}
		case SYNCHRONIZED: {
			SynchronizedStructure sync = (SynchronizedStructure) struct;
			addLine(indent, "synchronized (" + sync.getMonitorEnterInstruction().getTarget().getStringRepresentation() + ") {");
			addStructure(sync, indent + 1);
			addLine(indent, "}");
			break;
		}
		case CONTROLSTATEMENT: {
			ControlStatement cs = (ControlStatement) struct;
			addLine(indent, cs.getStatementType().toString());
			break;
		}
		}
	}
}
