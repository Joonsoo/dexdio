package com.giyeok.dexdio.augmentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.Handler;
import com.giyeok.dexdio.model.DexCodeItem.Handler.HandlerItem;
import com.giyeok.dexdio.model.DexCodeItem.Try;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstMonitor;
import com.giyeok.dexdio.model.insns.DexInstMoveResult;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.util.GraphUtil;

/**
 * Control flow graph 정보를 추출한다
 * 
 * @author Joonsoo
 *
 */
public class ControlFlowAnalyzer extends Augmentation {

	public static ControlFlowAnalyzer get(DexProgram program) {
		return (ControlFlowAnalyzer) Augmentation.getAugmentation(ControlFlowAnalyzer.class, program);
	}
	
	@Override
	protected String getAugmentationName() {
		return "Control flow analyzer";
	}
	
	public ControlFlowAnalyzer(DexProgram program) {
		super(program);
		analyses = new HashMap<DexMethod, ControlFlowAnalyzer.ControlFlowAnalysis>();
	}
	
	public void visit() {
		visitMethods(getProgram(), new MethodVisitor() {
			
			@Override
			public void visit(DexProgram program, DexMethod method) {
				DexCodeItem codeitem = method.getCodeItem();
				
				if (codeitem != null) {
					getControlFlowForMethod(codeitem);
				}
			}
		});
	}
	
	private Map<DexMethod, ControlFlowAnalysis> analyses;
	
	public ControlFlowAnalysis getControlFlowForMethod(DexCodeItem codeitem) {
		DexMethod method = codeitem.getBelongedMethod();
		ControlFlowAnalysis cache = analyses.get(method);
		if (cache != null) {
			return cache;
		}
		cache = new ControlFlowAnalysis(codeitem);
		analyses.put(method, cache);
		return cache;
	}
	
	public static class ControlFlowAnalysis {
		public static class BasicBlock {
			private DexInstruction[] instructions;
			private String name;
			private Try tri;
			
			public BasicBlock(String name, DexInstruction[] instructions) {
				this.name = name;
				this.instructions = instructions;
				this.tri = null;
			}
			
			public boolean isFirstInstruction(DexInstruction instruction) {
				return instructions[0] == instruction;
			}
			
			public boolean isLastInstruction(DexInstruction instruction) {
				return instructions[instructions.length - 1] == instruction;
			}
			
			public DexInstruction getFirstInstruction() {
				return instructions[0];
			}
			
			public DexInstruction getLastInstruction() {
				return instructions[instructions.length - 1];
			}
			
			public DexInstruction[] getInstructionsArray() {
				return instructions;
			}
			
			public String getName() {
				return name;
			}
			
			public Try getTry() {
				return tri;
			}
			
			@Override
			public String toString() {
				return getName();
			}
		}
		
		public static class Edge {
			BasicBlock source;
			BasicBlock destination;
			boolean isHandlerEdge;
			Try tri;
			HandlerItem handler;
			DexType exception;
			
			public Edge(BasicBlock source, BasicBlock destination) {
				this.source = source;
				this.destination = destination;
				this.isHandlerEdge = false;
				this.handler = null;
				this.exception = null;
			}
			
			public Edge(BasicBlock source, BasicBlock destination, Try tri, HandlerItem handler) {
				this.source = source;
				this.destination = destination;
				this.isHandlerEdge = true;
				this.tri = tri;
				this.handler = handler;
				if (handler != null) {
					this.exception = handler.getExceptionType();
				}
			}
			
			@Override
			public boolean equals(Object other) {
				if (other instanceof Edge) {
					Edge o = (Edge) other;
					return o.source == source && o.destination == destination && o.isHandlerEdge == isHandlerEdge && o.exception == exception;
				} else {
					return true;
				}
			}
			
			public BasicBlock getSource() {
				return source;
			}
			
			public BasicBlock getDestination() {
				return destination;
			}
			
			public boolean isHandlerEdge() {
				return isHandlerEdge;
			}
			
			public DexType getExceptionType() {
				return exception;
			}
		}
		
		public BasicBlock[] getBlocks() {
			return blocks.toArray(new BasicBlock[0]);
		}
		
		public Edge[] getEdges() {
			return edges.toArray(new Edge[0]);
		}
		
		public static interface CodeSetVisitor<T> {
			/**
			 * instruction을 방문한다.
			 * true를 반환하면 방문을 계속하고, false를 반환하면 방문을 중단한다
			 * @param instruction
			 * @param progress
			 * @return
			 */
			public boolean visit(DexInstruction instruction, T progress);
		}
		
		public static interface MutableVariable<T> {
			public T get();
			public void set(T t);
		}
		
		public class CodeSet {
			private Set<BasicBlock> blocks;
			private Set<DexInstruction> instructions;
			
