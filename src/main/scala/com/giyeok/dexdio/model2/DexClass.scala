package com.giyeok.dexdio.model2

case class DexField0(fieldId: Int, fieldName: String, fieldType: DexType)
case class DexMethod0(methodId: Int, methodName: String, paramTypes: Seq[DexType], returnType: DexType)

case class DexAccessFlags(value: Int) {

}

trait DexCodeItem

// inherited field/method에 annotations가 있을 수 있을까? 없을거같은데..

sealed trait DexField {
    val fieldId: Int
    val fieldName: String
    val fieldType: DexType
}
case class DexInheritedField(
    fieldId: Int,
    fieldName: String,
    fieldType: DexType,
    annotations: Option[DexAnnotations]) extends DexField
case class DexStaticField(
    fieldId: Int,
    fieldName: String,
    fieldType: DexType,
    defaultValue: Option[DexValue],
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags) extends DexField
case class DexInstanceField(
    fieldId: Int,
    fieldName: String,
    fieldType: DexType,
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags) extends DexField

case class DexParameter(
    paramType: DexType,
    paramName: String,
    annotations: Option[DexAnnotations] /* reigster: DexCodeItem#DexRegister */ )

sealed trait DexMethod {
    val methodId: Int
    val methodName: String
    val parameters: Seq[DexParameter]
    val returnType: DexType
}
case class DexInheritedMethod(
    methodId: Int,
    methodName: String,
    parameters: Seq[DexParameter],
    returnType: DexType,
    annotations: Option[DexAnnotations]) extends DexMethod
case class DexDirectMethod(
    methodId: Int,
    methodName: String,
    parameters: Seq[DexParameter],
    returnType: DexType,
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags,
    codeitem: Option[DexCodeItem]) extends DexMethod
case class DexVirtualMethod(
    methodId: Int,
    methodName: String,
    parameters: Seq[DexParameter],
    returnType: DexType,
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags,
    codeitem: Option[DexCodeItem]) extends DexMethod

// type, field, method 등이 모두 만들어진 후에 그것들을 묶어서 쓰기 쉽게 클래스 단위로 묶음
class DexClass(
        val classId: Int,
        val className: String,
        val superClass: Option[DexClass],
        val implements: Seq[DexClass],
        val inheritedFields: Seq[DexInheritedField],
        val staticFields: Seq[DexStaticField],
        val instanceFields: Seq[DexInstanceField],
        val inheritedMethods: Seq[DexInheritedMethod],
        val directMethods: Seq[DexDirectMethod],
        val virtualMethods: Seq[DexVirtualMethod]) {

}
