package com.giyeok.dexdio.augmentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.Edge;
import com.giyeok.dexdio.augmentation.ControlFlowStructuralizer.ControlStatement.ControlStatementType;
import com.giyeok.dexdio.augmentation.Logger.Log;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.Handler;
import com.giyeok.dexdio.model.DexCodeItem.Handler.HandlerItem;
import com.giyeok.dexdio.model.DexCodeItem.Try;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstIf;
import com.giyeok.dexdio.model.insns.DexInstIfZero;
import com.giyeok.dexdio.model.insns.DexInstMonitor;
import com.giyeok.dexdio.model.insns.DexInstSwitch;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.model.insns.DexInstruction.DexInstructionType;
import com.giyeok.dexdio.util.GraphUtil;
import com.giyeok.dexdio.util.Pair;

public class ControlFlowStructuralizer extends Augmentation {

	public static ControlFlowStructuralizer get(DexProgram program) {
		return (ControlFlowStructuralizer) Augmentation.getAugmentation(ControlFlowStructuralizer.class, program);
	}
	
	@Override
	protected String getAugmentationName() {
		return "Control flow structuralizer";
	}
	
	public static abstract class ControlFlowStructure {
		private ControlStatement lastStatement;
		
		public ControlStatement getLastStatement() {
			return lastStatement;
		}
		
		public abstract StructureType getStructureType();
		
		public static enum StructureType {
			FLAT,
			BLOCK,
			IF,
			LOOP,
			WHILE,
			DOWHILE,
			SWITCH,
			TRYCATCH,
			SYNCHRONIZED,
			CONTROLSTATEMENT
		}
	}
	
	public static class FlatStructure extends ControlFlowStructure {
		private BasicBlock block;
		private ControlFlowStructure next;
		
		public FlatStructure(BasicBlock block) {
			this.block = block;
			this.next = null;
		}
		
		public BasicBlock getBlock() {
			return block;
		}
		
		public ControlFlowStructure getNext() {
			return next;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.FLAT;
		}
	}
	
	public static class BlockStructure extends ControlFlowStructure {
		private ControlFlowStructure body;
		private ControlFlowStructure next;
		
		public ControlFlowStructure getBody() {
			return body;
		}
		
		public ControlFlowStructure getNext() {
			return next;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.BLOCK;
		}
	}
	
	public static class IfStructure extends ControlFlowStructure {
		private BasicBlock condition;
		private boolean negated;
		private ControlFlowStructure thenpart;
		private ControlFlowStructure elsepart;
		private ControlFlowStructure next;
		
		public IfStructure(BasicBlock condition, boolean negated) {
			// may need label
			this.condition = condition;
			this.negated = negated;
		}
		
		public BasicBlock getConditionBlock() {
			return condition;
		}
		
		public boolean isNegated() {
			return negated;
		}
		
		public ControlFlowStructure getThenPart() {
			return thenpart;
		}
		
		public ControlFlowStructure getElsePart() {
			return elsepart;
		}
		
		public ControlFlowStructure getNext() {
			return next;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.IF;
		}
	}
	
	public static class LoopStructure extends ControlFlowStructure {
		private ControlFlowStructure body;
		
		public LoopStructure(ControlFlowStructure body) {
			this.body = body;
		}
		
		public ControlFlowStructure getBody() {
			return body;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.LOOP;
		}
	}
	
	public static class WhileStructure extends ControlFlowStructure {
		private BasicBlock condition;
		private boolean negated;
		private ControlFlowStructure body;
		private ControlFlowStructure next;
		
		public WhileStructure(BasicBlock condition, boolean negated) {
			// may need label
			this.condition = condition;
			this.negated = negated;
		}
		
		public BasicBlock getConditionBlock() {
			return condition;
		}
		
		public ControlFlowStructure getBody() {
			return body;
		}
		
		public ControlFlowStructure getNext() {
			return next;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.WHILE;
		}
	}
	