			public CodeSet() {
				this.blocks = new HashSet<BasicBlock>();
				this.instructions = new HashSet<DexInstruction>();
			}
			
			public boolean contains(DexInstruction instruction) {
				BasicBlock bb = findBasicBlockOfInstruction(instruction);
				
				return blocks.contains(bb) || instructions.contains(instruction);
			}
			
			public <T> void visit(CodeSetVisitor<T> visitor, T initial) {
				T progress = initial;
				for (BasicBlock block: blocks) {
					for (DexInstruction instruction: block.getInstructionsArray()) {
						if (! visitor.visit(instruction, progress)) {
							return;
						}
					}
				}
				for (DexInstruction instruction: instructions) {
					if (! visitor.visit(instruction, progress)) {
						return;
					}
				}
			}
			
			private void addBlocks(Set<BasicBlock> blocks) {
				for (BasicBlock node: blocks) {
					this.blocks.add(node);
				}
			}
			
			private void addInstruction(DexInstruction instruction) {
				instructions.add(instruction);
			}
		}
		
		public CodeSet getReachables(DexInstruction start, DexInstruction end) {
			// TODO may need efficiency improvement
			BasicBlock startB = findBasicBlockOfInstruction(start);
			BasicBlock endB = findBasicBlockOfInstruction(end);
			CodeSet result = new CodeSet();
			
			if (startB == endB) {
				int firstIndex = codeitem.getIndexOfInstruction(start);
				int lastIndex = codeitem.getIndexOfInstruction(end);
				
				for (int i = firstIndex; i <= lastIndex; i++) {
					result.addInstruction(codeitem.getInstruction(i));
				}
			} else {
				Set<BasicBlock> reachableBlocks = GraphUtil.getReachables(getEdges(), startB, endB);
				reachableBlocks.remove(startB);
				reachableBlocks.remove(endB);
				
				result.addBlocks(reachableBlocks);

				for (int i = codeitem.getIndexOfInstruction(start); ; i++) {
					DexInstruction inst = codeitem.getInstruction(i);
					
					result.addInstruction(inst);
					if (startB.isLastInstruction(inst)) {
						break;
					}
				}
				for (int i = codeitem.getIndexOfInstruction(end); ; i++) {
					DexInstruction inst = codeitem.getInstruction(i);
					
					result.addInstruction(inst);
					if (endB.isLastInstruction(inst)) {
						break;
					}
				}
			}
			return result;
		}
		
		private DexCodeItem codeitem;
		
		private Map<DexInstruction, BasicBlock> instructionToBlock;
		private ArrayList<BasicBlock> blocks;
		private Set<Edge> edges;
		
		public ControlFlowAnalysis(DexCodeItem codeitem) {
			this.codeitem = codeitem;
			
			final boolean reviseExceptionBlock = true;			// true이면 예외처리로 인해 발생하는 블럭을 앞뒤로 확장하여 블록 수를 줄일 수 있는 경우 그렇게 한다
			DexInstruction instructions[] = codeitem.getInstructionsArray();
			boolean blockStarter[] = new boolean[instructions.length];
			
			Arrays.fill(blockStarter, false);
			blockStarter[0] = true;
			
			// 1. 제어 흐름에 의해 블록이 시작될 위치 설정
			for (int i = 0; i < instructions.length; i++) {
				int gothroughs[] = instructions[i].getPossibleGoThroughs();
				
				if (gothroughs.length == 0) {
					if (i < (instructions.length - 1)) {
						blockStarter[i + 1] = true;
					}
				} else if ((i == instructions.length - 1) || (gothroughs.length != 1 || gothroughs[0] != instructions[i + 1].getAddress())) {
					for (int j: gothroughs) {
						blockStarter[codeitem.getIndexOfInstructionByAddress(j)] = true;
					}
				}
			}
			
			// 2. 핸들러에 의해 블럭이 나뉘는 곳 설정
			Handler[] handlers = codeitem.getHandlers();
			if (handlers != null) {
				for (Handler handler: handlers) {
					for (int addr: handler.getHandlerStartPoints()) {
						blockStarter[codeitem.getIndexOfInstructionByAddress(addr)] = true;
					}
				}
			}
			
			// 3. catch 블럭 설정
			Try[] tries = codeitem.getTries();
			if (tries != null) {
				for (Try tri: tries) {
					int startindex = tri.getStartIndex();
					if (reviseExceptionBlock) {
						int j = startindex - 1;
						while (j >= 0 && (! instructions[j].canThrowException())) {
							if (blockStarter[j]) {
								startindex = j;
								break;
							}
							j--;
						}
					}
					blockStarter[startindex] = true;
					
					if (tri.getFinIndex() >= 0) {
						int finindex = tri.getFinIndex();
						if (finindex < blockStarter.length) {
							if (instructions[finindex] instanceof DexInstMoveResult) {
								finindex++;
							}
							if (reviseExceptionBlock) {
								int k = finindex;
								while (! instructions[k].canThrowException()) {
									k++;
									if (k >= instructions.length || blockStarter[k]) {
										finindex = k;
										break;
									}
								}
							}
							if (finindex < blockStarter.length) {
								blockStarter[finindex] = true;
							}
						}
					}
				}
			}
			
			// 4. monitor-enter와 exit이 하나의 블록 전체를 차지하도록 블록 추가
			for (int i = 1; i < instructions.length - 1; i++) {
				if (instructions[i] instanceof DexInstMonitor) {
					blockStarter[i] = true;
					blockStarter[i + 1] = true;
				}
			}
			
			// 5. 베이직 블럭 설정
			ArrayList<DexInstruction> basicBlock = new ArrayList<DexInstruction>();
			int blockCounter = 0;
			
			instructionToBlock = new HashMap<DexInstruction, ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock>();
			blocks = new ArrayList<ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock>();
			basicBlock.add(instructions[0]);
			for (int i = 1; i < instructions.length; i++) {
				if (blockStarter[i]) {
					addBasicBlock("" + (++blockCounter), basicBlock.toArray(new DexInstruction[0]));
					basicBlock.clear();
				}
				basicBlock.add(instructions[i]);
			}
			addBasicBlock("" + (++blockCounter), basicBlock.toArray(new DexInstruction[0]));
			basicBlock = null;
			
			// 6. 제어 흐름에 의한 엣지 설정
			edges = new HashSet<ControlFlowAnalyzer.ControlFlowAnalysis.Edge>();
			for (int i = 0; i < instructions.length; i++) {
				int gothroughs[] = instructions[i].getPossibleGoThroughs();
				BasicBlock me = findBasicBlockOfInstruction(instructions[i]);
				
				for (int j: gothroughs) {
					BasicBlock other = findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(j));
					
					if (me != other) {
						addEdge(me, other);
					}
				}
			}
			
