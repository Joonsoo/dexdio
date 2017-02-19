package com.giyeok.dexdio.model0.insns;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model0.DexCodeItem.SwitchTable;
import com.giyeok.dexdio.model0.DexException;

public class DexInstSwitch extends DexInstruction {
	private SwitchTable table;

	public DexInstSwitch(DexCodeItem codeitem, int address, InstructionData instructionData) throws DexException {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 2;
		assert getOperand(0) instanceof OperandRegister;
		assert getOperand(1) instanceof OperandBranchTarget;
		
		int tableAddress = ((OperandBranchTarget) getOperand(1)).value() + address;
		if (getOpcode() == 0x2b) {
			// packed-switch
			table = codeitem.getPackedSwitchPayload(tableAddress);
		} else {
			// sparse-switch
			table = codeitem.getSparseSwitchPayload(tableAddress);
		}
	}
	
	public OperandRegister getValue() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getValueRegister() {
		return getValue().getRegister();
	}
	
	public OperandBranchTarget getPayloadAddress() {
		return (OperandBranchTarget) getOperand(1);
	}
	
	@Override
	public int[] getPossibleGoThroughs() {
		int gothrough[] = new int[table.size() + 1];
		int k = 0;
		for (int i: table.values()) {
			gothrough[k++] = getAddress() + i;
		}
		gothrough[k] = getFallThroughAddress();
		return gothrough;
	}
	
	public SwitchTable getSwitchTable() {
		return table;
	}

	public int getBranchAddressForValue(int value) {
		return getAddress() + table.getRelativeTarget(value);
	}
	
	public int getFallThroughAddress() {
		return getAddress() + getLength();
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x2b, "packed-switch",
			0x2c, "sparse-switch"
	});
	
	@Override
	public MnemonicMap getMnemonicMap() {
		return mnemonics;
	}

	@Override
	public boolean canThrowException() {
		// not sure
		return true;
	}

	@Override
	public DexInstructionType getInstructionType() {
		return DexInstructionType.SWITCH;
	}
}
