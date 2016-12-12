package com.giyeok.dexdio.views.classdetailview.listings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.augmentation.DataFlowAnalyzer;
import com.giyeok.dexdio.augmentation.DeadCodeCollector;
import com.giyeok.dexdio.augmentation.OperandTypeInferer;
import com.giyeok.dexdio.augmentation.instsem.InstSemArrayReference;
import com.giyeok.dexdio.augmentation.instsem.InstSemAssignment;
import com.giyeok.dexdio.augmentation.instsem.InstSemBinaryOp;
import com.giyeok.dexdio.augmentation.instsem.InstSemConst;
import com.giyeok.dexdio.augmentation.instsem.InstSemEtcOperand;
import com.giyeok.dexdio.augmentation.instsem.InstSemEtcStatement;
import com.giyeok.dexdio.augmentation.instsem.InstSemInstanceField;
import com.giyeok.dexdio.augmentation.instsem.InstSemInstanceMethodInvoke;
import com.giyeok.dexdio.augmentation.instsem.InstSemNewArray;
import com.giyeok.dexdio.augmentation.instsem.InstSemNewInstance;
import com.giyeok.dexdio.augmentation.instsem.InstSemParameter;
import com.giyeok.dexdio.augmentation.instsem.InstSemRegister;
import com.giyeok.dexdio.augmentation.instsem.InstSemStatement;
import com.giyeok.dexdio.augmentation.instsem.InstSemStaticField;
import com.giyeok.dexdio.augmentation.instsem.InstSemStaticMethodInvoke;
import com.giyeok.dexdio.augmentation.instsem.InstSemUnaryOp;
import com.giyeok.dexdio.augmentation.instsem.InstSemUnaryOp.UnaryOperator;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemElement;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer.MethodSemantics;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexParameter;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.views.classdetailview.ClassContentView;
import com.giyeok.dexdio.views.classdetailview.ClassContentView.MethodContentViewStyle;
import com.giyeok.dexdio.views.classdetailview.DexClassDetailViewer;
import com.giyeok.dexdio.widgets.GroupedLabelListWidget.ItemGroupSelectionListener;
import com.giyeok.dexdio.widgets.Label;
import com.giyeok.dexdio.widgets.LabelClickListener;
import com.giyeok.dexdio.widgets.LabelDrawer;
import com.giyeok.dexdio.widgets.LabelListWidget;

public class InstSemListing extends MethodContentListing {
	
	private Set<InstSemOperand> showingOperands;
	
	private InstSemOperand focus;
	private Set<InstSemOperand> gens, uses;
	
	DataFlowAnalyzer dataflow;
	DeadCodeCollector deadcodes;
	OperandTypeInferer typeinfer;
	
	public InstSemListing(DexClass showing, DexMethod method, DexCodeItem codeitem, ArrayList<Label> items, 
			ClassContentView cdv, MethodContentViewStyle viewStyle, DexClassDetailViewer controller) {
		super(showing, method, codeitem, items, cdv, viewStyle, controller);

		this.showingOperands = new HashSet<InstSemOperand>();
	}
	
	protected void initAugmentations() {
		assert codeitem != null && codeitem.getProgram() != null;
		dataflow = DataFlowAnalyzer.get(codeitem.getProgram());
		deadcodes = DeadCodeCollector.get(codeitem.getProgram());
		typeinfer = OperandTypeInferer.get(codeitem.getProgram());
	}

	class OperandHighlightingDrawer implements LabelDrawer {
		
		private LabelDrawer drawer;
		private InstSemOperand operand;
		
		public OperandHighlightingDrawer(LabelDrawer drawer, InstSemOperand operand) {
			this.drawer = drawer;
			this.operand = operand;
		}
		
