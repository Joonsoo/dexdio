package com.giyeok.dexdio.augmentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.giyeok.dexdio.augmentation.Logger.Log;
import com.giyeok.dexdio.augmentation.instsem.InstSemArrayReference;
import com.giyeok.dexdio.augmentation.instsem.InstSemAssignment;
import com.giyeok.dexdio.augmentation.instsem.InstSemConst;
import com.giyeok.dexdio.augmentation.instsem.InstSemParameter;
import com.giyeok.dexdio.augmentation.instsem.InstSemRegister;
import com.giyeok.dexdio.augmentation.instsem.InstSemStatement;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemLHS;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemType;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer.MethodSemantics;
import com.giyeok.dexdio.model.DexArrayType;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexInternalClass;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;

/**
 * data flow analysis 결과를 이용하여 각 오퍼랜드의 타입을 유추한다
 * 
 * @author Joonsoo
 *
 */
public class OperandTypeInferer extends Augmentation {

	public static OperandTypeInferer get(DexProgram program) {
		return (OperandTypeInferer) Augmentation.getAugmentation(OperandTypeInferer.class, program);
	}
	
	@Override
	protected String getAugmentationName() {
		return "Operand type inferer";
	}
	
	private InstructionSemanticizer semantics;
	private DataFlowAnalyzer dataflow;
	
	private Map<DexCodeItem, OperandTypesInMethod> methods;
	private Map<InstSemOperand, DexType> inferred;
	
	public OperandTypeInferer(DexProgram program) {
		super(program);
		
		semantics = InstructionSemanticizer.get(program);
		dataflow = DataFlowAnalyzer.get(program);
		
		methods = new HashMap<DexCodeItem, OperandTypesInMethod>();
		inferred = new HashMap<InstSemOperand, DexType>();

		visit(program);
	}
	
	public DexType getInferredType(InstSemOperand operand) {
		return inferred.get(operand);
	}
	
	public Set<DexType> getPossibleTypes(InstSemOperand operand) {
		OperandTypesInMethod m = methods.get(operand.getBelongedCodeItem());
		assert m != null;
		return m.progress.get(operand);
	}
	
	private void visit(DexProgram program) {
		visitMethods(program, new MethodVisitor() {
			
			@Override
			public void visit(DexProgram program, DexMethod method) {
				DexCodeItem codeitem = method.getCodeItem();
				
				if (codeitem != null) {
					methods.put(codeitem, new OperandTypesInMethod(codeitem, semantics.getMethodSemantics(codeitem)));
				}
			}
		});
	}
	
	private class OperandTypesInMethod {
		private Map<InstSemOperand, Set<DexType>> progress;
		
		public OperandTypesInMethod(DexCodeItem codeitem, MethodSemantics methsem) {
			progress = new HashMap<InstSemOperand, Set<DexType>>();
			
			process(codeitem, methsem);
		}
		
		private Set<DexType> getProgressOf(InstSemOperand operand) {
			Set<DexType> set = progress.get(operand);
			
			if (set == null) {
				set = new HashSet<DexType>();
				progress.put(operand, set);
			}
			return set;
		}
		
		/**
		 * 각 operand에 대해서 gen의 타입을 use에 추가한다
		 * 변화가 생기면 true를 반환하고 그렇지 않으면 false를 반환한다
		 * @return
		 */
		private boolean propagate(DexCodeItem codeitem) {
			boolean updated = false;
			for (InstSemOperand operand: progress.keySet()) {
				InstSemOperand showing = null;
				
				if (operand instanceof InstSemRegister) {
					showing = dataflow.getReplacementOf((InstSemRegister) operand);
					if (showing == null) {
						showing = operand;
					}
					if (! (showing instanceof InstSemConst)) {
						Set<DexType> prog = getProgressOf(operand);
						for (InstSemOperand gen: dataflow.getGensOf(codeitem, operand)) {
							if (operand.getInstSemType() == InstSemType.REGISTER_PAIR && gen.getInstSemType() == InstSemType.REGISTER) {
								Logger.addMessage(OperandTypeInferer.this, Log.line(new Log[] {
										Log.log("Register pair depends on a register-non-pair: "),
										Log.log(" " + getLoggingString(operand) + " at "),
										Log.log(codeitem.getBelongedMethod())
								}));
							}
							Set<DexType> genset = getProgressOf(gen);
							updated |= prog.addAll(genset);
						}
					}
				}
				// TODO needs verify
				for (InstSemOperand use: dataflow.getUsesOf(codeitem, operand)) {
					if (use instanceof InstSemRegister) {
						if (use.getInstSemType() == InstSemType.REGISTER_PAIR && operand.getInstSemType() == InstSemType.REGISTER) {
							Logger.addMessage(OperandTypeInferer.this, Log.line(new Log[] {
									Log.log("Register pair depends on a register-non-pair: "),
									Log.log(" " + getLoggingString(operand) + " at "),
									Log.log(codeitem.getBelongedMethod())
							}));
						}
						updated |= getProgressOf(use).addAll(getProgressOf(operand));
					}
				}
			}
			return updated;
		}
		
