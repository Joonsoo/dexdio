package com.giyeok.dexdio.model

case class InvalidDexTypeException(typeName: String) extends Exception

sealed trait DexType {
    val typeId: Id
    val typeName: String
    val javaTypeName: String

    def isWide: Boolean
}

sealed trait DexPrimitiveType extends DexType
case class DexVoidType(typeId: Id) extends DexPrimitiveType {
    val typeName = "V"
    val javaTypeName = "void"
    val isWide = false
}
case class DexBooleanType(typeId: Id) extends DexPrimitiveType {
    val typeName = "A"
    val javaTypeName = "boolean"
    val isWide = false
}
case class DexByteType(typeId: Id) extends DexPrimitiveType {
    val typeName = "B"
    val javaTypeName = "byte"
    val isWide = false
}
case class DexShortType(typeId: Id) extends DexPrimitiveType {
    val typeName = "S"
    val javaTypeName = "short"
    val isWide = false
}
case class DexCharType(typeId: Id) extends DexPrimitiveType {
    val typeName = "C"
    val javaTypeName = "char"
    val isWide = false
}
case class DexIntType(typeId: Id) extends DexPrimitiveType {
    val typeName = "I"
    val javaTypeName = "int"
    val isWide = false
}
case class DexLongType(typeId: Id) extends DexPrimitiveType {
    val typeName = "J"
    val javaTypeName = "long"
    val isWide = true
}
case class DexFloatType(typeId: Id) extends DexPrimitiveType {
    val typeName = "F"
    val javaTypeName = "float"
    val isWide = false
}
case class DexDoubleType(typeId: Id) extends DexPrimitiveType {
    val typeName = "D"
    val javaTypeName = "double"
    val isWide = true
}

sealed trait DexNonPrimitiveType extends DexType {
    val isWide = false
}
sealed trait DexClassType extends DexNonPrimitiveType {
    val className: String

    val typeName: String = s"L$className"
    val javaTypeName: String = className.replaceAllLiterally("/", ".")
}
case class DexInternalClassType(typeId: Id, className: String) extends DexClassType
case class DexExternalClassType(typeId: Id, className: String) extends DexClassType
case class DexArrayType(typeId: Id, elemType: DexType) extends DexNonPrimitiveType {
    val typeName = s"[${elemType.typeName}"
    val javaTypeName = s"${elemType.typeName}[]"
}

case class DexUnspecifiedClassType(className: String) extends DexClassType {
    lazy val typeId: Id = ???
}
