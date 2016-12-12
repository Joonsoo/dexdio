package com.giyeok.dexdio.views.classdetailview.listings;

import java.util.ArrayList;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock;
import com.giyeok.dexdio.augmentation.instsem.InstSemStatement;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer.MethodSemantics;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.views.classdetailview.ClassContentView;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.MethodContentViewStyle;
import com.giyeok.dexdio.views.classdetailview.DexClassDetailViewer;
import com.giyeok.dexdio.widgets.Label;

public class ReplacedAliveInstSemListing extends InstSemListing {

	public ReplacedAliveInstSemListing(DexClass showing, DexMethod method, DexCodeItem codeitem, ArrayList<Label> items, 
			ClassContentView cdv, MethodContentViewStyle viewStyle, DexClassDetailViewer controller) {
		super(showing, method, codeitem, items, cdv, viewStyle, controller);

		if (codeitem != null) {
			methodsemantics = InstructionSemanticizer.get(codeitem.getProgram()).getMethodSemantics(codeitem);
			semantics = methodsemantics.getStatements();

			initAugmentations();
		}
	}
	
	private MethodSemantics methodsemantics;
	private InstSemStatement semantics[];

	@Override
	public void addContent() {
		ControlFlowAnalysis cfg = ControlFlowAnalyzer.get(codeitem.getProgram()).getControlFlowForMethod(codeitem);
		
		BasicBlock bbs[] = cfg.getBlocks();
		for (int j = 0; j < bbs.length; j++) {
			addBasicBlock(bbs[j], 0, true);
		}
	}
	
	protected String getIndentSpace(int indent, int leftmargin) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < (indent + 2) * 4 + leftmargin; i++) {
			buf.append(' ');
		}
		return buf.toString();
	}
	
	protected void addBasicBlock(BasicBlock bb, int indent) {
		addBasicBlock(bb, indent, false);
	}
	
	protected void addLine(int indent, String text) {
		items.add(Label.newLabel(getIndentSpace(indent, 0) + text));
	}
	
	private void addBasicBlock(BasicBlock bb, int indent, boolean leftlabel) {
		InstSemStatement first = null, last = null;
		int firstIndex = codeitem.getIndexOfInstruction(bb.getFirstInstruction());
		int lastIndex = codeitem.getIndexOfInstruction(bb.getLastInstruction());
		assert firstIndex <= lastIndex;
		for (int i = firstIndex; i <= lastIndex; i++) {
			first = methodsemantics.findStatmentByInstruction(codeitem.getInstruction(i));
			if (! deadcodes.isDeadInstruction(first)) {
				break;
			}
		}
		if (deadcodes.isDeadInstruction(first)) {
			// 블록 전체가 dead code임을 의미
			items.add(Label.newLabel(getIndentSpace(indent, -2) + "-- block " + bb.getName() + " is all dead codes"));
			return;
		}
		for (int i = lastIndex; i >= firstIndex; i--) {
			last = methodsemantics.findStatmentByInstruction(codeitem.getInstruction(i));
			if (last != null && (! deadcodes.isDeadInstruction(last))) {
				break;
			}
		}
		assert ((! deadcodes.isDeadInstruction(first)) && (! deadcodes.isDeadInstruction(last)));

		ArrayList<Label> instructionLabel = new ArrayList<Label>();
		firstIndex = methodsemantics.getIndexOf(first);
		lastIndex = methodsemantics.getIndexOf(last);
		for (int i = firstIndex; i <= lastIndex; i++) {
			InstSemStatement sem = semantics[i];
			
			instructionLabel.clear();
			
			if (! deadcodes.isDeadInstruction(sem)) {
				String address = Integer.toHexString(sem.getInstruction().getAddress());
				String s;
				if (! leftlabel) {
					if (i == firstIndex) {
						addLine(indent, "// " + bb.getName() + "(" + Integer.toHexString(bb.getFirstInstruction().getAddress()) + "-" + Integer.toHexString(bb.getLastInstruction().getAddress()) + ")");
					}
					instructionLabel.add(Label.newLabel(getIndentSpace(indent, 0)));
				} else {
					if ((i == firstIndex) && (i == lastIndex)) {
						assert firstIndex == lastIndex;
						s = String.format("%" + ((indent + 2) * 4 - 3) + "s", bb.getName()) + " ──" + address + " : ";
					} else if (i == firstIndex) {
						s = String.format("%" + ((indent + 2) * 4 - 3) + "s", bb.getName()) + " ┌─" + address + " : ";
					} else if (i == lastIndex) {
						s = getIndentSpace(indent, -2) + "└─" + address + " : ";
					} else {
						s = getIndentSpace(indent, -2) + "│ " + address + " : ";
					}
					instructionLabel.add(Label.newLabel(s));
				}
				
				instructionSemanticLabel(cdv, instructionLabel, sem, true);
				cdv.addItemGroup(cdv.new ItemGroup(items.size(), new InstSemClicked(controller, sem, showing)));

				items.add(Label.newLabel(instructionLabel.toArray(new Label[0])));
			}
		}
	}
}
