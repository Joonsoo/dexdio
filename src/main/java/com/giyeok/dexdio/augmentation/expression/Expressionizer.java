package com.giyeok.dexdio.augmentation.expression;

import com.giyeok.dexdio.augmentation.Augmentation;
import com.giyeok.dexdio.model.DexProgram;

public class Expressionizer extends Augmentation {

	public static Expressionizer get(DexProgram program) {
		return (Expressionizer) Augmentation.getAugmentation(Expressionizer.class, program);
	}

	@Override
	protected String getAugmentationName() {
		return "Expressionizer";
	}

	public Expressionizer(DexProgram program) {
		super(program);
		// TODO Auto-generated constructor stub
	}

}
