package com.giyeok.dexdio.model2

import com.giyeok.dexdio.dexreader.DalvikExecutable
import com.giyeok.dexdio.dexreader.structs.encoded_field
import com.giyeok.dexdio.dexreader.structs.encoded_method
import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo
import com.giyeok.dexdio.dexreader.structs.encoded_array_item
import com.giyeok.dexdio.dexreader.structs.encoded_array
import com.giyeok.dexdio.dexreader.structs.encoded_value
import com.giyeok.dexdio.dexreader.value.Value
import com.giyeok.dexdio.dexreader.structs.encoded_annotation
import com.giyeok.dexdio.dexreader.structs.annotation_item

class DexProgram(dex: DalvikExecutable) {
    val stringTable = dex.getStringTable()
    val typeTable = dex.getTypeTable()
    val protoTable = dex.getProtoTable()
    val fieldTable = dex.getFieldTable()
    val methodTable = dex.getMethodTable()
    val classTable = dex.getClassTable()

    private def check(pred: => Boolean): Unit = {}

    val types: IndexedSeq[DexType] = {
        val typesMap = ((0 until typeTable.size()) map { i =>
            i -> typeTable.getTypeName(i)
        }).toMap
        check(typesMap.values.toSeq.distinct == typesMap.values.toSeq)
        val reverseTypesMap = typesMap map { kv => kv._2 -> kv._1 }

        def dexTypeOf(typeName: String, typeId: Int): DexType =
            (typeName.charAt(0), typeName) match {
                case ('V', "V") => DexVoidType(typeId)
                case ('Z', "Z") => DexBooleanType(typeId)
                case ('B', "B") => DexByteType(typeId)
                case ('S', "S") => DexShortType(typeId)
                case ('C', "C") => DexCharType(typeId)
                case ('I', "I") => DexIntType(typeId)
                case ('J', "J") => DexLongType(typeId)
                case ('F', "F") => DexFloatType(typeId)
                case ('D', "D") => DexDoubleType(typeId)
                case ('L', lClassName) =>
                    val classDef = classTable.getClassDefByTypeId(typeId)
                    if (classDef != null) {
                        check(classDef.getClassTypeId() == typeId)
                        check(typeTable.getTypeName(classDef.getClassTypeId) == typeName)
                        DexInternalClassType(typeId, lClassName)
                    } else {
                        DexExternalClassType(typeId, lClassName)
                    }
                case ('[', arrayType) =>
                    def elemType(elemTypeName: String): DexType =
                        reverseTypesMap get elemTypeName match {
                            case Some(elemTypeId) => dexTypeOf(elemTypeName, elemTypeId)
                            case None =>
                                if (elemTypeName.charAt(0) == '[') {
                                    DexArrayType(typeId, elemType(elemTypeName.substring(1)))
                                } else {
                                    throw InvalidDexTypeException()
                                }
                        }
                    DexArrayType(typeId, elemType(arrayType.substring(1)))
                case _ => ???
            }
        (0 until typeTable.size()) map { i =>
            dexTypeOf(typeTable.getTypeName(i), i)
        }
    }

    val fields =
        (0 until fieldTable.size()) map { fieldId =>
            val fieldDef = fieldTable.get(fieldId)
            val fieldType = types(fieldDef.getTypeIdx())
            val fieldName = fieldDef.getName()
            DexField0(fieldId, fieldName, fieldType)
        }

    val methods =
        (0 until methodTable.size()) map { methodId =>
            val methodDef = methodTable.get(methodId)
            val methodName = methodDef.getName()
            val methodProto = methodDef.getProto()
            val paramTypes = (0 until methodProto.getParametersCount()) map { i => types(methodProto.getParameter(i)) }
            val returnType = types(methodProto.getReturnTypeIdx())
            DexMethod0(methodId, methodName, paramTypes, returnType)
        }

    private def noneIfNull[T](value: T) =
        if (value == null) None else Some(value)

