package com.giyeok.dexdio.augmentation;

import java.util.HashSet;
import java.util.Set;

import com.giyeok.dexdio.augmentation.instsem.InstSemStatement;
import com.giyeok.dexdio.model.DexProgram;

public class DeadCodeCollector extends Augmentation {

	public static DeadCodeCollector get(DexProgram program) {
		return (DeadCodeCollector) Augmentation.getAugmentation(DeadCodeCollector.class, program);
	}
	
	@Override
	protected String getAugmentationName() {
		return "Dead code collector";
	}

	private Set<InstSemStatement> deadcodes;
	
	public DeadCodeCollector(DexProgram program) {
		super(program);
		deadcodes = new HashSet<InstSemStatement>();
	}

	public boolean addDeadInstruction(InstSemStatement instruction) {
		return deadcodes.add(instruction);
	}
	
	public boolean isDeadInstruction(InstSemStatement instruction) {
		return deadcodes.contains(instruction);
	}

}
