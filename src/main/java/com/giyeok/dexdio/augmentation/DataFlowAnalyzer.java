package com.giyeok.dexdio.augmentation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.CodeSet;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.CodeSetVisitor;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.MutableVariable;
import com.giyeok.dexdio.augmentation.instsem.InstSemArrayReference;
import com.giyeok.dexdio.augmentation.instsem.InstSemAssignment;
import com.giyeok.dexdio.augmentation.instsem.InstSemBinaryOp;
import com.giyeok.dexdio.augmentation.instsem.InstSemConst;
import com.giyeok.dexdio.augmentation.instsem.InstSemEtcStatement;
import com.giyeok.dexdio.augmentation.instsem.InstSemInstanceField;
import com.giyeok.dexdio.augmentation.instsem.InstSemInstanceMethodInvoke;
import com.giyeok.dexdio.augmentation.instsem.InstSemNewArray;
import com.giyeok.dexdio.augmentation.instsem.InstSemParameter;
import com.giyeok.dexdio.augmentation.instsem.InstSemRegister;
import com.giyeok.dexdio.augmentation.instsem.InstSemRegisterPair;
import com.giyeok.dexdio.augmentation.instsem.InstSemStatement;
import com.giyeok.dexdio.augmentation.instsem.InstSemStaticField;
import com.giyeok.dexdio.augmentation.instsem.InstSemStaticMethodInvoke;
import com.giyeok.dexdio.augmentation.instsem.InstSemUnaryOp;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemElement;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemLHS;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemanticizer.MethodSemantics;
import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexMethod;
import com.giyeok.dexdio.model0.DexProgram;
import com.giyeok.dexdio.model0.insns.DexInstMoveResult;
import com.giyeok.dexdio.model0.insns.DexInstNop;
import com.giyeok.dexdio.model0.insns.DexInstruction;
import com.giyeok.dexdio.util.ArraysUtil;

/**
 * data flow analysis 결과를 이용하여 상수로 사용되는 오퍼랜드를 찾는다
 * 
 * @author Joonsoo
 *
 */
public class DataFlowAnalyzer extends Augmentation {

	public static DataFlowAnalyzer get(DexProgram program) {
		return (DataFlowAnalyzer) Augmentation.getAugmentation(DataFlowAnalyzer.class, program);
	}
	
	@Override
	protected String getAugmentationName() {
		return "Augmentation messages";
	}
	
	public DataFlowAnalyzer(DexProgram program) {
		super(program);
		
		visit(program);
	}
	
	private DexProgram program;
	
	private InstructionSemanticizer semantics;
	private DeadCodeCollector deads;
	
	private Map<DexCodeItem, CodeItemAnalysis> analyses;
	
	private void visit(DexProgram program) {
		this.program = program;
		
		semantics = InstructionSemanticizer.get(program);
		deads = DeadCodeCollector.get(program);
		
		analyses = new HashMap<DexCodeItem, DataFlowAnalyzer.CodeItemAnalysis>();
		
		visitMethods(program, new MethodVisitor() {
			
			@Override
			public void visit(DexProgram program, DexMethod method) {
				DexCodeItem codeitem = method.getCodeItem();
				if (codeitem != null) {
					InstSemStatement sem[] = semantics.getSemantics(codeitem);
					InstSemParameter params[] = semantics.getMethodSemantics(codeitem).getParametersIncludingInstanceRegister();
					assert sem != null && params != null;
					analyses.put(codeitem, new CodeItemAnalysis(codeitem, sem, params));
				}
			}
		});
	}
	
	public InstSemOperand[] getGensOf(DexCodeItem codeitem, InstSemOperand operand) {
		if (operand.getStatement() == null) {
			return new InstSemOperand[0];
		}
		CodeItemAnalysis analysis = analyses.get(codeitem);
		
		if (analysis == null) {
			return null;
		}
		return analysis.getGensOf(operand);
	}
	
	public InstSemOperand[] getUsesOf(DexCodeItem codeitem, InstSemOperand operand) {
		CodeItemAnalysis analysis = analyses.get(codeitem);
		
		if (analysis == null) {
			return null;
		}
		return analysis.getUsesOf(operand);
	}

	public InstSemOperand getReplacementOf(InstSemRegister register) {
		CodeItemAnalysis analysis = analyses.get(register.getStatement().getInstruction().getCodeItem());
		
		if (analysis == null) {
			return null;
		}
		return analysis.getReplacementOf(register);
	}
	
