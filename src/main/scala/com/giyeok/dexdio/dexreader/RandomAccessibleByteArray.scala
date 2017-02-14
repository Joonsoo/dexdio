package com.giyeok.dexdio.dexreader

class RandomAccessibleByteArray(bytes: Array[Byte]) extends RandomAccessible {
    private var isBigEndian: Boolean = true
    private var pointer: Long = 0

    def setEndian(bigEndian: Boolean): Unit = { this.isBigEndian = bigEndian }

    def length: Long = bytes.length

    def seek(pos: Long): Unit = { pointer = pos }

    def has(): Boolean = pointer < length

    def read(): Byte = {
        val b = bytes(pointer.toInt)
        pointer += 1
        b
    }

    def readBoolean(): Boolean = (read() != 0)

    def readByte(): Byte = read()

    def readChar(): Char = {
        val a = readUnsignedByte()
        val b = readUnsignedByte()
        if (isBigEndian) {
            ((a << 8) | b).toChar
        } else {
            (a | (b << 8)).toChar
        }
    }

    def readShort(): Short = {
        val a = readUnsignedByte()
        val b = readUnsignedByte()
        if (isBigEndian) {
            ((a << 8) | b).toShort
        } else {
            (a | (b << 8)).toShort
        }
    }

    def readInt(): Int = {
        val a = readUnsignedByte()
        val b = readUnsignedByte()
        val c = readUnsignedByte()
        val d = readUnsignedByte()
        if (isBigEndian) {
            (a << 24) | (b << 16) | (c << 8) | d
        } else {
            a | (b << 8) | (c << 16) | (d << 24)
        }
    }

    def readLong(): Long = {
        val a = readUnsignedByte().toLong
        val b = readUnsignedByte().toLong
        val c = readUnsignedByte().toLong
        val d = readUnsignedByte().toLong
        val e = readUnsignedByte().toLong
        val f = readUnsignedByte().toLong
        val g = readUnsignedByte().toLong
        val h = readUnsignedByte().toLong
        if (isBigEndian) {
            (a << 56) | (b << 48) | (c << 40) | (d << 32) |
                (e << 24) | (f << 16) | (g << 8) | h
        } else {
            a | (b << 8) | (c << 16) | (d << 24) |
                (e << 32) | (f << 40) | (g << 48) | (h << 56)
        }
    }

    def readFloat(): Float =
        java.lang.Float.intBitsToFloat(readInt())

    def readDouble(): Double =
        java.lang.Double.longBitsToDouble(readLong())

    def readUnsignedByte(): Int =
        read().toInt & 0xff

    def readUnsignedShort(): Int =
        readShort().toInt & 0xffff

    def readFully(bytes: Array[Byte]): Unit = {
        System.arraycopy(this.bytes, pointer.toInt, bytes, 0, bytes.length)
        pointer += bytes.length
    }
}
