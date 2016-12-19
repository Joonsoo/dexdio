package com.giyeok.dexdio.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.giyeok.dexdio.dexreader.structs.code_item;
import com.giyeok.dexdio.dexreader.structs.encoded_catch_handler;
import com.giyeok.dexdio.dexreader.structs.encoded_type_addr_pair;
import com.giyeok.dexdio.dexreader.structs.try_item;
import com.giyeok.dexdio.model.insns.DexDisassembler;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.util.Pair;

public class DexCodeItem {
	private DexProgram program;
	private DexMethod belonged;
	
	private int registersSize;
	private int insSize, outsSize;
	private int triesSize;
	// debug_info_off?
	private int insns_size;
	private ArrayList<DexInstruction> instructions;
	
	public class Try {
		private int start_addr;
		private int insns_count;
		private Handler handler;
		
		public Try(int start_addr, int insns_count, Handler handler) {
			this.start_addr = start_addr;
			this.insns_count = insns_count;
			assert handler != null;
			this.handler = handler;
			handler.tries.add(this);
		}
		
		public int getStartAddress() {
			return start_addr;
		}
		
		public int getInstructionsLength() {
			return insns_count;
		}
		
		public int getStartIndex() {
			return getIndexOfInstructionByAddress(getStartAddress());
		}
		
		/**
		 * try body에 해당하는 마지막 인스트럭션의 다음 인스트럭션의 인덱스를 반환한다.
		 * 이 때, try body의 마지막 인스트럭션이 codeitem의 마지막 인스트럭션이라 다음 인스트럭션이 존재하지 않으면 -1을 반환한다
		 * @return
		 */
		public int getFinIndex() {
			return getIndexOfInstructionByAddress(getStartAddress() + getInstructionsLength());
		}
		
		public Handler getHandler() {
			return handler;
		}
	}
	
	public class Handler {
		public class HandlerItem {
			private DexType type;
			private int address;
			
			public HandlerItem(DexType type, int address) {
				this.type = type;
				this.address = address;
			}
			
			public DexType getExceptionType() {
				return type;
			}
			
			public int getHandlerAddress() {
				return address;
			}
		}
		
		private HandlerItem[] handlers;
		private int catch_all_addr;
		
		private ArrayList<Try> tries;
		
		public Handler(encoded_catch_handler handler) {
			assert (Math.abs(handler.size()) == handler.handlers().length);
			
			encoded_type_addr_pair pairs[] = handler.handlers();
			
			handlers = new HandlerItem[pairs.length];
			for (int i = 0; i < pairs.length; i++) {
				handlers[i] = new HandlerItem(program.getTypeByTypeId(pairs[i].type_idx()), pairs[i].addr());
			}
			
			if (handler.size() > 0) {
				this.catch_all_addr = -1;
			} else {
				this.catch_all_addr = handler.catch_all_addr();
			}
			
			this.tries = new ArrayList<Try>();
		}
		
		public Try[] getTries() {
			return tries.toArray(new Try[0]);
		}
		
		public Set<Integer> getHandlerStartPoints() {
			Set<Integer> set = new HashSet<Integer>();
			
			if (catch_all_addr >= 0) {
				set.add(catch_all_addr);
			}
			for (int i = 0; i < handlers.length; i++) {
				set.add(handlers[i].address);
			}
			return set;
		}
		
		public HandlerItem[] getHandlerItems() {
			return handlers;
		}
		
		public int getCatchAllAddr() {
			return catch_all_addr;
		}
	}
	
	private Try[] tries;
	private Handler[] handlers;
	
	public Try[] getTries() {
		return tries;
	}
	
	public Handler[] getHandlers() {
		return handlers;
	}
	
	public class DexRegister {
		private String name;
		private DexRegister next;
		
		public DexRegister(String name, DexRegister next) {
			this.name = name;
			this.next = next;
		}
		
		public String getName() {
			return name;
		}
		
		public DexRegister getNextRegister() {
			return next;
		}
		
		public DexCodeItem getBelongedCodeItem() {
			return DexCodeItem.this;
		}
		
		@Override
		public String toString() {
			return getName();
		}
	}
	
	private DexRegister[] registers;
	private DexRegister instanceRegister;
	