	private class InstSemElementEdge {
		private InstSemOperand gen;
		private InstSemOperand use;
		
		InstSemElementEdge(InstSemOperand gen, InstSemOperand use) {
			this.gen = gen;
			this.use = use;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof InstSemElementEdge) {
				InstSemElementEdge otheredge = (InstSemElementEdge) other;
				return otheredge.gen == this.gen && otheredge.use == this.use;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return gen.hashCode() * 1000 + use.hashCode();
		}
	}
	
	private class CodeItemAnalysis {
		
		CodeItemAnalysis(DexCodeItem codeitem, InstSemStatement sem[], InstSemParameter params[]) {
			process(codeitem, sem, params);
		}
		
		public InstSemOperand[] getGensOf(InstSemOperand operand) {
			Set<InstSemOperand> defs = new HashSet<InstSemOperand>();
			
			for (InstSemElementEdge edge: edges) {
				if (edge.use == operand) {
					defs.add(edge.gen);
				}
			}
			return defs.toArray(new InstSemOperand[0]);
		}
		
		public InstSemOperand[] getUsesOf(InstSemOperand operand) {
			Set<InstSemOperand> uses = new HashSet<InstSemOperand>();
			
			for (InstSemElementEdge edge: edges) {
				if (edge.gen == operand) {
					uses.add(edge.use);
				}
			}
			return uses.toArray(new InstSemOperand[0]);
		}
		
		/**
		 * register를 다른 레지스터나 상수로 치환할 수 있으면 치환할 다른 레지스터나 상수를 반환하고, 치환할 수 없으면 null을 반환한다
		 * @param register
		 * @return
		 */
		public InstSemOperand getReplacementOf(InstSemRegister register) {
			return replacements.get(register);
		}
		
		private Map<DexInstruction, InstSemStatement> instructionToInstSem;
		private Map<InstSemStatement, Map<DexRegister, Set<InstSemOperand>>> ins;
		private Map<InstSemStatement, Map<DexRegister, Set<InstSemOperand>>> outs;
		private Set<InstSemElementEdge> edges;
		private Map<InstSemRegister, InstSemOperand> replacements;
		
		private Map<DexRegister, Set<InstSemOperand>> getBeforeStatementDefSetMap(InstSemStatement statement) {
			Map<DexRegister, Set<InstSemOperand>> map = ins.get(statement);
			
			if (map == null) {
				map = new HashMap<DexCodeItem.DexRegister, Set<InstSemOperand>>();
				ins.put(statement, map);
			}
			
			return map;
		}
		
		private Set<InstSemOperand> getBeforeStatementRegisterDefSet(InstSemStatement statement, DexRegister register) {
			Map<DexRegister, Set<InstSemOperand>> map = getBeforeStatementDefSetMap(statement);
			Set<InstSemOperand> set = map.get(register);
			
			if (set == null) {
				set = new HashSet<InstructionSemantic.InstSemOperand>();
				map.put(register, set);
			}
			
			return set;
		}
		
		private Map<DexRegister, Set<InstSemOperand>> getAfterStatementDefSetMap(InstSemStatement statement) {
			Map<DexRegister, Set<InstSemOperand>> map = outs.get(statement);
			
			if (map == null) {
				map = new HashMap<DexCodeItem.DexRegister, Set<InstSemOperand>>();
				outs.put(statement, map);
			}
			
			return map;
		}
		
		private Set<InstSemOperand> getAfterStatementRegisterDefSet(InstSemStatement statement, DexRegister register) {
			Map<DexRegister, Set<InstSemOperand>> map = getAfterStatementDefSetMap(statement);
			Set<InstSemOperand> set = map.get(register);
			
			if (set == null) {
				set = new HashSet<InstructionSemantic.InstSemOperand>();
				map.put(register, set);
			}
			
			return set;
		}
		
