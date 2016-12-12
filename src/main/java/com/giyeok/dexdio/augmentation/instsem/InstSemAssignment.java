package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.util.ArraysUtil;


public class InstSemAssignment extends InstSemStatement {
	
	private InstSemLHS lhs;
	private InstSemOperand rhs;
	
	private DexCodeItem codeitem;

	public InstSemAssignment(DexCodeItem codeitem, InstSemLHS lhs, InstSemOperand rhs) {
		this.codeitem = codeitem;
		
		this.lhs = lhs;
		this.rhs = rhs;
		
		lhs.setStatement(this);
		rhs.setStatement(this);
	}

	@Override
	public DexCodeItem getBelongedCodeItem() {
		return codeitem;
	}
	
	public InstSemLHS getLefthandside() {
		return lhs;
	}
	
	public InstSemOperand getRighthandside() {
		return rhs;
	}

	@Override
	public String getStringRepresentation() {
		return lhs.getStringRepresentation() + " = " + rhs.getStringRepresentation();
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
		return InstSemType.ASSIGNMENT_STATEMENT;
	}

	@Override
	public InstSemOperand[] getContainingOperands() {
		return ArraysUtil.concat(lhs.getContainingOperands(), rhs.getContainingOperands());
	}
}
