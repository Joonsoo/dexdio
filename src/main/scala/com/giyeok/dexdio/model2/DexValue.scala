package com.giyeok.dexdio.model2

sealed trait DexValue

case class DexByteValue(value: Byte) extends DexValue
case class DexShortValue(value: Short) extends DexValue
case class DexCharValue(value: Char) extends DexValue
case class DexIntValue(value: Int) extends DexValue
case class DexLongValue(value: Long) extends DexValue
case class DexFloatValue(value: Float) extends DexValue
case class DexDoubleValue(value: Double) extends DexValue
case class DexStringValue(value: String) extends DexValue
case class DexTypeValue(value: DexType) extends DexValue
case class DexFieldValue(value: DexField0) extends DexValue
case class DexMethodValue(value: DexMethod0) extends DexValue
case class DexEnumValue(value: DexField0) extends DexValue
case class DexArrayValue(value: IndexedSeq[DexValue]) extends DexValue
case class DexAnnotationValue(values: Seq[(String, DexValue)]) extends DexValue {
    lazy val valuesMap = values toMap
}
case object DexNullValue extends DexValue
case class DexBooleanValue(value: Boolean) extends DexValue

case class DexAnnotationItem(visibility: Short, annots: Seq[(String, DexValue)])
case class DexAnnotations(annotations: Seq[DexAnnotationItem])