		private void process(DexCodeItem codeitem, InstSemStatement sem[], InstSemParameter params[]) {
			// prepare
			edges = new HashSet<DataFlowAnalyzer.InstSemElementEdge>();
			ins = new HashMap<InstSemStatement, Map<DexRegister,Set<InstSemOperand>>>();
			outs = new HashMap<InstSemStatement, Map<DexRegister,Set<InstSemOperand>>>();
			instructionToInstSem = new HashMap<DexInstruction, InstSemStatement>();
			for (int i = 0; i < sem.length; i++) {
				instructionToInstSem.put(sem[i].getInstruction(), sem[i]);
			}
			
			// adding gen items from parameters
			for (InstSemParameter param: params) {
				if (param.isWide()) {
					getBeforeStatementRegisterDefSet(sem[0], param.getRegister()).add(param);
					getBeforeStatementRegisterDefSet(sem[0], param.getRegister().getNextRegister()).add(param);
				} else {
					getBeforeStatementRegisterDefSet(sem[0], param.getRegister()).add(param);
				}
			}
			
			// make dependency graph
			updated = true;
			while (updated) {
				updated = false;
				for (int i = 0; i < sem.length; i++) {
					process(sem[i]);
					
					// outs.get(sem[i])를 sem[i] 다음에 분기될 수 있는 지점의 ins에 병합
					Map<DexRegister, Set<InstSemOperand>> out = outs.get(sem[i]);
					if (out != null) {
						for (DexInstruction j: sem[i].getInstruction().getPossibleNextInstructions()) {
							DexInstruction nextinstruction = j;
							while (instructionToInstSem.get(nextinstruction) == null) {
								assert nextinstruction instanceof DexInstNop || nextinstruction instanceof DexInstMoveResult;
								DexInstruction ns[] = nextinstruction.getPossibleNextInstructions();
								if (ns.length == 0) {
									continue;
								}
								assert ns.length == 1;
								nextinstruction = ns[0];
							}
							InstSemStatement next = instructionToInstSem.get(nextinstruction);
							// merge outs.get(sem[i]) into ins.get(next)
							for (Entry<DexRegister, Set<InstSemOperand>> entry: out.entrySet()) {
								for (InstSemOperand op: entry.getValue()) {
									updated |= getBeforeStatementRegisterDefSet(next, entry.getKey()).add(op);
								}
							}
						}
					}
				}
			}
			
			// find replacables
			replacements = new HashMap<InstSemRegister, InstructionSemantic.InstSemOperand>();
			boolean upd = true;
			ControlFlowAnalysis cfg = ControlFlowAnalyzer.get(program).getControlFlowForMethod(codeitem);
			while (upd) {
				upd = false;
				for (int i = 0; i < sem.length; i++) {
					if (! deads.isDeadInstruction(sem[i])){
						if (sem[i] instanceof InstSemAssignment) {
							InstSemAssignment assign = (InstSemAssignment) sem[i];
							InstSemLHS lhs = assign.getLefthandside();
							final InstSemOperand rhs = assign.getRighthandside();
							
							if (lhs instanceof InstSemRegister) {
								InstSemRegister lreg = (InstSemRegister) lhs;
								InstSemOperand uses[] = getUsesOf(lreg);
								
								if (uses.length == 0) {
									deads.addDeadInstruction(sem[i]);
									upd = true;
								} else if (rhs instanceof InstSemConst) {
									boolean allReplacable = true;
									for (InstSemOperand use: uses) {
										if (! (use instanceof InstSemRegister)) {
											assert false;
											allReplacable = false;
											break;
										}

										InstSemOperand gens[] = getGensOf((InstSemRegister) use);
										assert gens.length >= 1 && ArraysUtil.indexOfExact(gens, lreg) >= 0;
										if (gens.length == 1) {
											replacements.put((InstSemRegister) use, rhs);
										} else {
											allReplacable = false;
										}
									}
									if (allReplacable) {
										deads.addDeadInstruction(sem[i]);
										upd = true;
									}
								} else if (rhs instanceof InstSemRegister) {
									// TODO rhs가 InstSemLHS이면 arrayop, instancefield, staticfield를 모두 치환한다.
									// 하지만 register 외에 LHS에 해당하는 arrayop, instancefield, staticfield도 단일 스레드에선 문제가 없지만 멀티 스레드일 경우 문제가 발생할 여지가 있음
									boolean allReplacable = true;
									final MethodSemantics msem = semantics.getMethodSemantics(codeitem);
									for (InstSemOperand use: uses) {
										if (! (use instanceof InstSemRegister)) {
											assert false;
											allReplacable = false;
											break;
										}

										// TODO rhs가 wide 레지스터인의 뒤쪽 레지스터를 가리킬 경우 치환을 포기한다
										
										if (rhs.getStatement() != use.getStatement()) {
											final DexInstruction starter = rhs.getStatement().getInstruction();
											final DexInstruction end = use.getStatement().getInstruction();
											CodeSet cs = cfg.getReachables(starter, end);
											MutableVariable<Boolean> unchanged = new MutableVariable<Boolean>() {
												private boolean value = true;
	
												@Override
												public Boolean get() {
													return value;
												}
	
												@Override
												public void set(Boolean t) {
													this.value = t;
												}
											};
											cs.visit(new CodeSetVisitor<MutableVariable<Boolean>>() {
	
												@Override
												public boolean visit(DexInstruction instruction, MutableVariable<Boolean> progress) {
													if (instruction != starter && instruction != end) {
														InstSemStatement stmt = msem.findStatmentByInstruction(instruction);
														
														if (stmt != null && stmt instanceof InstSemAssignment) {
															InstSemAssignment assign = (InstSemAssignment) stmt;
															
															if (assign.getLefthandside().mayReferSamePlace((InstSemLHS) rhs)) {
																progress.set(false);
																return false;
															}
														}
													}
													return true;
												}
											}, unchanged);
											
											if (unchanged.get()) {
												InstSemOperand gens[] = getGensOf((InstSemRegister) use);
												assert gens.length >= 1 && ArraysUtil.indexOfExact(gens, lreg) >= 0;
												if (gens.length == 1) {
													replacements.put((InstSemRegister) use, rhs);
												} else {
													allReplacable = false;
												}
											} else {
												allReplacable = false;
											}
										}
									}
									if (allReplacable) {
										deads.addDeadInstruction(sem[i]);
										upd = true;
									}
								}
							}
						}
					}
				}
			}
			// nested replacement elimination
			Map<InstSemRegister, InstSemOperand> updates = new HashMap<InstSemRegister, InstSemOperand>();
			do {
				updates.clear();
				for (Entry<InstSemRegister, InstSemOperand> entry: replacements.entrySet()) {
					if (replacements.containsKey(entry.getValue())) {
						updates.put(entry.getKey(), replacements.get(entry.getValue()));
					}
				}
				replacements.putAll(updates);
			} while (! updates.isEmpty());
			
			// finalize
			instructionToInstSem = null;
		}
		
