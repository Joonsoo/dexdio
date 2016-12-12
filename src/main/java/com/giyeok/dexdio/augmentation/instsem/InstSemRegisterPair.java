package com.giyeok.dexdio.augmentation.instsem;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;

public class InstSemRegisterPair extends InstSemRegister {

	private DexRegister nextregister;
	
	public InstSemRegisterPair(DexCodeItem codeitem, DexRegister register) {
		super(codeitem, register);
		
		assert register.getNextRegister() != null;
		nextregister = register.getNextRegister();
	}
	
	public InstSemRegisterPair(DexCodeItem codeitem, DexRegister reg1, DexRegister reg2) {
		super(codeitem, reg1);
		this.nextregister = reg2;
	}
	
	public DexRegister getNextRegister() {
		return nextregister;
	}

	@Override
	public String getStringRepresentation() {
		return "{" + register.getName() + ":" + nextregister.getName() + "}";
	}
	
	@Override
	public String toString() {
		return getStringRepresentation();
	}

	@Override
	public boolean mayReferSamePlace(InstSemLHS other) {
		if (other instanceof InstSemRegisterPair) {
			InstSemRegisterPair otherreg = (InstSemRegisterPair) other;
			return this.register == otherreg.register && this.nextregister == otherreg.nextregister;
		}
		return false;
	}

	@Override
	public InstSemType getInstSemType() {
		return InstSemType.REGISTER_PAIR;
	}
}
