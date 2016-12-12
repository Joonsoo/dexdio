package com.giyeok.dexdio.views.classdetailview.listings;

import java.util.ArrayList;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
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


public class ReplacedInstSemListing extends InstSemListing {
	
	public ReplacedInstSemListing(DexClass showing, DexMethod method, DexCodeItem codeitem, ArrayList<Label> items, 
			ClassContentView cdv, MethodContentViewStyle viewStyle, DexClassDetailViewer controller) {
		super(showing, method, codeitem, items, cdv, viewStyle, controller);
	}

	@Override
	public void addContent() {
		MethodSemantics methodsemantics = InstructionSemanticizer.get(codeitem.getProgram()).getMethodSemantics(codeitem);
		InstSemStatement semantics[] = methodsemantics.getStatements();
		ControlFlowAnalysis cfg = ControlFlowAnalyzer.get(codeitem.getProgram()).getControlFlowForMethod(codeitem);
		ArrayList<Label> instructionLabel = new ArrayList<Label>();
		
		initAugmentations();

		for (int i = 0; i < semantics.length; i++) {
			final InstSemStatement sem = semantics[i];

			instructionLabel.clear();

			instructionBlockLabel(instructionLabel, sem.getInstruction(), cfg);
			if (deadcodes.isDeadInstruction(sem)) {
				instructionLabel.add(Label.newLabel("// "));
			}
			instructionSemanticLabel(cdv, instructionLabel, sem, true);
			cdv.addItemGroup(cdv.new ItemGroup(items.size(), new InstSemClicked(controller, sem, showing)));
			items.add(Label.newLabel(instructionLabel.toArray(new Label[0])));
		}
	}
}
