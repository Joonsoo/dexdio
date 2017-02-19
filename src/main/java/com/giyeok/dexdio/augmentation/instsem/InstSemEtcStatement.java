package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.model0.DexCodeItem;



public class InstSemEtcStatement extends InstSemStatement {
	
	private String mnemonic;
	private InstSemElement[] useoperands;
	
	private DexCodeItem codeitem;

	/**
	 * operands는 모두 읽기에만 사용되는 것으로 간주
	 * @param opcode
	 * @param useoperands
	 */
	public InstSemEtcStatement(DexCodeItem codeitem, String mnemonic, InstSemElement[] useoperands) {
		this.codeitem = codeitem;
		
		this.mnemonic = mnemonic;
		this.useoperands = useoperands;
		
		for (InstSemElement op: useoperands) {
			op.setStatement(this);
		}
	}
	
	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public String getOpcodeMnemonic() {
		return mnemonic;
	}
	
	public InstSemElement[] getUseOperands() {
		return useoperands;
	}

	@Override
	public String getStringRepresentation() {
		StringBuffer buf = new StringBuffer();
		
		buf.append(mnemonic);
		buf.append(" ");
		for (int i = 0; i < useoperands.length; i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(useoperands[i].getStringRepresentation());
		}
		return buf.toString();
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public InstSemStatement getStatement() {
		return this;
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.ETC_STATEMENT;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return useoperands;
	}
}
