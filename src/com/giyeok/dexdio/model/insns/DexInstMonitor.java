package com.giyeok.dexdio.model.insns;

import com.giyeok.dexdio.augmentation.Logger;
import com.giyeok.dexdio.augmentation.Logger.Log;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class DexInstMonitor extends DexInstruction {

	public DexInstMonitor(DexCodeItem codeitem, int address, InstructionData instructionData) {
		super(codeitem, address, instructionData);

		assert mnemonics.containsKey(getOpcode());
		assert getOperandsLength() == 1;
		assert getOperand(0) instanceof OperandRegister;
		
		Logger.addMessage(getProgram(), Log.line(new Log[] {
				Log.log(getOpcodeMnemonic() + " appeared at "),
				Log.log(codeitem.getBelongedMethod()),
				Log.log(" " + Integer.toHexString(address))
		}));
	}
	
	public OperandRegister getTarget() {
		return (OperandRegister) getOperand(0);
	}
	
	public DexRegister getTargetRegister() {
		return getTarget().getRegister();
	}

	public static enum MonitorType {
		ENTER,
		EXIT
	}
	
	public MonitorType getMonitorType() {
		switch (getOpcode()) {
		case 0x1d:
			return MonitorType.ENTER;
		case 0x1e:
			return MonitorType.EXIT;
		}
		assert false;
		return null;
	}

	private static final MnemonicMap mnemonics = new MnemonicMap(new Object[] {
			0x1d, "monitor-enter",
			0x1e, "monitor-exit"
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
		return DexInstructionType.MONITOR;
	}
}
