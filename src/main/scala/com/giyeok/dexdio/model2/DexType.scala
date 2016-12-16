package com.giyeok.dexdio.model2

import com.giyeok.dexdio.model._

case class InvalidDexTypeException() extends Exception

sealed trait DexType {
    val typeId: Int
    val typeName: String
    val javaTypeName: String

    def isWide: Boolean
}

sealed trait DexPrimitiveType extends DexType
case class DexVoid(typeId: Int) extends DexPrimitiveType {
    val typeName = "V"
    val javaTypeName = "void"
    val isWide = false
}
case class DexBoolean(typeId: Int) extends DexPrimitiveType {
    val typeName = "A"
    val javaTypeName = "boolean"
    val isWide = false
}
case class DexByte(typeId: Int) extends DexPrimitiveType {
    val typeName = "B"
    val javaTypeName = "byte"
    val isWide = false
}
case class DexShort(typeId: Int) extends DexPrimitiveType {
    val typeName = "S"
    val javaTypeName = "short"
    val isWide = false
}
case class DexChar(typeId: Int) extends DexPrimitiveType {
    val typeName = "C"
    val javaTypeName = "char"
    val isWide = false
}
case class DexInt(typeId: Int) extends DexPrimitiveType {
    val typeName = "I"
    val javaTypeName = "int"
    val isWide = false
}
case class DexLong(typeId: Int) extends DexPrimitiveType {
    val typeName = "J"
    val javaTypeName = "long"
    val isWide = true
}
case class DexFloat(typeId: Int) extends DexPrimitiveType {
    val typeName = "F"
    val javaTypeName = "float"
    val isWide = false
}
case class DexDouble(typeId: Int) extends DexPrimitiveType {
    val typeName = "D"
    val javaTypeName = "double"
    val isWide = true
}

sealed trait DexNonPrimitiveType extends DexType {
    val isWide = false
}
sealed trait DexClassType extends DexNonPrimitiveType
case class DexInternalClassType(typeId: Int, className: String) extends DexClassType {
    val typeName = s"L$className"
    val javaTypeName = className.replaceAllLiterally("/", ".")
}
case class DexExternalClassType(typeId: Int, className: String) extends DexClassType {
    val typeName = s"L$className"
    val javaTypeName = className.replaceAllLiterally("/", ".")
}
case class DexArrayType(typeId: Int, elemType: DexType) extends DexNonPrimitiveType {
    val typeName = s"[${elemType.typeName}"
    val javaTypeName = s"${elemType.typeName}[]"
}