	public static class DoWhileStructure extends ControlFlowStructure {
		public DoWhileStructure(ControlFlowStructure body, BasicBlock condition, ControlFlowStructure next) {
			// may need label
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.DOWHILE;
		}
	}
	
	public static class SwitchStructure extends ControlFlowStructure {
		private BasicBlock block;
		private ArrayList<Pair<Integer, Pair<ControlFlowStructure, Boolean>>> cases;
		private ControlFlowStructure fallthrough;
		private ControlFlowStructure next;
		
		public SwitchStructure(BasicBlock block) {
			assert block.getLastInstruction() instanceof DexInstSwitch;
			
			this.block = block;
			this.cases = new ArrayList<Pair<Integer, Pair<ControlFlowStructure, Boolean>>>();
			// cases: 값 -> (핸들러(maybe null) -> break(true이면 break, false이면 fallthrough))
			this.next = null;
		}
		
		public BasicBlock getBranchBlock() {
			return block;
		}
		
		public ArrayList<Pair<Integer, Pair<ControlFlowStructure, Boolean>>> getCases() {
			return cases;
		}
		
		public ControlFlowStructure getDefault() {
			return fallthrough;
		}
		
		public ControlFlowStructure getNext() {
			return next;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.SWITCH;
		}
	}
	
	public static class TryCatchStructure extends ControlFlowStructure {
		private ControlFlowStructure trypart;
		private ArrayList<Pair<DexType, ControlFlowStructure>> handlers;
		private ControlFlowStructure finalli;
		private ControlFlowStructure next;
		
		public TryCatchStructure() {
			this.trypart = null;
			this.handlers = new ArrayList<Pair<DexType,ControlFlowStructure>>();
			this.finalli = null;
			this.next = null;
		}
		
		public ControlFlowStructure getTryBody() {
			return trypart;
		}
		
		public ArrayList<Pair<DexType, ControlFlowStructure>> getHandlers() {
			return handlers;
		}
		
		public ControlFlowStructure getFinally() {
			return finalli;
		}
		
		public ControlFlowStructure getNext() {
			return next;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.TRYCATCH;
		}
	}
	
	public static class SynchronizedStructure extends ControlFlowStructure {
		private DexInstMonitor monitorenter;
		private ControlFlowStructure body;
		private ControlFlowStructure next;
		
		public SynchronizedStructure(DexInstMonitor monitorenter, ControlFlowStructure body, ControlFlowStructure next) {
			this.monitorenter = monitorenter;
			this.body = body;
			this.next = next;
		}
		
		public DexInstMonitor getMonitorEnterInstruction() {
			return monitorenter;
		}
		
		public ControlFlowStructure getBody() {
			return body;
		}
		
		public ControlFlowStructure getNext() {
			return next;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.SYNCHRONIZED;
		}
	}
	
	public static class ControlStatement extends ControlFlowStructure {
		public enum ControlStatementType {
			BREAK,
			CONTINUE,
			GOTO
		}
		
		private ControlStatementType type;
		private ControlFlowStructure struct;
		
		public ControlStatement(ControlStatementType type, ControlFlowStructure struct) {
			this.type = type;
			this.struct = struct;
		}
		
		public ControlStatementType getStatementType() {
			return type;
		}
		
		public ControlFlowStructure getTarget() {
			return struct;
		}

		@Override
		public StructureType getStructureType() {
			return StructureType.CONTROLSTATEMENT;
		}
	}

	private ControlFlowAnalyzer cf;
	
	private Map<DexCodeItem, ControlFlowStructure> structures;
	
	public ControlFlowStructure getStructure(DexCodeItem codeitem) {
		return structures.get(codeitem);
	}

	public ControlFlowStructuralizer(DexProgram program) {
		super(program);

		cf = ControlFlowAnalyzer.get(program);

		structures = new HashMap<DexCodeItem, ControlFlowStructure>();
		visit(program);
	}
	
	private void visit(DexProgram program) {
		visitMethods(program, new MethodVisitor() {
			
			@Override
			public void visit(DexProgram program, DexMethod method) {
				DexCodeItem codeitem = method.getCodeItem();
				
				if (codeitem != null) {
					structures.put(codeitem, structuralize(codeitem));
				}
			}
		});
	}
	