		@Override
		public int drawLabel(GC g, int left, int top, int vcenter, int height) {
			int width = drawer.drawLabel(g, left, top, vcenter, height);
			
			if (focus == operand) {
				g.setForeground(ColorConstants.black);
				g.drawRectangle(left - 1, top - 1, width + 2, height);
			}
			boolean gen = (gens == null)? false:gens.contains(operand);
			boolean use = (uses == null)? false:uses.contains(operand);
			if (gen && use) {
				g.setForeground(ColorConstants.red);
				g.drawLine(left, top + 1, width, top + 1);
				g.setForeground(ColorConstants.blue);
				g.drawLine(left, top + height - 1, width, top + height - 1);
			} else if (gen) {
				g.setForeground(ColorConstants.red);
				g.drawRectangle(left, top, width, height - 2);
			} else if (use) {
				g.setForeground(ColorConstants.blue);
				g.drawRectangle(left, top, width, height - 2);
			}
			return width;
		}
	}
	
	class InstSemClicked implements ItemGroupSelectionListener {
		
		private DexClassDetailViewer controller;
		private InstSemStatement sem;
		private DexClass showingClass;
	
		public InstSemClicked(DexClassDetailViewer controller, InstSemStatement sem, DexClass showingClass) {
			this.controller = controller;
			this.sem = sem;
			this.showingClass = showingClass;
		}
		
		@Override
		public void selected() {
			// TODO this is temporary
			controller.showInstruction(sem.getInstruction());
		}
	
		@Override
		public void deselected() {
			controller.showClass(showingClass);
		}
		
		@Override
		public void deselectedToMove() {
			// nothing to do
		}
	}
	
	private class RegisterOperandClickListener implements LabelClickListener {
		
		private InstSemOperand operand;
		
		public RegisterOperandClickListener(Label label, InstSemOperand operand) {
			this.operand = operand;
			
			label.setLabelDrawer(new OperandHighlightingDrawer(label.getLabelDrawer(), operand));
		}
		
		@Override
		public boolean labelClicked(LabelListWidget widget, int index, Label label,
				int x, int y, MouseEvent e) {
			if (focus == operand) {
				focus = null;
				gens = null;
				uses = null;
			} else {
				focus = operand;
				// System.out.println("clicked " + operand.getStringRepresentation() + " " + ((operand.getStatement() == null)? "(parameter)":Integer.toHexString(operand.getStatement().getInstruction().getAddress())));
				gens = new HashSet<InstSemOperand>();
				for (InstSemOperand op: dataflow.getGensOf(codeitem, operand)) {
					gens.addAll(propagateToShowingsByGens(op));
				}
				uses = new HashSet<InstSemOperand>();
				for (InstSemOperand op: dataflow.getUsesOf(codeitem, operand)) {
					uses.addAll(propagateToShowingsByUses(op));
				}
			}
			widget.redraw();
			return true;
		}
		
		private Set<InstSemOperand> propagateToShowingsByGens(InstSemOperand op) {
			// System.out.println("gen propagate " + op.getStringRepresentation() + " " + ((op.getStatement() == null)? "(parameter)":Integer.toHexString(op.getStatement().getInstruction().getAddress())));
			if (showingOperands.contains(op)) {
				// System.out.println("done");
				Set<InstSemOperand> set = new HashSet<InstSemOperand>();
				set.add(op);
				return set;
			} else {
				if (op.getStatement() == null) {
					return new HashSet<InstructionSemantic.InstSemOperand>();
				} else {
					Set<InstSemOperand> set = new HashSet<InstSemOperand>();
					for (InstSemOperand o: dataflow.getGensOf(codeitem, op)) {
						set.addAll(propagateToShowingsByGens(o));
					}
					return set;
				}
			}
		}

