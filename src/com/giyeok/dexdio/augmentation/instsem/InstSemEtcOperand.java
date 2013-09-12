package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;

public class InstSemEtcOperand implements InstSemOperand {
	
	private String mnemonic;
	
	private DexCodeItem codeitem;
	
	public InstSemEtcOperand(DexCodeItem codeitem, String mnemonic) {
		this.codeitem = codeitem;

		this.mnemonic = mnemonic;
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}

	@Override
	public String getStringRepresentation() {
		return mnemonic;
	}
	
	private InstSemStatement statement;
	
	@Override
	public void setStatement(InstSemStatement statement) {
		this.statement = statement;
	}

	@Override
	public InstSemStatement getStatement() {
		return this.statement;
	}
	
	private DexType operandType;

	@Override
	public void setOperandTypeData(DexType type) {
		this.operandType = type;
	}

	@Override
	public DexType getOperandTypeData() {
		return operandType;
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.ETC_OPERAND;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return new InstSemOperand[] { this };
	}
}