	private DexCodeItem codeitem;
	private ControlFlowAnalysis cfg;
	private Edge edges[];
	private Edge normaledges[];
	
	private Map<BasicBlock, ControlFlowStructure> processed;
	private Map<BasicBlock, ControlFlowStructure> predecessor;
	
	private ControlFlowStructure structuralize(DexCodeItem codeitem) {
		ControlFlowStructure struct;
		
		this.codeitem = codeitem;
		cfg = cf.getControlFlowForMethod(codeitem);
		
		BasicBlock blocks[] = cfg.getBlocks();
		edges = cfg.getEdges();
		normaledges = getNormalsOnly(edges);
		
		processed = new HashMap<BasicBlock, ControlFlowStructure>();
		predecessor = new HashMap<BasicBlock, ControlFlowStructure>();
		
		try {
			struct = structuralize(new HashSet<BasicBlock>(Arrays.asList(blocks)), blocks[0], new HashMap<BasicBlock, ControlStatement>(), false);
			assert struct != null;
			Logger.addMessage(this, Log.line(new Log[] { Log.log("Supported structure on "), Log.log(codeitem.getBelongedMethod()) }));
		} catch(UnsupportedStructureException e) {
			Logger.addMessage(this, Log.line(new Log[] { Log.log("Unsupported structure on "), Log.log(codeitem.getBelongedMethod()), Log.log(" : " + e.getMessage()) }));
			struct = null;
		}
		
		return struct;
	}
	
	private Edge[] getNormalsOnly(Edge edges[]) {
		int j = 0;
		for (int i = 0; i < edges.length; i++) {
			if (! edges[i].isHandlerEdge) {
				j++;
			}
		}
		Edge[] result = new Edge[j];
		j = 0;
		for (int i = 0; i < edges.length; i++) {
			if (! edges[i].isHandlerEdge) {
				result[j++] = edges[i];
			}
		}
		return result;
	}
	
	private ControlFlowStructure structuralize(Set<BasicBlock> left, BasicBlock pointer, Map<BasicBlock, ControlStatement> controls, boolean caringTry) throws UnsupportedStructureException {
		DexInstruction firstInstruction = pointer.getFirstInstruction();
		DexInstruction lastInstruction = pointer.getLastInstruction();
		Edge inedges[] = GraphUtil.getInEdgesOf(edges, pointer);
		Edge outedges[] = GraphUtil.getOutEdgesOf(edges, pointer);
		Edge normaloutedges[] = getNormalsOnly(outedges);
		
		BasicBlock leftNodes[] = left.toArray(new BasicBlock[0]);
		if ((! caringTry) && (pointer.getTry() != null)) {
			// try catch structure
			Handler handler = pointer.getTry().getHandler();
			
			Set<BasicBlock> trybody = new HashSet<BasicBlock>();
			for (Try tri: handler.getTries()) {
				int finIndex = tri.getFinIndex();
				if (finIndex < 0) {
					finIndex = codeitem.getInstructionsSize();
				}
				for (int i = tri.getStartIndex(); i < finIndex; i++) {
					trybody.add(cfg.findBasicBlockOfInstruction(codeitem.getInstruction(i)));
				}
			}
			
			Set<BasicBlock> starters = new HashSet<BasicBlock>();
			for (HandlerItem hi: handler.getHandlerItems()) {
				starters.add(cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(hi.getHandlerAddress())));
			}
			if (handler.getCatchAllAddr() >= 0) {
				starters.add(cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(handler.getCatchAllAddr())));
			}
			
			starters.add(pointer);
			BasicBlock confluence = GraphUtil.getConfluence(leftNodes, normaledges, starters.toArray(new BasicBlock[0]));
			
			Set<BasicBlock> tryfollower = GraphUtil.getReachables(leftNodes, normaledges, pointer, confluence);
			
			if (! trybody.containsAll(tryfollower)) {
				throw new UnsupportedStructureException("Smaller trybody");
			}
			