	public DexCodeItem(DexProgram program, DexMethod belonged, code_item code) throws DexException {
		this.program = program;
		this.belonged = belonged;
		
		this.registersSize = code.registers_size();
		this.insSize = code.ins_size();
		this.outsSize = code.outs_size();
		this.triesSize = code.tries_size();
		
		this.registers = new DexRegister[registersSize];
		DexRegister next = null;
		for (int i = registersSize - 1; i >= 0; i--) {
			registers[i] = new DexRegister("v" + i, next);
			next = registers[i];
		}

		DexParameter[] params = belonged.getParameters();
		int registerIndex = registersSize - 1;
		for (int i = params.length - 1; i >= 0; i--) {
			if (params[i].getType().isWide()) {
				registerIndex -= 1;
			}
			params[i].setRegister(registers[registerIndex]);
			registerIndex -= 1;
		}
		if (belonged.isIntanceMethod()) {
			instanceRegister = registers[registerIndex];
			instanceRegister.name = "this";
		} else {
			instanceRegister = null;
		}
		
		this.instructions = new ArrayList<DexInstruction>();
		
		if (code.tries_size() == 0) {
			tries = null;
			handlers = null;
		} else {
			tries = new Try[code.tries_size()];
			handlers = new DexCodeItem.Handler[code.handlers().size()];
			
			Map<Integer, Handler> handlerByOffset = new HashMap<Integer, DexCodeItem.Handler>();
			int offset = code.handlers().getStartingOffset();
			encoded_catch_handler[] handlerlist = code.handlers().list();
			assert handlerlist.length == code.handlers().size();
			
			for (int i = 0; i < handlerlist.length; i++) {
				Handler handler = new Handler(handlerlist[i]);
				handlers[i] = handler;
				handlerByOffset.put(offset, handler);
				offset += handlerlist[i].getByteLength();
			}
			
			try_item[] trieslist = code.tries();
			assert trieslist.length == code.tries_size();
			
			for (int i = 0; i < trieslist.length; i++) {
				try_item tryitem = trieslist[i];
				if (! handlerByOffset.containsKey(tryitem.handler_off())) {
					throw new DexException("try_item refers to a non-existing handler");
				}
				tries[i] = new Try(tryitem.start_addr(), tryitem.insn_count(), handlerByOffset.get(tryitem.handler_off()));
			}
		}
		
		// System.out.println(belonged.getBelongedType().getTypeFullNameBeauty() + "." + belonged.getName());
		// System.out.println(belonged.getBelongedType().getTypeId() + " " + belonged.getMethodId());
		disassemble(code.insns(), code.insns_size());
	}
	
	public int getInsSize() {
		return insSize;
	}
	
	public int getOutsSize() {
		return outsSize;
	}
	
	public int getRegistersSize() {
		return registersSize;
	}
	
	public int getInstructionsSize() {
		return instructions.size();
	}
	
	public DexInstruction getInstruction(int index) {
		return instructions.get(index);
	}
	
	public DexInstruction[] getInstructionsArray() {
		return instructions.toArray(new DexInstruction[0]);
	}

	private Map<Integer, Integer> addressToIndex = null;
	
	private void initAddressToIndex() {
		if (addressToIndex == null) {
			addressToIndex = new HashMap<Integer, Integer>();
			for (int i = 0; i < instructions.size(); i++) {
				addressToIndex.put(instructions.get(i).getAddress(), i);
			}
		}
	}
	
	public DexInstruction getInstructionAtAddress(int address) {
		int index = getIndexOfInstructionByAddress(address);
		if (index < 0) {
			return null;
		} else {
			return getInstruction(index);
		}
	}
	
	public int getIndexOfInstructionByAddress(int address) {
		initAddressToIndex();
		if (addressToIndex.containsKey(address)) {
			return addressToIndex.get(address);
		} else {
			return -1;
		}
	}
	
	public int getIndexOfInstruction(DexInstruction instruction) {
		return instructions.indexOf(instruction);
	}
	
	public DexProgram getProgram() {
		return program;
	}
	
	public DexMethod getBelongedMethod() {
		return belonged;
	}
	
	public DexRegister getRegister(int number) {
		return registers[number];
	}
	
	/**
	 * returns the register which holds the instance where the method is running.
	 * It will return null if the belonged method is static method.
	 * @return
	 */
	public DexRegister getInstanceRegister() {
		return instanceRegister;
	}
	