		private boolean updated;
		
		private void addEdge(InstSemOperand src, InstSemOperand dst) {
			updated |= edges.add(new InstSemElementEdge(src, dst));
		}
		
		private void copyDefSetExcept(InstSemStatement statement, InstSemRegister except) {
			DexRegister exceptRegister = (except == null)? null:except.getRegister();
			DexRegister except2 = ((except != null) && (except instanceof InstSemRegisterPair))? ((InstSemRegisterPair) except).getNextRegister() : null;
			for (Entry<DexRegister, Set<InstSemOperand>> entry: getBeforeStatementDefSetMap(statement).entrySet()) {
				if (entry.getKey() != exceptRegister && entry.getKey() != except2) {
					Set<InstSemOperand> set = getAfterStatementRegisterDefSet(statement, entry.getKey());
					for (InstSemOperand gen: entry.getValue()) {
						updated |= set.add(gen);
					}
				}
			}
			if (exceptRegister != null) {
				Set<InstSemOperand> set = getAfterStatementRegisterDefSet(statement, exceptRegister);
				assert set.isEmpty() || (set.size() == 1 && set.contains(except));
				updated |= set.add(except);
				if (except2 != null) {
					set = getAfterStatementRegisterDefSet(statement, except2);
					assert set.isEmpty() || (set.size() == 1 && set.contains(except));
					updated |= set.add(except);
				}
			}
		}
		
		private void copyDefSet(InstSemStatement statement) {
			copyDefSetExcept(statement, null);
		}
		
		private void addUse(InstSemStatement statement, InstSemRegister use) {
			DexRegister register = use.getRegister();
			Set<InstSemOperand> gens = getBeforeStatementRegisterDefSet(statement, register);
			
			for (InstSemOperand gen: gens) {
				if (! edges.contains(new InstSemElementEdge(gen, use))) {
					// System.out.println(use.getStringRepresentation() + " uses " + gen.getStringRepresentation());
				}
				addEdge(gen, use);
			}
		}
		
