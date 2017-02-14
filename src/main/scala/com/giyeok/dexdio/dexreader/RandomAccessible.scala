package com.giyeok.dexdio.dexreader

trait RandomAccessible {
    def setEndian(bigEndian: Boolean): Unit

    def length: Long
    def seek(pos: Long): Unit
    def has(): Boolean

    def readBoolean(): Boolean
    def readByte(): Byte
    def readChar(): Char
    def readShort(): Short
    def readInt(): Int
    def readLong(): Long
    def readFloat(): Float
    def readDouble(): Double

    def readUnsignedByte(): Int
    def readUnsignedShort(): Int

    def readFully(bytes: Array[Byte]): Unit
}