		private Set<InstSemOperand> propagateToShowingsByUses(InstSemOperand op) {
			// System.out.println("use propagate " + op.getStringRepresentation() + " " + ((op.getStatement() == null)? "(parameter)":Integer.toHexString(op.getStatement().getInstruction().getAddress())));
			if (showingOperands.contains(op)) {
				// System.out.println("done");
				Set<InstSemOperand> set = new HashSet<InstSemOperand>();
				set.add(op);
				return set;
			} else {
				if (op.getStatement() == null) {
					return new HashSet<InstructionSemantic.InstSemOperand>();
				} else {
					Set<InstSemOperand> set = new HashSet<InstSemOperand>();
					for (InstSemOperand o: dataflow.getUsesOf(codeitem, op)) {
						set.addAll(propagateToShowingsByUses(o));
					}
					return set;
				}
			}
		}

		@Override
		public boolean labelDoubleClicked(LabelListWidget widget, int index,
				Label label, int x, int y, MouseEvent e) {
			// nothing to do
			return false;
		}
	}

	Label getNameLabelForOperandSemantic(InstSemOperand operand) {
		return getNameLabelForOperandSemantic(operand, operand);
	}
	
	Label getNameLabelForOperandSemantic(InstSemOperand operand, InstSemOperand showing) {
		Label label;
		if (showing instanceof InstSemConst) {
			label = Label.newLabel(((InstSemConst) showing).getStringRepresentation(typeinfer.getInferredType(operand)));
		} else {
			label = Label.newLabel(showing.getStringRepresentation());
		}
		label.addClickListener(new RegisterOperandClickListener(label, operand));
		return postprocessOperandLabel(label, operand);
	}
	
	Label getOperandLabel(ArrayList<Label> sublabels, InstSemOperand operand) {
		Label label = Label.newLabel(sublabels.toArray(new Label[0]));
		label.setLabelDrawer(new OperandHighlightingDrawer(label.getLabelDrawer(), operand));
		return postprocessOperandLabel(label, operand);
	}
	
	private Label postprocessOperandLabel(Label label, final InstSemOperand operand) {
		label.addClickListener(new LabelClickListener() {
			
			@Override
			public boolean labelDoubleClicked(LabelListWidget widget, int index,
					Label label, int x, int y, MouseEvent e) {
				// nothing to do
				return false;
			}
			
			@Override
			public boolean labelClicked(LabelListWidget widget, int index, Label label, int x, int y, MouseEvent e) {
				System.out.println("=== " + operand.getStringRepresentation() + " " + ((operand.getStatement() == null)? "(parameter)":Integer.toHexString(operand.getStatement().getInstruction().getAddress())) + " : ");

				System.out.println("gens:");
				for (InstSemOperand op: dataflow.getGensOf(codeitem, operand)) {
					InstSemStatement statement = op.getStatement();
					if (statement == null) {
						System.out.println(op.getStringRepresentation() + " (parameter)");
					} else {
						System.out.println(op.getStringRepresentation() + " at " + Integer.toHexString(statement.getInstruction().getAddress()));
					}
				}
				System.out.println("uses:");
				for (InstSemOperand op: dataflow.getUsesOf(codeitem, operand)) {
					InstSemStatement statement = op.getStatement();
					if (statement == null) {
						System.out.println(op.getStringRepresentation() + " (parameter)");
					} else {
						System.out.println(op.getStringRepresentation() + " at " + Integer.toHexString(statement.getInstruction().getAddress()));
					}
				}

				DexType infer = typeinfer.getInferredType(operand);
				if (infer == null) {
					System.out.print("Unknown (");
				} else {
					System.out.print(infer.getTypeFullNameBeauty() + " (");
				}
				
				Set<DexType> types = typeinfer.getPossibleTypes(operand);
				if (types == null) {
					System.out.print("Undefined - should not be!)");
				} else {
					boolean first = true;
					for (DexType type: types) {
						if (! first) {
							System.out.print(", ");
						}
						System.out.print(type.getTypeFullNameBeauty());
						first = false;
					}
					System.out.print(")");
				}
				
				DexType typedata = operand.getOperandTypeData();
				if (typedata == null) {
					System.out.println(" - nothing");
				} else {
					System.out.println(" - " + typedata.getTypeFullNameBeauty());
				}
				return false;
			}
		});
		return label;
	}