    private def decodeDexValue(v: encoded_value): DexValue = {
        import com.giyeok.dexdio.dexreader.value
        v.getValueType() match {
            case encoded_value.VALUE_BYTE =>
                DexByteValue(v.getValue().asInstanceOf[value.Byte].getValue)
            case encoded_value.VALUE_SHORT =>
                DexShortValue(v.getValue().asInstanceOf[value.Short].getValue)
            case encoded_value.VALUE_CHAR =>
                DexCharValue(v.getValue().asInstanceOf[value.Short].getValue.toChar)
            case encoded_value.VALUE_INT =>
                DexIntValue(v.getValue().asInstanceOf[value.Int].getValue)
            case encoded_value.VALUE_LONG =>
                DexLongValue(v.getValue().asInstanceOf[value.Long].getValue)
            case encoded_value.VALUE_FLOAT =>
                DexFloatValue(v.getValue().asInstanceOf[value.Float].getValue)
            case encoded_value.VALUE_DOUBLE =>
                DexDoubleValue(v.getValue().asInstanceOf[value.Double].getValue)
            case encoded_value.VALUE_STRING =>
                DexStringValue(stringTable.get(v.getValue().asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_TYPE =>
                DexTypeValue(types(v.getValue().asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_FIELD =>
                DexFieldValue(fields(v.getValue().asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_METHOD =>
                DexMethodValue(methods(v.getValue().asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_ENUM =>
                DexEnumValue(fields(v.getValue().asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_ARRAY =>
                val elems = v.getValue().asInstanceOf[encoded_array].values()
                DexArrayValue((0 until elems.length) map { i => decodeDexValue(elems(i)) })
            case encoded_value.VALUE_ANNOTATION =>
                val annots = decodeEncodedAnnotation(v.getValue().asInstanceOf[encoded_annotation])
                DexAnnotationValue(annots)
            case encoded_value.VALUE_NULL =>
                DexNullValue
            case encoded_value.VALUE_BOOLEAN =>
                DexBooleanValue(v.getValue().asInstanceOf[value.Byte].getValue == 1)
        }
    }

    private def decodeEncodedAnnotation(annot: encoded_annotation) =
        (annot.elements() map { e =>
            stringTable.get(e.name_idx()) -> decodeDexValue(e.value())
        }).toSeq

    private def decodeAnnotations(annotations: AnnotationsInfo#Annotations): DexAnnotations = {
        DexAnnotations(annotations.annotations() map { annot =>
            DexAnnotationItem(annot.visibility(), decodeEncodedAnnotation(annot.annotation()))
        })
    }

    val classes = {
        val fieldIdsByCls = (0 until fieldTable.size()) groupBy { fieldId =>
            fieldTable.get(fieldId).getClassIdx()
        } mapValues { _.toSet }

        val methodIdsByCls = (0 until methodTable.size()) groupBy { methodId =>
            methodTable.get(methodId).getClassIdx()
        } mapValues { _.toSet }

        // TODO 클래스가 아닌 타입을 가리키고 있으면 문제

        (0 until classTable.size()) map { classId =>
            val classDef = classTable.getClassByClassId(classId)
            val classTypeId = classDef.getClassTypeId()
            check(classDef.getClassTypeName().length() > 0 && classDef.getClassTypeName().charAt(0) == 'L')
            val accessFlags = DexAccessFlags(classDef.getAccessFlags())
            val className = classDef.getClassTypeName()
            val superClass: Option[DexClassType] = classDef.getSuperclassTypeId match {
                case -1 => None
                case superClassTypeId => Some(types(superClassTypeId).asInstanceOf[DexClassType])
            }
            val implements: Seq[DexClassType] = classDef.getInterfaceTypeIds match {
                case null => Seq()
                case implementTypes => implementTypes map { types(_).asInstanceOf[DexClassType] }
            }
            val sourceFile: Option[String] = noneIfNull(classDef.getSourceFileName())

            val (classAnnotations: Option[DexAnnotations], fieldAnnotationsMap: Map[Int, DexAnnotations], methodAnnotationsMap: Map[Int, DexAnnotations], paramAnnotationsMap: Map[Int, IndexedSeq[Option[DexAnnotations]]]) = classDef.getAnnotations() match {
                case null =>
                    (None, Map(), Map(), Map())
                case annotationsInfo =>
                    val classAnnotations = noneIfNull(annotationsInfo.getAnnotationsOnClass()) map { decodeAnnotations _ }
                    val fieldAnnotationsMap = annotationsInfo.getAnnotationsOnFields() map { fa =>
                        fa.field_idx() -> decodeAnnotations(fa.annotations())
                    } toMap
                    val methodAnnotationsMap = annotationsInfo.getAnnotationsOnMethods() map { ma =>
                        ma.method_idx() -> decodeAnnotations(ma.annotations())
                    } toMap
                    val paramAnnotationsMap = annotationsInfo.getAnnotationsOnParameters() map { pa =>
                        pa.method_idx() -> ((0 until pa.length()) map { i => noneIfNull(pa.get(i)) map { decodeAnnotations _ } })
                    } toMap

                    (classAnnotations, fieldAnnotationsMap, methodAnnotationsMap, paramAnnotationsMap)
            }

            // staticFieldId -> Value
            val staticValues: IndexedSeq[DexValue] = {
                val static_values_item: encoded_array_item = classDef.static_values()
                if (static_values_item != null) {
                    val static_values: encoded_array = static_values_item.value()
                    val values: IndexedSeq[encoded_value] = static_values.values().toIndexedSeq
                    check(values.length == static_values.size())
                    values map { decodeDexValue _ }
                } else {
                    IndexedSeq()
                }
            }

            val class_data = classDef.class_data()
            if (class_data != null) {
                check(class_data.static_fields().length == class_data.static_fields_size())
                check(class_data.instance_fields().length == class_data.static_fields_size())
                check(class_data.direct_methods().length == class_data.direct_methods_size())
                check(class_data.virtual_methods().length == class_data.virtual_methods_size())

                def decodeFields[T <: DexField](efields: Seq[encoded_field])(func: (Int, DexField0, encoded_field) => T): IndexedSeq[T] = {
                    val (_, result) = efields.zipWithIndex.foldLeft((0, Vector[T]())) { (m, efieldI) =>
                        val (efield, idx) = efieldI
                        val (_fieldId, cc) = m
                        val fieldId = _fieldId + efield.field_idx_diff()
                        val field = func(idx, fields(fieldId), efield)
                        (fieldId, cc :+ field)
                    }
                    result
                }

                // defaultValue, annotations만 하면 될듯

                check(fieldAnnotationsMap.keys forall { fieldId => fieldTable.get(fieldId).getClassIdx == classId })
                check(methodAnnotationsMap.keys forall { methodId => methodTable.get(methodId).getClassIdx == classId })
                check(paramAnnotationsMap forall { pa =>
                    val (methodId, paramAnnots) = pa
                    (methodTable.get(methodId).getClassIdx() == classId) &&
                        (methods(methodId).paramTypes.length == paramAnnots.length)
                })

                def extractEncodedField(fieldId: Int, efield: encoded_field): (Option[DexAnnotations], DexAccessFlags) =
                    (fieldAnnotationsMap get fieldId, DexAccessFlags(efield.access_flags()))

                val staticFields = decodeFields(class_data.static_fields()) { (idx, field0, efield) =>
                    val defaultValue = if (idx < staticValues.length) Some(staticValues(idx)) else None
                    val (annotations, accessFlags) = extractEncodedField(field0.fieldId, efield)
                    DexStaticField(field0.fieldId, field0.fieldName, field0.fieldType, defaultValue, annotations, accessFlags)
                }
                check(staticValues.length <= staticFields.length)
                // check if every staticFields and Values matches type

                val instanceFields = decodeFields(class_data.instance_fields()) { (idx, field0, efield) =>
                    val (annotations, accessFlags) = extractEncodedField(field0.fieldId, efield)
                    DexInstanceField(field0.fieldId, field0.fieldName, field0.fieldType, annotations, accessFlags)
                }

                val inheritedFields = {
                    val definedFieldIds = ((staticFields ++ instanceFields) map { _.fieldId }).toSet
                    ((fieldIdsByCls.getOrElse(classId, Set()) -- definedFieldIds) map { fieldId =>
                        val field0 = fields(fieldId)
                        val annotations = fieldAnnotationsMap get fieldId
                        DexInheritedField(field0.fieldId, field0.fieldName, field0.fieldType, annotations)
                    }).toSeq sortBy { _.fieldId }
                }

                def dexMethodsFrom[T <: DexMethod](emethods: Seq[encoded_method])(func: (DexMethod0, encoded_method) => T): IndexedSeq[T] = {
                    val (_, result) = emethods.foldLeft((0, Vector[T]())) { (m, emethod) =>
                        val (_methodId, cc) = m
                        val methodId = _methodId + emethod.method_idx_diff()
                        val method = func(methods(methodId), emethod)
                        (methodId, cc :+ method)
                    }
                    result
                }

                def extractMethod0(method0: DexMethod0) = {
                    val annotations: Option[DexAnnotations] = methodAnnotationsMap get method0.methodId
                    val parameters: Seq[DexParameter] = paramAnnotationsMap get method0.methodId match {
                        case Some(paramAnnots) =>
                            assert(method0.paramTypes.size == paramAnnots.size)
                            (method0.paramTypes zip paramAnnots) map { pa => DexParameter(pa._1, pa._2) }
                        case None =>
                            method0.paramTypes map { DexParameter(_, None) }
                    }
                    (annotations, parameters)
                }
                def extractEncodedMethod(emethod: encoded_method) = {
                    val accessFlags: DexAccessFlags = DexAccessFlags(emethod.access_flags())
                    val codeitem = noneIfNull(emethod.code_item()) map { DexCodeItem(_) }
                    (accessFlags, codeitem)
                }
                val directMethods = dexMethodsFrom(class_data.direct_methods()) { (method0, emethod) =>
                    val (annotations, parameters) = extractMethod0(method0)
                    val (accessFlags, codeitem) = extractEncodedMethod(emethod)
                    DexDirectMethod(method0.methodId, method0.methodName, parameters, method0.returnType, annotations, accessFlags, codeitem)
                }
                val virtualMethods = dexMethodsFrom(class_data.virtual_methods()) { (method0, emethod) =>
                    val (annotations, parameters) = extractMethod0(method0)
                    val (accessFlags, codeitem) = extractEncodedMethod(emethod)
                    DexVirtualMethod(method0.methodId, method0.methodName, parameters, method0.returnType, annotations, accessFlags, codeitem)
                }
                val inheritedMethods = {
                    val definedMethodIds = ((directMethods ++ virtualMethods) map { _.methodId }).toSet
                    ((methodIdsByCls.getOrElse(classId, Set()) -- definedMethodIds) map { methodId =>
                        val method0 = methods(methodId)
                        val (annotations, parameters) = extractMethod0(method0)
                        DexInheritedMethod(method0.methodId, method0.methodName, parameters, method0.returnType, annotations)
                    }).toSeq sortBy { _.methodId }
                }

                new DexDefinedClass(
                    classId, classTypeId, className,
                    accessFlags,
                    classAnnotations,
                    superClass,
                    implements,
                    inheritedFields,
                    staticFields,
                    instanceFields,
                    inheritedMethods,
                    directMethods,
                    virtualMethods)
            } else {
                check(fieldAnnotationsMap.isEmpty)
                check(methodAnnotationsMap.isEmpty)
                check(paramAnnotationsMap.isEmpty)
                check(staticValues.isEmpty)
                new DexMarkerClass(
                    classId, classTypeId, className,
                    accessFlags,
                    classAnnotations,
                    superClass,
                    implements)
            }
        }
    }

    lazy val classByType = (classes map { c => (types(c.typeId).asInstanceOf[DexClassType] -> c) }).toMap
    lazy val fieldByName = (fields map { f => (f.fieldName -> f) }).toMap
}
