package com.giyeok.dexdio.dexreader;

import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EndianRandomAccessFile implements DataInput {
	private RandomAccessFile file;
	private boolean bigEndian = true;

	public EndianRandomAccessFile(File file, String mode)
			throws FileNotFoundException {
		this.file = new RandomAccessFile(file, mode);
	}
	
	public EndianRandomAccessFile(String name, String mode)
			throws FileNotFoundException {
		this.file = new RandomAccessFile(name, mode);
	}
	
	public void setEndian(boolean bigEndian) {
		this.bigEndian = bigEndian;
	}
	
	public void close() throws IOException {
		file.close();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return file.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return file.readByte();
	}

	@Override
	public char readChar() throws IOException {
		if (bigEndian) {
			return file.readChar();
		} else {
			return (char)((readUnsignedByte()) | (readUnsignedByte() << 8));
		}
	}

	@Override
	public double readDouble() throws IOException {
		if (bigEndian) {
			return file.readDouble();
		} else {
			// TODO verify this
			return java.lang.Double.longBitsToDouble(readLong());
		}
	}

	@Override
	public float readFloat() throws IOException {
		if (bigEndian) {
			return file.readFloat();
		} else {
			// TODO verify this
			return java.lang.Float.intBitsToFloat(readInt());
		}
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		file.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		file.readFully(b, off, len);
	}

	@Override
	public int readInt() throws IOException {
		if (bigEndian) {
			return file.readInt();
		} else {
			return (readUnsignedByte() | 
					(readUnsignedByte() << 8) | 
					(readUnsignedByte() << 16) | 
					(readUnsignedByte() << 24));
		}
	}

	@Override
	public String readLine() throws IOException {
		return file.readLine();
	}

	@Override
	public long readLong() throws IOException {
		if (bigEndian) {
			return file.readLong();
		} else {
			return (((long) readUnsignedByte()) | 
					(((long) readUnsignedByte()) << 8) | 
					(((long) readUnsignedByte()) << 16) | 
					(((long) readUnsignedByte()) << 24) |
					(((long) readUnsignedByte()) << 32) | 
					(((long) readUnsignedByte()) << 40) | 
					(((long) readUnsignedByte()) << 48) | 
					(((long) readUnsignedByte()) << 56));
		}
	}

	@Override
	public short readShort() throws IOException {
		if (bigEndian) {
			return file.readShort();
		} else {
			return (short) 
					(((short) readUnsignedByte()) | 
					 (((short) readUnsignedByte()) << 8));
		}
	}

	@Override
	public String readUTF() throws IOException {
		return file.readUTF();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return file.readUnsignedByte();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return ((int) readShort()) & 0xffff;
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return file.skipBytes(n);
	}

	public void seek(long pos) throws IOException {
		file.seek(pos);
	}

	public long length() throws IOException {
		return file.length();
	}
	
	public long getFilePointer() throws IOException {
		return file.getFilePointer();
	}

	public boolean has() throws IOException {
		return getFilePointer() < length();
	}
	
}
