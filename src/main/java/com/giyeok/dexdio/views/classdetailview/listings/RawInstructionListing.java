package com.giyeok.dexdio.views.classdetailview.listings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.model0.DexClass;
import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexMethod;
import com.giyeok.dexdio.model0.DexParameter;
import com.giyeok.dexdio.model0.insns.DexInstruction;
import com.giyeok.dexdio.model0.insns.Operand;
import com.giyeok.dexdio.model0.insns.OperandConstantPool;
import com.giyeok.dexdio.model0.insns.OperandRegister;
import com.giyeok.dexdio.model0.insns.OperandRegisterRange;
import com.giyeok.dexdio.views.classdetailview.ClassContentView;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.MethodContentViewStyle;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.OccurrenceMarkingClickListener;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.OccurrenceMarkingClickListener.ColorType;
import com.giyeok.dexdio.views.classdetailview.DexClassDetailViewer;
import com.giyeok.dexdio.widgets.GroupedLabelListWidget.ItemGroupSelectionListener;
import com.giyeok.dexdio.widgets.Label;

public class RawInstructionListing extends MethodContentListing {

	private Map<DexRegister, Set<Label>> registerLabels;
	
	public RawInstructionListing(DexClass showing, DexMethod method, DexCodeItem codeitem, ArrayList<Label> items, 
			ClassContentView cdv, MethodContentViewStyle viewStyle, DexClassDetailViewer controller) {
		super(showing, method, codeitem, items, cdv, viewStyle, controller);

		registerLabels = new HashMap<DexCodeItem.DexRegister, Set<Label>>();
	}
	
	Label getNameLabelForRegister(DexRegister register) {
		Set<Label> labels = registerLabels.get(register);

		if (labels == null) {
			labels = new HashSet<Label>();
			registerLabels.put(register, labels);
		}
		
		Label label;
		label = Label.newLabel(register.getName());
		label.addClickListener(new OccurrenceMarkingClickListener<DexRegister>(label, labels, ColorType.RegisterColor));
		labels.add(label);
		return label;
	}
	
	Label getNameLabelForOperand(Operand operand) {
		// temporary!
		// TODO rewrite this method for proper purpose
		if (operand instanceof OperandConstantPool) {
			switch (((OperandConstantPool) operand).getConstantKind()) {
			case FIELD:
				return getNameLabelForField(codeitem.getProgram().getFieldByFieldId(((OperandConstantPool) operand).getValue()), true);
			case METHOD:
				return getNameLabelForMethod(codeitem.getProgram().getMethodByMethodId(((OperandConstantPool) operand).getValue()), true);
			default:
				return Label.newLabel(operand.getStringRepresentation());
			}
		} else if (operand instanceof OperandRegister) {
			return getNameLabelForRegister(((OperandRegister) operand).getRegister());
		} else if (operand instanceof OperandRegisterRange) {
			DexRegister registers[] = ((OperandRegisterRange) operand).getRegisters();
			ArrayList<Label> labels = new ArrayList<Label>();
			labels.add(Label.newLabel("{"));
			for (int k = 0; k < registers.length; k++) {
				if (k > 0) {
					labels.add(Label.newLabel(", "));
				}
				labels.add(getNameLabelForRegister(registers[k]));
			}
			labels.add(Label.newLabel("}"));
			return Label.newLabel(labels.toArray(new Label[0]));
		} else {
			return Label.newLabel(operand.getStringRepresentation());
		}
	}
	
	void instructionRawContent(ArrayList<Label> instructionLabel, DexInstruction instruction) {
		instructionLabel.add(Label.newLabel(instruction.getOpcodeMnemonic()));
		int opcount = instruction.getOperandsLength();
		for (int j = 0; j < opcount; j++) {
			Operand operand = instruction.getOperand(j);
			
			if (j > 0) {
				instructionLabel.add(Label.newLabel(", "));
			} else {
				instructionLabel.add(Label.newLabel(" "));
			}

			instructionLabel.add(getNameLabelForOperand(operand));
		}
	}

	@Override
	public void addContent() {
		ControlFlowAnalysis cfg = ControlFlowAnalyzer.get(codeitem.getProgram()).getControlFlowForMethod(codeitem);
		ArrayList<Label> instructionLabel = new ArrayList<Label>();
		
		for (int i = 0; i < codeitem.getInstructionsSize(); i++) {
			final DexInstruction instruction = codeitem.getInstruction(i);

			instructionLabel.clear();

			instructionBlockLabel(instructionLabel, instruction, cfg);
			instructionRawContent(instructionLabel, instruction);
			cdv.addItemGroup(cdv.new ItemGroup(items.size(), new ItemGroupSelectionListener() {
				
				@Override
				public void selected() {
					controller.showInstruction(instruction);
				}

				@Override
				public void deselected() {
					controller.showClass(showing);
				}

				@Override
				public void deselectedToMove() {
					// nothing to do
				}
			}));
			items.add(Label.newLabel(instructionLabel.toArray(new Label[0])));
		}
	}

	@Override
	Label getParameterLabel(DexParameter parameter) {
		return getNameLabelForRegister(parameter.getRegister());
	}

	@Override
	public Label getInstanceRegisterLabel() {
		return getNameLabelForRegister(codeitem.getInstanceRegister());
	}
}
