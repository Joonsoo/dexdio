package com.giyeok.dexdio.dexreader

import java.io.File
import java.io.RandomAccessFile

class RandomAccessibleFile(file: File) extends RandomAccessible {
    private val f = new RandomAccessFile(file, "r")
    private var isBigEndian: Boolean = true

    def setEndian(isBigEndian: Boolean): Unit = { this.isBigEndian = isBigEndian }

    def length: Long = f.length()

    def seek(pos: Long): Unit = f.seek(pos)

    def has(): Boolean = f.getFilePointer < length

    def readBoolean(): Boolean = f.readBoolean()

    def readByte(): Byte = f.readByte()

    def readChar(): Char =
        if (isBigEndian) f.readChar() else {
            val a = readUnsignedByte()
            val b = readUnsignedByte()
            (a | (b << 8)).toChar
        }

    def readShort(): Short =
        if (isBigEndian) f.readShort() else {
            val a = readUnsignedByte()
            val b = readUnsignedByte()
            (a | (b << 8)).toShort
        }

    def readInt(): Int =
        if (isBigEndian) f.readInt() else {
            val a = readUnsignedByte()
            val b = readUnsignedByte()
            val c = readUnsignedByte()
            val d = readUnsignedByte()
            a | (b << 8) | (c << 16) | (d << 24)
        }

    def readLong(): Long =
        if (isBigEndian) f.readLong() else {
            val a = readUnsignedByte().toLong
            val b = readUnsignedByte().toLong
            val c = readUnsignedByte().toLong
            val d = readUnsignedByte().toLong
            val e = readUnsignedByte().toLong
            val f = readUnsignedByte().toLong
            val g = readUnsignedByte().toLong
            val h = readUnsignedByte().toLong
            a | (b << 8) | (c << 16) | (d << 24) |
                (e << 32) | (f << 40) | (g << 48) | (h << 56)
        }

    def readFloat(): Float =
        if (isBigEndian) f.readFloat() else {
            java.lang.Float.intBitsToFloat(readInt())
        }

    def readDouble(): Double =
        if (isBigEndian) f.readDouble() else {
            java.lang.Double.longBitsToDouble(readLong())
        }

    def readUnsignedByte(): Int =
        f.readUnsignedByte()

    def readUnsignedShort(): Int =
        readShort().toInt & 0xffff

    def readFully(bytes: Array[Byte]): Unit = f.readFully(bytes)
}
