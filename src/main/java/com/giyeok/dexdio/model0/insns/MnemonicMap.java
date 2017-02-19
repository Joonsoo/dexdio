package com.giyeok.dexdio.model0.insns;

import java.util.HashMap;
import java.util.Map;

public class MnemonicMap {
	private Map<Integer, String> map;
	
	public MnemonicMap(Object[] mnemonics) {
		map = new HashMap<Integer, String>();
		
		for (int i = 0; i < mnemonics.length; i += 2) {
			map.put((Integer) mnemonics[i], (String) mnemonics[i + 1]);
		}
	}
	
	public boolean containsKey(int opcode) {
		return map.containsKey(opcode);
	}
	
	public String get(int opcode) {
		return map.get(opcode);
	}
}