	protected void instructionSemanticLabel(ClassContentView cdv, ArrayList<Label> labels, final InstructionSemantic sem, boolean replaced) {
		switch (sem.getInstSemType()) {
		case ARRAY_REFERENCE: {
			InstSemArrayReference arrayref = (InstSemArrayReference) sem;
			
			showingOperands.add(arrayref);
			
			ArrayList<Label> sublabels = new ArrayList<Label>();
			instructionSemanticLabel(cdv, sublabels, arrayref.getArray(), replaced);
			sublabels.add(Label.newLabel("["));
			instructionSemanticLabel(cdv, sublabels, arrayref.getIndex(), replaced);
			sublabels.add(Label.newLabel("]"));
			
			labels.add(getOperandLabel(sublabels, arrayref));
			return;
		}
		case ASSIGNMENT_STATEMENT: {
			InstSemAssignment assignment = (InstSemAssignment) sem;
			
			// assignment is statement (not operand)
			
			instructionSemanticLabel(cdv, labels, assignment.getLefthandside(), replaced);
			labels.add(Label.newLabel(" = "));
			instructionSemanticLabel(cdv, labels, assignment.getRighthandside(), replaced);
			return;
		}
		case BINARY_OP: {
			InstSemBinaryOp binop = (InstSemBinaryOp) sem;
			
			showingOperands.add(binop);
			
			ArrayList<Label> sublabels = new ArrayList<Label>();
			instructionSemanticLabel(cdv, sublabels, binop.getFirstOperand(), replaced);
			sublabels.add(Label.newLabel(" " + binop.getOperatorStringRepresentation() + " "));
			instructionSemanticLabel(cdv, sublabels, binop.getSecondOperand(), replaced);
			
			labels.add(getOperandLabel(sublabels, binop));
			return;
		}
		case CONST: {
			InstSemConst cons = (InstSemConst) sem;
			
			showingOperands.add(cons);
			
			labels.add(getNameLabelForOperandSemantic(cons));
			return;
		}
		case ETC_STATEMENT: {
			InstSemEtcStatement etc = (InstSemEtcStatement) sem;
			
			// etc is statement (not operand)
			
			labels.add(Label.newLabel(etc.getOpcodeMnemonic()));
			labels.add(Label.newLabel(" "));
			InstSemElement elems[] = etc.getUseOperands();
			for (int i = 0; i < elems.length; i++) {
				if (i > 0) {
					labels.add(Label.newLabel(", "));
				}
				instructionSemanticLabel(cdv, labels, elems[i], replaced);
			}
			return;
		}
		case INSTANCE_FIELD: {
			InstSemInstanceField ifield = (InstSemInstanceField) sem;
			DexField field = ifield.getField();
			
			showingOperands.add(ifield);
			
			ArrayList<Label> sublabels = new ArrayList<Label>();
			
			if (field != null) {
				if (typeinfer.getInferredType(ifield.getInstance()) != field.getBelongedClass()) {
					sublabels.add(Label.newLabel("("));
					sublabels.add(Label.newLabel("("));
					sublabels.add(getNameLabelForType(field.getBelongedClass()));
					sublabels.add(Label.newLabel(")"));
					instructionSemanticLabel(cdv, sublabels, ifield.getInstance(), replaced);
					sublabels.add(Label.newLabel(")"));
				} else {
					instructionSemanticLabel(cdv, sublabels, ifield.getInstance(), replaced);
				}
				sublabels.add(Label.newLabel("."));
				sublabels.add(getNameLabelForField(field, false));
			} else {
				instructionSemanticLabel(cdv, sublabels, ifield.getInstance(), replaced);
				sublabels.add(Label.newLabel("."));
				sublabels.add(Label.newLabel(ifield.getFieldName()));
			}

			labels.add(getOperandLabel(sublabels, ifield));
			return;
		}
		case INSTANCE_METHOD_INVOKE: {
			InstSemInstanceMethodInvoke invoke = (InstSemInstanceMethodInvoke) sem;
			InstSemOperand instance = invoke.getInstance();
			DexMethod method = invoke.getMethod();
			InstSemOperand args[] = invoke.getArguments();
			
			showingOperands.add(invoke);
			
			ArrayList<Label> sublabels = new ArrayList<Label>();
			
			if (typeinfer.getInferredType(instance) != method.getBelongedType()) {
				sublabels.add(Label.newLabel("("));
				sublabels.add(Label.newLabel("("));
				sublabels.add(getNameLabelForType(method.getBelongedType()));
				sublabels.add(Label.newLabel(")"));
				instructionSemanticLabel(cdv, sublabels, instance, replaced);
				sublabels.add(Label.newLabel(")"));
			} else {
				instructionSemanticLabel(cdv, sublabels, instance, replaced);
			}
			sublabels.add(Label.newLabel("."));
			sublabels.add(getNameLabelForMethod(method, false));
			sublabels.add(Label.newLabel("("));
			for (int i = 0; i < args.length; i++) {
				if (i > 0) {
					sublabels.add(Label.newLabel(", "));
				}
				instructionSemanticLabel(cdv, sublabels, args[i], replaced);
			}
			sublabels.add(Label.newLabel(")"));

			labels.add(getOperandLabel(sublabels, invoke));
			return;
		}
		case NEW_ARRAY: {
			InstSemNewArray newarray = (InstSemNewArray) sem;
			
			showingOperands.add(newarray);
			
			DexType type = newarray.getType().getElementType();
			InstSemOperand size = newarray.getSize();
			InstSemOperand items[] = newarray.getItems();
			ArrayList<Label> sublabels = new ArrayList<Label>();
			
			sublabels.add(Label.newLabel("new"));
			sublabels.add(Label.newLabel(" "));
			sublabels.add(getNameLabelForType(type));			// TODO type의 element type으로 만들어야 함
			if (size != null) {
				sublabels.add(Label.newLabel("["));
				instructionSemanticLabel(cdv, sublabels, size, replaced);
				sublabels.add(Label.newLabel("]"));
			} else {
				sublabels.add(Label.newLabel("["));
				sublabels.add(Label.newLabel("]"));
				sublabels.add(Label.newLabel(" "));
				sublabels.add(Label.newLabel("{"));
				for (int i = 0; i < items.length; i++) {
					if (i > 0) {
						sublabels.add(Label.newLabel(", "));
					}
					instructionSemanticLabel(cdv, sublabels, items[i], replaced);
				}
				sublabels.add(Label.newLabel("}"));
			}

			labels.add(getOperandLabel(sublabels, newarray));
			return;
		}
		case REGISTER: case REGISTER_PAIR: {
			InstSemRegister register = (InstSemRegister) sem;
			InstSemOperand rep = dataflow.getReplacementOf(register);
			if ((! replaced) || rep == null) {
				showingOperands.add(register);
				
				Label registerlabel = getNameLabelForOperandSemantic(register);
				labels.add(registerlabel);
			} else {
				showingOperands.add(register);
				
				Label registerlabel = getNameLabelForOperandSemantic(register, rep);
				labels.add(registerlabel);
				registerlabel.addClickListener(new LabelClickListener() {
					
					@Override
					public boolean labelDoubleClicked(LabelListWidget widget, int index,
							Label label, int x, int y, MouseEvent e) {
						// nothing to do
						return false;
					}
					
					@Override
					public boolean labelClicked(LabelListWidget widget, int index, Label label,
							int x, int y, MouseEvent e) {
						// System.out.println("It was " + sem.getStringRepresentation());
						return false;
					}
				});
			}
			return;
		}
		case STATIC_FIELD: {
			InstSemStaticField sfield = (InstSemStaticField) sem;
			
			showingOperands.add(sfield);
			
			Label label = getNameLabelForField(sfield.getField(), true);
			label.setLabelDrawer(new OperandHighlightingDrawer(label.getLabelDrawer(), sfield));
			labels.add(postprocessOperandLabel(label, sfield));
			return;
		}
		case STATIC_METHOD_INVOKE: {
			InstSemStaticMethodInvoke sinvoke = (InstSemStaticMethodInvoke) sem;
			DexMethod method = sinvoke.getMethod();
			InstSemOperand args[] = sinvoke.getArguments();
			
			showingOperands.add(sinvoke);
			
			ArrayList<Label> sublabels = new ArrayList<Label>();
			
			sublabels.add(getNameLabelForMethod(method, true));
			sublabels.add(Label.newLabel("("));
			for (int i = 0; i < args.length; i++) {
				if (i > 0) {
					sublabels.add(Label.newLabel(", "));
				}
				instructionSemanticLabel(cdv, sublabels, args[i], replaced);
			}
			sublabels.add(Label.newLabel(")"));
			
			labels.add(getOperandLabel(sublabels, sinvoke));
			return;
		}
		case UNARY_OP: {
			InstSemUnaryOp unop = (InstSemUnaryOp) sem;
			UnaryOperator operator = unop.getOperator();
			
			showingOperands.add(unop);
			
			ArrayList<Label> sublabels = new ArrayList<Label>();
			sublabels.add(Label.newLabel(operator.toString()));
			sublabels.add(Label.newLabel(" "));
			instructionSemanticLabel(cdv, sublabels, unop.getOperand(), replaced);
			
			labels.add(getOperandLabel(sublabels, unop));
			return;
		}
		case NEW_INSTANCE: {
			InstSemNewInstance newinstance = (InstSemNewInstance) sem;
			
			showingOperands.add(newinstance);
			
			ArrayList<Label> sublabels = new ArrayList<Label>();
			sublabels.add(Label.newLabel("new "));
			sublabels.add(getNameLabelForType(newinstance.getType()));
			
			labels.add(getOperandLabel(sublabels, newinstance));
			return;
		}
		case ETC_OPERAND: {
			InstSemEtcOperand etc = (InstSemEtcOperand) sem;
			
			showingOperands.add(etc);
			
			Label label = Label.newLabel(etc.getStringRepresentation());
			label.setLabelDrawer(new OperandHighlightingDrawer(label.getLabelDrawer(), etc));
			labels.add(postprocessOperandLabel(label, etc));
			return;
		}
		default:
			assert false;
		}
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
			instructionSemanticLabel(cdv, instructionLabel, sem, false);
			cdv.addItemGroup(cdv.new ItemGroup(items.size(), new InstSemClicked(controller, sem, showing)));
			items.add(Label.newLabel(instructionLabel.toArray(new Label[0])));
		}
	}

	@Override
	Label getParameterLabel(DexParameter parameter) {
		DexCodeItem codeitem = method.getCodeItem();
		
		if (codeitem == null) {
			return Label.newLabel(parameter.getRegister().getName());
		} else {
			MethodSemantics methodsemantics = InstructionSemanticizer.get(method.getProgram()).getMethodSemantics(codeitem);
			
			InstSemParameter paramobj = methodsemantics.getParameterForRegister(parameter.getRegister());
			
			showingOperands.add(paramobj);
			
			return getNameLabelForOperandSemantic(paramobj);
		}
	}

	@Override
	public Label getInstanceRegisterLabel() {
		MethodSemantics methodsemantics = InstructionSemanticizer.get(method.getProgram()).getMethodSemantics(method.getCodeItem());
		InstSemParameter instancereg = methodsemantics.getParameterForRegister(codeitem.getInstanceRegister());

		showingOperands.add(instancereg);
		
		Label label = Label.newLabel(instancereg.getStringRepresentation());
		label.setLabelDrawer(new OperandHighlightingDrawer(label.getLabelDrawer(), instancereg));
		
		return label;
	}
}