		private boolean processArrayReference(InstSemArrayReference arrayref) {
			// getProgressOf(arrayref.getArray());	=> 여기에는 array type이 들어 있어야 하고, 이 타입들로부터
			// getProgressOf(arrayref)				=> 여기에 array type들의 element type을 넣어 주어야 한다
			for (DexType type: getProgressOf(arrayref.getArray())) {
				if (type instanceof DexArrayType) {
					getProgressOf(arrayref).add(((DexArrayType) type).getElementType());
					// System.out.println(((DexArrayType) type).getElementType().getTypeFullNameBeauty());
				} else {
					System.out.println("Why! " + type.getTypeFullNameBeauty());
				}
			}
			return false;
		}
		
		private void process(DexCodeItem codeitem, MethodSemantics methsem) {
			InstSemStatement sem[];
			sem = methsem.getStatements();
			
			// 모든 오퍼랜드를 수집하고 기본 타입 정보를 저장한다
			for (InstSemParameter param: methsem.getParametersIncludingInstanceRegister()) {
				Set<DexType> set = getProgressOf(param);
				DexType type = param.getOperandTypeData();
				if (type != null) {
					set.add(type);
				}
			}
			for (int i = 0; i < sem.length; i++) {
				InstSemStatement statement = sem[i];
				InstSemOperand ops[] = statement.getContainingOperands();
				
				for (InstSemOperand op: ops) {
					Set<DexType> set = getProgressOf(op);
					DexType type = op.getOperandTypeData();
					if (type != null) {
						set.add(type);
					}
				}
			}
			// TODO check-cast 인스트럭션이 실행된 이후부터 ClassCastException 예외 처리 핸들러에서 다시 돌아올 때까진 타입이 지정되었다고 볼 수 있다
	
			// propagation과 정의 과정에선 처리되지 못한 instsem에 대한 타입 처리를 converge할 때까지 반복 수행한다
			boolean updated = true;
			
			while (updated) {
				updated = propagate(codeitem);
				for (int i = 0; i < sem.length; i++) {
					InstSemStatement statement = sem[i];
					
					switch (statement.getInstSemType()) {
					case ASSIGNMENT_STATEMENT: {
						InstSemAssignment assign = (InstSemAssignment) statement;
						InstSemLHS lhs = assign.getLefthandside();
						InstSemOperand rhs = assign.getRighthandside();
						
						if (lhs instanceof InstSemArrayReference) {
							updated |= processArrayReference((InstSemArrayReference) lhs);
							updated |= getProgressOf(rhs).addAll(getProgressOf(lhs));
						}
						if (rhs instanceof InstSemArrayReference) {
							updated |= processArrayReference((InstSemArrayReference) rhs);
						}
						break;
					}
					}
				}
			}
			
			// type inference 정보를 저장한다
			DeadCodeCollector deadcodes = DeadCodeCollector.get(getProgram());
			for (InstSemOperand operand: progress.keySet()) {
				Set<DexType> set = getProgressOf(operand);
				DexType inferredtype = getMostSpecificTypeAmong(set);
				
				if (inferredtype != null) {
					inferred.put(operand, inferredtype);
					/*
					AugmentationMessages.addMessage(OperandTypeInferer.this, Message.line(new Message[] {
							Message.msg("Known: "),
							Message.msg(codeitem.getBelongedMethod()),
							Message.msg(" " + getLoggingString(operand) + " : "),
							Message.msg(inferredtype)
					}));
					*/
				} else {
					if (operand instanceof InstSemRegister && (! deadcodes.isDeadInstruction(operand.getStatement()))) {
						ArrayList<Log> msg = new ArrayList<Log>();
						msg.add(Log.log(codeitem.getBelongedMethod()));
						msg.add(Log.log(" " + getLoggingString(operand) + " : "));
						Iterator<DexType> type = set.iterator();
						boolean first = true;
						while (type.hasNext()) {
							if (! first) {
								msg.add(Log.log(", "));
							}
							msg.add(Log.log(type.next()));
							first = false;
						}
						Logger.addMessage(OperandTypeInferer.this, Log.line(msg.toArray(new Log[0])));
					}
				}
			}
		}
		
		private DexType getMostSpecificTypeAmong(Set<DexType> types) {
			DexType result = null;
			
			for (DexType type: types) {
				if (result == null) {
					result = type;
				} else {
					if ((! (result instanceof DexClass)) || (! (type instanceof DexClass))) {
						return null;
					}
					DexClass a = (DexClass) result, b = (DexClass) type;
					if (a instanceof DexInternalClass && b instanceof DexInternalClass) {
						if (b.isAncestorOf((DexInternalClass) a)) {
							assert ! a.isAncestorOf((DexInternalClass) b);
							result = a;
						} else if (a.isAncestorOf((DexInternalClass) b)) {
							assert ! b.isAncestorOf((DexInternalClass) a);
							result = b;
						} else {
							return null;
						}
					} else if (a instanceof DexInternalClass) {
						if (b.isAncestorOf((DexInternalClass) a)) {
							result = a;
						}
					} else if (b instanceof DexInternalClass) {
						if (a.isAncestorOf((DexInternalClass) b)) {
							result = b;
						}
					} else {
						return null;
					}
				}
			}
			return result;
		}
		
		private String getLoggingString(InstSemOperand op) {
			return op.getStringRepresentation() + " " + 
					((op.getStatement() == null)? 
							"(parameter)":Integer.toHexString(op.getStatement().getInstruction().getAddress()));
		}
	}
}