	public static class SwitchTable {
		private ArrayList<Pair<Integer, Integer>> table;
		
		public SwitchTable() {
			table = new ArrayList<Pair<Integer,Integer>>();
		}
		
		public void put(int value, int relativeTarget) {
			table.add(new Pair<Integer, Integer>(value, relativeTarget));
		}
		
		public int getRelativeTarget(int value) {
			for (Pair<Integer, Integer> p: table) {
				if (p.getKey() == value) {
					return p.getValue();
				}
			}
			return -1;
		}
		
		public int size() {
			return table.size();
		}
		
		public Collection<Integer> values() {
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (Pair<Integer, Integer> p: table) {
				list.add(p.getValue());
			}
			return list;
		}
		
		public ArrayList<Pair<Integer, Integer>> getTable() {
			return table;
		}
	}
	
	public SwitchTable getPackedSwitchPayload(int address) throws DexException {
		if (insns[address] != 0x0100) {
			throw new DexException("packed-switch-payload must have ident of 0x0100");
		}
		int size = insns[address + 1];
		int firstKey = insns[address + 2] | (insns[address + 3] << 16);
		SwitchTable table = new SwitchTable();
		
		for (int i = 0; i < size; i++) {
			table.put(firstKey + i, insns[address + 4 + i * 2] | (insns[address + 4 + i * 2 + 1] << 16));
		}
		return table;
	}
	
	public SwitchTable getSparseSwitchPayload(int address) throws DexException {
		if (insns[address] != 0x0200) {
			throw new DexException("sparse-switch-payload must have ident of 0x0200");
		}
		int size = insns[address + 1];
		
		int keys[] = new int[size];
		int targets[] = new int[size];
		int p = address + 2;

		for (int i = 0; i < size; i++) {
			keys[i] = insns[p] | (insns[p + 1] << 16);
			p += 2;
		}
		for (int i = 0; i < size; i++) {
			targets[i] = insns[p] | (insns[p + 1] << 16);
			p += 2;
		}
		
		SwitchTable table = new SwitchTable();
		
		for (int i = 0; i < size; i++) {
			table.put(keys[i], targets[i]);
		}
		return table;
	}
	
	public static class FillArrayData {
	}
	
	public FillArrayData getFillArrayDataPayload(int address) throws DexException {
		if (insns[address] != 0x0300) {
			throw new DexException("fill-array-data-payload must have ident of 0x0300");
		}
		return null;
	}
	
	/**
	 * insns and pointer are only used by "disassemble" and "organize" methods.
	 */
	private int[] insns;
	private int pointer;
	
	/**
	 * Dalvik bytecode disassembler based on http://source.android.com/tech/dalvik/dalvik-bytecode.html
	 * @param insns
	 * @throws DexException 
	 */
	private void disassemble(int[] insns, int insns_size) throws DexException {
		DexInstruction instruction;
		Queue<Integer> leftAddresses;
		Set<Integer> doneAddresses;

		leftAddresses = new LinkedList<Integer>();
		doneAddresses = new HashSet<Integer>();
		
		leftAddresses.add(0);
		if (handlers != null) {
			for (int i = 0; i < handlers.length; i++) {
				for (int p: handlers[i].getHandlerStartPoints()) {
					if (! leftAddresses.contains(p)) {
						leftAddresses.add(p);
					}
				}
			}
		}
		this.insns = insns;
		instructions.clear();
		while (! leftAddresses.isEmpty()) {
			pointer = leftAddresses.poll();
			doneAddresses.add(pointer);
			instruction = DexDisassembler.disassemble1(this, insns, pointer);
			if (instruction == null) {
				throw new DexException("Code is ended unexpectedly");
			}
			instructions.add(instruction);
			for (int i: instruction.getPossibleGoThroughs()) {
				if ((! doneAddresses.contains(i)) && (! leftAddresses.contains(i))) {
					leftAddresses.add(i);
				}
			}
		}
		this.insns = null;
		
		Collections.sort(instructions, new Comparator<DexInstruction>() {

			@Override
			public int compare(DexInstruction arg0, DexInstruction arg1) {
				return arg0.getAddress() - arg1.getAddress();
			}
		});
		// TODO instructions 사이에 겹치는 부분이 없는지 확인해야 함
	}
}