		private void processElement(InstSemStatement statement, InstSemOperand elem) {
			switch (elem.getInstSemType()) {
			case REGISTER: case REGISTER_PAIR:
				addUse(statement, (InstSemRegister) elem);
				break;
			case ARRAY_REFERENCE:
				addUse(statement, ((InstSemArrayReference) elem).getArray());
				addUse(statement, ((InstSemArrayReference) elem).getIndex());
				break;
			case BINARY_OP: {
				InstSemElement op1 = ((InstSemBinaryOp) elem).getFirstOperand();
				InstSemElement op2 = ((InstSemBinaryOp) elem).getSecondOperand();
				assert op1 instanceof InstSemRegister || op1 instanceof InstSemConst;
				assert op2 instanceof InstSemRegister || op2 instanceof InstSemConst;
				if (op1 instanceof InstSemRegister) {
					addUse(statement, (InstSemRegister) op1);
				}
				if (op2 instanceof InstSemRegister) {
					addUse(statement, (InstSemRegister) op2);
				}
				addEdge((InstSemElement) op1, (InstSemBinaryOp) elem);
				addEdge((InstSemElement) op2, (InstSemBinaryOp) elem);
				break;
			}
			case CONST:
				// nothing to do
				break;
			case INSTANCE_FIELD:
				addUse(statement, ((InstSemInstanceField) elem).getInstance());
				break;
			case INSTANCE_METHOD_INVOKE: {
				addUse(statement, ((InstSemInstanceMethodInvoke) elem).getInstance());
				InstSemRegister args[] = ((InstSemInstanceMethodInvoke) elem).getArguments();
				for (int i = 0; i < args.length; i++) {
					addUse(statement, args[i]);
				}
				break;
			}
			case NEW_ARRAY: {
				InstSemRegister size = ((InstSemNewArray) elem).getSize();
				if (size == null) {
					for (InstSemRegister item: ((InstSemNewArray) elem).getItems()) {
						addUse(statement, item);
					}
				} else {
					addUse(statement, size);
				}
				break;
			}
			case STATIC_FIELD:
				// nothing to do
				break;
			case STATIC_METHOD_INVOKE:
				for (InstSemRegister arg: ((InstSemStaticMethodInvoke) elem).getArguments()) {
					addUse(statement, arg);
				}
				break;
			case UNARY_OP:
				addUse(statement, ((InstSemUnaryOp) elem).getOperand());
				addEdge(((InstSemUnaryOp) elem).getOperand(), (InstSemUnaryOp) elem);
				break;
			case NEW_INSTANCE:
				// nothing to do
			case ETC_OPERAND:
				// nothing to do
				break;
			default:
				assert false;
			}
		}
		
		private void process(InstSemStatement statement) {
			switch (statement.getInstSemType()) {
			case ASSIGNMENT_STATEMENT: {
				InstSemAssignment assign = (InstSemAssignment) statement;
				
				InstSemLHS lhs = assign.getLefthandside();
				InstSemOperand rhs = assign.getRighthandside();
				
				if (lhs instanceof InstSemRegister) {
					processElement(statement, rhs);
					addEdge(rhs, (InstSemRegister) lhs);
					copyDefSetExcept(statement, (InstSemRegister) lhs);
					return;
				} else if (lhs instanceof InstSemArrayReference) {
					addUse(statement, ((InstSemArrayReference) lhs).getArray());
					addUse(statement, ((InstSemArrayReference) lhs).getIndex());
					processElement(statement, rhs);
				} else if (lhs instanceof InstSemInstanceField) {
					addUse(statement, ((InstSemInstanceField) lhs).getInstance());
					processElement(statement, rhs);
				} else if (lhs instanceof InstSemStaticField) {
					processElement(statement, rhs);
				}
				break;
			}
			case ETC_STATEMENT: {
				for (InstSemElement elem: ((InstSemEtcStatement) statement).getUseOperands()) {
					if (elem instanceof InstSemRegister) {
						addUse(statement, (InstSemRegister) elem);
					}
				}
				break;
			}
			case INSTANCE_METHOD_INVOKE:
				processElement(statement, (InstSemInstanceMethodInvoke) statement);
				break;
			case STATIC_METHOD_INVOKE:
				processElement(statement, (InstSemStaticMethodInvoke) statement);
				break;
			default:
				assert false;
			}
			copyDefSet(statement);
		}
	}
}