			if (! tryfollower.containsAll(trybody)) {
				// L1: { try { break L1; } catch {} finally {} somethingelse } following
				// TODO
				throw new UnsupportedStructureException("tryfollower != trybody");
			} else {
				TryCatchStructure trystruct;
				trystruct = new TryCatchStructure();

				controls.put(confluence, new ControlStatement(ControlStatementType.BREAK, trystruct));
				trystruct.trypart = structuralize(trybody, pointer, controls, true);
				
				HandlerItem item[] = handler.getHandlerItems();
				for (int i = 0; i < item.length; i++) {
					ControlFlowStructure hstruct;
					BasicBlock hblock = cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(item[i].getHandlerAddress()));
					Set<BasicBlock> hblocks = GraphUtil.getReachables(leftNodes, normaledges, hblock, confluence);
					
					hstruct = structuralize(hblocks, hblock, controls, false);
					trystruct.handlers.add(new Pair<DexType, ControlFlowStructure>(item[i].getExceptionType(), hstruct));
				}
				controls.remove(confluence);
				if (confluence != null) {
					trystruct.next = structuralize(GraphUtil.getReachables(leftNodes, edges, confluence, null), confluence, controls, false);
				}
				return trystruct;
			}
		}
		
		if (controls.containsKey(pointer)) {
			return controls.get(pointer);
		} else if (processed.containsKey(pointer)) {
			return new ControlStatement(ControlStatementType.GOTO, processed.get(pointer));
		}
		
		switch (lastInstruction.getInstructionType()) {
		case MONITOR: {
			assert normaloutedges.length == 1;
			// TODO
			left.remove(pointer);
			FlatStructure struct;
			struct = new FlatStructure(pointer);
			processed.put(pointer, struct);
			BasicBlock nextblock = (BasicBlock) outedges[0].getDestination();
			predecessor.put(nextblock, struct);
			struct.next = structuralize(left, nextblock, controls, caringTry);
			return struct;
		}
		case IF: case IF_ZERO: {
			// if then, if then else, loop - while(true), while, do while
			assert normaloutedges.length == 2;
			
			BasicBlock thenblock, elseblock;
			if (lastInstruction.getInstructionType() == DexInstructionType.IF) {
				DexInstIf ifs = (DexInstIf) lastInstruction;
				thenblock = cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(ifs.getBranchTargetAddress()));
				elseblock = cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(ifs.getFallthroughAddress()));
			} else {
				DexInstIfZero ifs = (DexInstIfZero) lastInstruction;
				thenblock = cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(ifs.getBranchTargetAddress()));
				elseblock = cfg.findBasicBlockOfInstruction(codeitem.getInstructionAtAddress(ifs.getFallthroughAddress()));
			}

			boolean thenReachable = GraphUtil.isReachable(leftNodes, normaledges, thenblock, pointer);
			boolean elseReachable = GraphUtil.isReachable(leftNodes, normaledges, elseblock, pointer);

			left.remove(pointer);
			
			if ((! thenReachable) && (! elseReachable)) {
				// if then, if then else
				BasicBlock confluence = (BasicBlock) GraphUtil.getConfluence(leftNodes, normaledges, new BasicBlock[] { thenblock, elseblock });
				if (confluence != thenblock && confluence != elseblock) {
					// if then else
					IfStructure ifstruct = new IfStructure(pointer, false);
					processed.put(pointer, ifstruct);
					controls.put(confluence, new ControlStatement(ControlStatementType.BREAK, ifstruct));
					Set<BasicBlock> thenblocks = GraphUtil.getReachables(leftNodes, normaledges, thenblock, confluence);
					Set<BasicBlock> elseblocks = GraphUtil.getReachables(leftNodes, normaledges, elseblock, confluence);
					ifstruct.thenpart = structuralize(thenblocks, thenblock, controls, caringTry);
					ifstruct.elsepart = structuralize(elseblocks, elseblock, controls, caringTry);
					controls.remove(confluence);
					if (confluence == null) {
						ifstruct.next = null;
					} else {
						predecessor.put(confluence, ifstruct);
						ifstruct.next = structuralize(GraphUtil.getReachables(leftNodes, normaledges, confluence, null), confluence, controls, caringTry);
					}
					return ifstruct;
				} else {
					// if then
					boolean negated;
					if (confluence == elseblock) {
						negated = false;
					} else {
						negated = true;
						BasicBlock tempblock;
						tempblock = thenblock;
						thenblock = elseblock;
						elseblock = tempblock;
					}
					IfStructure ifstruct = new IfStructure(pointer, negated);
					processed.put(pointer, ifstruct);
					controls.put(confluence, new ControlStatement(ControlStatementType.BREAK, ifstruct));
					ifstruct.thenpart = structuralize(GraphUtil.getReachables(leftNodes, normaledges, thenblock, confluence), thenblock, controls, caringTry);
					ifstruct.elsepart = null;
					controls.remove(confluence);
					if (confluence == null) {
						ifstruct.next = null;
					} else {
						predecessor.put(confluence, ifstruct);
						ifstruct.next = structuralize(GraphUtil.getReachables(leftNodes, normaledges, thenblock, null), confluence, controls, caringTry);
					}
					return ifstruct;
				}
			} else {
				// loop, while, do while
				BasicBlock bodyblock, nextblock;
				if (thenReachable && elseReachable) {
					bodyblock = thenblock;
					// while (true) { if () {} else {} }
					// TODO support later
					throw new UnsupportedStructureException("Double reachable branch");
				} else if (thenReachable) {
					bodyblock = thenblock;
					nextblock = elseblock;
				} else {
					assert (! thenReachable) && elseReachable;
					bodyblock = elseblock;
					nextblock = thenblock;
				}
				WhileStructure whstruct = new WhileStructure(pointer, thenblock != bodyblock);
				processed.put(pointer, whstruct);
				controls.put(pointer, new ControlStatement(ControlStatementType.CONTINUE, whstruct));
				controls.put(nextblock, new ControlStatement(ControlStatementType.BREAK, whstruct));
				Set<BasicBlock> bodyblocks = GraphUtil.getReachables(leftNodes, edges, bodyblock, nextblock);
				bodyblocks.remove(pointer);
				whstruct.body = structuralize(bodyblocks, bodyblock, controls, caringTry);
				controls.remove(pointer);
				controls.remove(nextblock);
				if (nextblock == null) {
					whstruct.next = null;
				} else {
					predecessor.put(nextblock, whstruct);
					whstruct.next = structuralize(GraphUtil.getReachables(leftNodes, edges, nextblock, null), nextblock, controls, caringTry);
				}
				return whstruct;
			}
		}
		case SWITCH: {
			// TODO blocks에서 pointer로 오는 엣지가 있으면 loop 따위로 묶어서 controls로 처리해 주어야 할듯
			throw new UnsupportedStructureException("Switch");
		}
		case GOTO: {
			assert normaloutedges.length == 1;
			left.remove(pointer);
			FlatStructure struct = new FlatStructure(pointer);
			processed.put(pointer, struct);
			if (left.isEmpty()) {
				return struct;
			} else {
				predecessor.put(normaloutedges[0].getDestination(), struct);
				struct.next = structuralize(left, normaloutedges[0].getDestination(), controls, caringTry);
				return struct;
			}
		}
		case THROW: {
			assert normaloutedges.length == 0;
			return new FlatStructure(pointer);
		}
		case RETURN: {
			assert normaloutedges.length == 0;
			return new FlatStructure(pointer);
		}
		default: {
			assert normaloutedges.length == 1;
			left.remove(pointer);
			FlatStructure struct = new FlatStructure(pointer);
			processed.put(pointer, struct);
			if (left.isEmpty()) {
				return struct;
			} else {
				predecessor.put(normaloutedges[0].getDestination(), struct);
				struct.next = structuralize(left, normaloutedges[0].getDestination(), controls, caringTry);
				return struct;
			}
		}
		}
	}
}
