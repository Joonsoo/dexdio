package com.giyeok.dexdio.dexreader.value;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.EndianRandomAccessFile;

/**
 * 
 * @author Joonsoo
 *
 */

public abstract class Value {
	public abstract void read(EndianRandomAccessFile stream) throws IOException;
	public abstract int getByteLength();
}
