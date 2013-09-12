package com.giyeok.dexdio.util;

public class MutableValuePair<K, V> extends Pair<K, V> {
	public MutableValuePair(K key, V value) {
		super(key, value);
	}
	
	public void setValue(V v) {
		this.value = v;
	}
}
