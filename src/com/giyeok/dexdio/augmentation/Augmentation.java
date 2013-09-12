package com.giyeok.dexdio.augmentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.insns.DexInstruction;


public abstract class Augmentation {
	
	private static Map<DexProgram, Map<Class<? extends Augmentation>, Augmentation>> augs = new HashMap<DexProgram, Map<Class<? extends Augmentation>,Augmentation>>();
	
	protected static <T extends Augmentation> Augmentation getAugmentation(Class<T> type, DexProgram program) {
		Map<Class<? extends Augmentation>, Augmentation> augset;
		
		if (! augs.containsKey(program)) {
			augset = new HashMap<Class<? extends Augmentation>, Augmentation>();
			augs.put(program, augset);
		} else {
			augset = augs.get(program);
		}
		
		if (augset.containsKey(type)) {
			return augset.get(type);
		} else {
			try {
				Constructor ctor = type.getDeclaredConstructor(DexProgram.class);
				type.getDeclaredConstructor(DexProgram.class);

				Augmentation aug = (Augmentation) ctor.newInstance(program);
				
				augset.put(type, aug);
				
				return aug;
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	protected abstract String getAugmentationName();
	
	private DexProgram program;
	
	public Augmentation(DexProgram program) {
		this.program = program;
	}
	
	public DexProgram getProgram() {
		return program;
	}

	protected interface InstructionVisitor {
		public void visit(DexProgram program, DexMethod method, DexInstruction instruction);
	}
	
	protected static void visitInstructions(DexProgram program, InstructionVisitor visitor) {
		int size = program.getMethodsCount();
		for (int i = 0; i < size; i++) {
			DexMethod method = program.getMethodByMethodId(i);
			DexCodeItem codeitem = method.getCodeItem();
			
			if (codeitem != null) {
				int insns = codeitem.getInstructionsSize();
				for (int j = 0; j < insns; j++) {
					DexInstruction instruction = codeitem.getInstruction(j);
					
					visitor.visit(program, method, instruction);
				}
			}
		}
	}
	
	protected interface MethodVisitor {
		public void visit(DexProgram program, DexMethod method);
	}
	
	protected static void visitMethods(DexProgram program, MethodVisitor visitor) {
		int size = program.getMethodsCount();
		for (int i = 0; i < size; i++) {
			DexMethod method = program.getMethodByMethodId(i);

			visitor.visit(program, method);
		}
	}
}