			// 7. 핸들러 블록 설정
			if (tries != null) {
				for (Try tri: tries) {
					HandlerItem handleritems[] = tri.getHandler().getHandlerItems();
					int catchAll = tri.getHandler().getCatchAllAddr();
					
					int endAddress = tri.getStartAddress() + tri.getInstructionsLength();
					
					int j = codeitem.getIndexOfInstructionByAddress(tri.getStartAddress());
					BasicBlock lastThrower = null;
					while (j < instructions.length && instructions[j].getAddress() < endAddress) {
						BasicBlock thrower = findBasicBlockOfInstruction(instructions[j]);
						if (lastThrower != thrower) {
							assert thrower.tri == null;
							thrower.tri = tri;
							lastThrower = thrower;
							if (canThisBasicBlockThrowAnException(codeitem, thrower)) {
								if (handlers != null) {
									for (HandlerItem handleritem: handleritems) {
										addHandlerEdge(thrower, findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(handleritem.getHandlerAddress())), tri, handleritem);
									}
								}
								if (catchAll >= 0) {
									addHandlerEdge(thrower, findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(catchAll)), tri, null);
								}
							}
						}
						j++;
					}
				}
			}
			
			// 개발 코드: 속성 검증
			for (int i = 0; i < instructions.length; i++) {
				assert (! blockStarter[i]) || (blockStarter[i] && (! (instructions[i] instanceof DexInstMoveResult)));
			}
		}
		
		private boolean canThisBasicBlockThrowAnException(DexCodeItem codeitem, BasicBlock thrower) {
			int startIndex = codeitem.getIndexOfInstruction(thrower.getFirstInstruction());
			int lastIndex = codeitem.getIndexOfInstruction(thrower.getLastInstruction());
			
			for (int i = startIndex; i <= lastIndex; i++) {
				if (codeitem.getInstruction(i).canThrowException()) {
					return true;
				}
			}
			return false;
		}
		
		private void addBasicBlock(String name, DexInstruction basicBlock[]) {
			BasicBlock block = new BasicBlock(name, basicBlock);
			
			blocks.add(block);
			for (DexInstruction instruction: basicBlock) {
				instructionToBlock.put(instruction, block);
			}
		}
		
		private void addEdge(BasicBlock start, BasicBlock end) {
			edges.add(new Edge(start, end));
		}
		
		private void addHandlerEdge(BasicBlock start, BasicBlock end, Try tri, HandlerItem handleritem) {
			edges.add(new Edge(start, end, tri, handleritem));
		}
		
		public BasicBlock findBasicBlockOfInstruction(DexInstruction instruction) {
			return instructionToBlock.get(instruction);
		}
	}
}
