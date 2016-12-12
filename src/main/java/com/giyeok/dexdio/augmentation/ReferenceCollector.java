package com.giyeok.dexdio.augmentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstCheckCast;
import com.giyeok.dexdio.model.insns.DexInstFilledNewArray;
import com.giyeok.dexdio.model.insns.DexInstInstanceOf;
import com.giyeok.dexdio.model.insns.DexInstInstanceOp;
import com.giyeok.dexdio.model.insns.DexInstInvoke;
import com.giyeok.dexdio.model.insns.DexInstMoveConst;
import com.giyeok.dexdio.model.insns.DexInstNewArray;
import com.giyeok.dexdio.model.insns.DexInstNewInstance;
import com.giyeok.dexdio.model.insns.DexInstStaticOp;
import com.giyeok.dexdio.model.insns.DexInstruction;

/**
 * 각 메소드를 호출하는 인스트럭션, 필드를 참조하는 인스트럭션, 타입을 참조하는 인스트럭션을 찾는다 
 * 
 * @author Joonsoo
 *
 */
public class ReferenceCollector extends Augmentation {

	public static ReferenceCollector get(DexProgram program) {
		return (ReferenceCollector) Augmentation.getAugmentation(ReferenceCollector.class, program);
	}
	
	@Override
	protected String getAugmentationName() {
		return "Reference collector";
	}
	
	private Map<DexMethod, ArrayList<DexInstruction>> methodscallers;
	private Map<DexField, ArrayList<DexInstruction>> fieldreferences;
	private Map<DexType, ArrayList<DexInstruction>> typereferences;
	
	public ReferenceCollector(DexProgram program) {
		super(program);
		
		methodscallers = new HashMap<DexMethod, ArrayList<DexInstruction>>();
		fieldreferences = new HashMap<DexField, ArrayList<DexInstruction>>();
		typereferences = new HashMap<DexType, ArrayList<DexInstruction>>();
		
		visit(program);
	}
	
	public ArrayList<DexInstruction> getMethodCallers(DexMethod callee) {
		return methodscallers.get(callee);
	}
	
	public ArrayList<DexInstruction> getFieldReferences(DexField field) {
		return fieldreferences.get(field);
	}
	
	private void visit(DexProgram program) {
		// collect method callers
		visitInstructions(program, new InstructionVisitor() {

			@Override
			public void visit(DexProgram program, DexMethod method, DexInstruction instruction) {
				if (instruction instanceof DexInstInvoke) {
					DexMethod callee = ((DexInstInvoke) instruction).getInvokingMethod();
					
					ArrayList<DexInstruction> callers = methodscallers.get(callee);
					if (callers == null) {
						callers = new ArrayList<DexInstruction>();
						methodscallers.put(callee, callers);
					}
					callers.add(instruction);
				}
			}
		});
		
		// collect field references
		visitInstructions(program, new InstructionVisitor() {
			
			@Override
			public void visit(DexProgram program, DexMethod method,
					DexInstruction instruction) {
				DexField refd = null;
				
				switch (instruction.getInstructionType()) {
				case INSTANCE_OP:					// instanceop
					refd = ((DexInstInstanceOp) instruction).getField();
					break;
				case STATIC_OP:						// staticop
					refd = ((DexInstStaticOp) instruction).getField();
					break;
				}
				
				if (refd != null) {
					ArrayList<DexInstruction> refs = fieldreferences.get(refd);
					if (refs == null) {
						refs = new ArrayList<DexInstruction>();
						fieldreferences.put(refd, refs);
					}
					refs.add(instruction);
				}
			}
		});
		
		// collect type references
		visitInstructions(program, new InstructionVisitor() {
			
			@Override
			public void visit(DexProgram program, DexMethod method,
					DexInstruction instruction) {
				DexType refd = null;
				
				switch (instruction.getInstructionType()) {
				case MOVE_CONST:					// const-class
					refd = ((DexInstMoveConst) instruction).getConstClassType();
					break;
				case CHECK_CAST:					// check-cast
					refd = ((DexInstCheckCast) instruction).getType();
					break;
				case INSTANCE_OF:					// instance-of
					refd = ((DexInstInstanceOf) instruction).getType();
					break;
				case NEW_INSTANCE:					// new-instance
					refd = ((DexInstNewInstance) instruction).getType();
					break;
				case NEW_ARRAY:						// new-array
					refd = ((DexInstNewArray) instruction).getType();
					break;
				case FILLED_NEW_ARRAY:				// filled-new-array
					refd = ((DexInstFilledNewArray) instruction).getType();
					break;
				}
				
				if (refd != null) {
					ArrayList<DexInstruction> refs = typereferences.get(refd);
					if (refs == null) {
						refs = new ArrayList<DexInstruction>();
						typereferences.put(refd, refs);
					}
					refs.add(instruction);
				}
			}
		});
	}
}
