package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.RandomAccessible;

/**
 * 
 * @author Joonsoo
 *
 */

public abstract class Value {
	public abstract void read(RandomAccessible stream) throws IOException;
	public abstract int getByteLength();
}
