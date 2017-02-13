package com.giyeok.dexdio.model2

import com.giyeok.dexdio.dexreader.structs.code_item

case class Id(dexId: Int, itemId: Int)
object Id extends Ordering[Id] {
    override def compare(x: Id, y: Id): Int =
        if (x.dexId != y.dexId) x.dexId - y.dexId else x.itemId - y.itemId

}

case class DexField0(fieldId: Id, fieldName: String, fieldType: DexType)
case class DexMethod0(methodId: Id, methodName: String, paramTypes: Seq[DexType], returnType: DexType)

case class DexAccessFlags(value: Int) extends AnyVal {

}

case class DexCodeItem(codeItem: code_item) {
}

// inherited field/method에 annotations가 있을 수 있을까? 없을거같은데..

sealed trait DexField {
    val fieldId: Id
    val fieldName: String
    val fieldType: DexType
}
case class DexInheritedField(
    fieldId: Id,
    fieldName: String,
    fieldType: DexType,
    annotations: Option[DexAnnotations]
) extends DexField
case class DexStaticField(
    fieldId: Id,
    fieldName: String,
    fieldType: DexType,
    defaultValue: Option[DexValue],
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags
) extends DexField
case class DexInstanceField(
    fieldId: Id,
    fieldName: String,
    fieldType: DexType,
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags
) extends DexField

case class DexParameter(
    paramType: DexType,
    annotations: Option[DexAnnotations] /* reigster: DexCodeItem#DexRegister */ )

sealed trait DexMethod {
    val methodId: Id
    val methodName: String
    val parameters: Seq[DexParameter]
    val returnType: DexType
}
case class DexInheritedMethod(
    methodId: Id,
    methodName: String,
    parameters: Seq[DexParameter],
    returnType: DexType,
    annotations: Option[DexAnnotations]
) extends DexMethod
case class DexDirectMethod(
    methodId: Id,
    methodName: String,
    parameters: Seq[DexParameter],
    returnType: DexType,
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags,
    codeitem: Option[DexCodeItem]
) extends DexMethod
case class DexVirtualMethod(
    methodId: Id,
    methodName: String,
    parameters: Seq[DexParameter],
    returnType: DexType,
    annotations: Option[DexAnnotations],
    accessFlags: DexAccessFlags,
    codeitem: Option[DexCodeItem]
) extends DexMethod

// type, field, method 등이 모두 만들어진 후에 그것들을 묶어서 쓰기 쉽게 클래스 단위로 묶음
sealed trait DexClass {
    val classId: Id
    val typeId: Id
    val className: String
    val accessFlags: DexAccessFlags
}

class DexDefinedClass(
        val classId: Id,
        val typeId: Id,
        val className: String,
        val accessFlags: DexAccessFlags,
        val annotations: Option[DexAnnotations],
        val superClass: Option[DexClassType],
        val implements: Seq[DexClassType],
        val inheritedFields: Seq[DexInheritedField],
        val staticFields: Seq[DexStaticField],
        val instanceFields: Seq[DexInstanceField],
        val inheritedMethods: Seq[DexInheritedMethod],
        val directMethods: Seq[DexDirectMethod],
        val virtualMethods: Seq[DexVirtualMethod]
) extends DexClass {
    implicit val ord = Id

    val fields: Seq[DexField] = (inheritedFields ++ staticFields ++ instanceFields) sortBy { _.fieldId }
    val methods: Seq[DexMethod] = (inheritedMethods ++ directMethods ++ virtualMethods) sortBy { _.methodId }
}

class DexMarkerClass(
        val classId: Id,
        val typeId: Id,
        val className: String,
        val accessFlags: DexAccessFlags,
        val annotations: Option[DexAnnotations],
        val superClass: Option[DexClassType],
        val implements: Seq[DexClassType]
) extends DexClass {

}