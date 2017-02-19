package com.giyeok.dexdio.model2

import com.giyeok.dexdio.dexreader.ClassTable
import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo
import com.giyeok.dexdio.dexreader.DalvikExecutable
import com.giyeok.dexdio.dexreader.FieldTable
import com.giyeok.dexdio.dexreader.MethodTable
import com.giyeok.dexdio.dexreader.ProtoTable
import com.giyeok.dexdio.dexreader.StringTable
import com.giyeok.dexdio.dexreader.TypeTable
import com.giyeok.dexdio.dexreader.structs.encoded_annotation
import com.giyeok.dexdio.dexreader.structs.encoded_array
import com.giyeok.dexdio.dexreader.structs.encoded_array_item
import com.giyeok.dexdio.dexreader.structs.encoded_field
import com.giyeok.dexdio.dexreader.structs.encoded_method
import com.giyeok.dexdio.dexreader.structs.encoded_value

class DexProgram(dexes: IndexedSeq[DalvikExecutable]) {
    class DexFile(val dexId: Int, dex: DalvikExecutable) {
        val stringTable: StringTable = dex.getStringTable
        val typeTable: TypeTable = dex.getTypeTable
        val protoTable: ProtoTable = dex.getProtoTable
        val fieldTable: FieldTable = dex.getFieldTable
        val methodTable: MethodTable = dex.getMethodTable
        val classTable: ClassTable = dex.getClassTable
    }
    val dexFiles: IndexedSeq[DexFile] =
        dexes.zipWithIndex map { p => new DexFile(p._2, p._1) }

    private def check(pred: => Boolean): Unit = {
        // assert(pred)
    }

    val types: Map[Id, DexType] = {
        val typesMap: Map[Id, String] =
            (dexFiles flatMap { dex =>
                (0 until dex.typeTable.size()) map { i =>
                    Id(dex.dexId, i) -> dex.typeTable.getTypeName(i)
                }
            }).toMap
        // TODO multi dex 어플리케이션에서는 typesMap에 중복이 있을 수 있음
        check(typesMap.values.toSet.size == typesMap.values.size)
        val reverseTypesMap = typesMap map { kv => (kv._2, kv._1) }

        def dexTypeOf(typeName: String, typeId: Id): DexType =
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
                    val classDef = dexFiles(typeId.dexId).classTable.getClassDefByTypeId(typeId.itemId)
                    if (classDef != null) {
                        check(classDef.getClassTypeId == typeId.itemId)
                        check(dexFiles(typeId.dexId).typeTable.getTypeName(classDef.getClassTypeId) == typeName)
                        DexInternalClassType(typeId, lClassName)
                    } else {
                        DexExternalClassType(typeId, lClassName)
                    }
                case ('[', arrayType) =>
                    def elemType(elemTypeName: String): DexType =
                        reverseTypesMap get elemTypeName match {
                            case Some(elemTypeId) => dexTypeOf(elemTypeName, elemTypeId)
                            case None =>
                                elemTypeName.charAt(0) match {
                                    case '[' => DexArrayType(typeId, elemType(elemTypeName.substring(1)))
                                    case 'L' => DexUnspecifiedClassType(elemTypeName)
                                    case _ => throw InvalidDexTypeException(elemTypeName)
                                }
                        }
                    DexArrayType(typeId, elemType(arrayType.substring(1)))
                case _ => ???
            }
        (dexFiles flatMap { dex =>
            (0 until dex.typeTable.size()) map { i =>
                val id = Id(dex.dexId, i)
                id -> dexTypeOf(dex.typeTable.getTypeName(i), id)
            }
        }).toMap
    }

    val fields: IndexedSeq[DexField0] =
        dexFiles flatMap { dex =>
            (0 until dex.fieldTable.size()) map { fieldId =>
                val fieldDef = dex.fieldTable.get(fieldId)
                val fieldType = types(Id(dex.dexId, fieldDef.getTypeIdx))
                val fieldName = fieldDef.getName
                DexField0(Id(dex.dexId, fieldId), fieldName, fieldType)
            }
        }

    val methods: IndexedSeq[DexMethod0] =
        dexFiles flatMap { dex =>
            (0 until dex.methodTable.size()) map { methodId =>
                val methodDef = dex.methodTable.get(methodId)
                val methodName = methodDef.getName
                val methodProto = methodDef.getProto
                val paramTypes = (0 until methodProto.getParametersCount) map { i => types(Id(dex.dexId, methodProto.getParameter(i))) }
                val returnType = types(Id(dex.dexId, methodProto.getReturnTypeIdx))
                DexMethod0(Id(dex.dexId, methodId), methodName, paramTypes, returnType)
            }
        }

    private def decodeDexValue(dex: DexFile, v: encoded_value): DexValue = {
        import com.giyeok.dexdio.dexreader.value
        v.getValueType match {
            case encoded_value.VALUE_BYTE =>
                DexByteValue(v.getValue.asInstanceOf[value.Byte].getValue)
            case encoded_value.VALUE_SHORT =>
                DexShortValue(v.getValue.asInstanceOf[value.Short].getValue)
            case encoded_value.VALUE_CHAR =>
                DexCharValue(v.getValue.asInstanceOf[value.Short].getValue.toChar)
            case encoded_value.VALUE_INT =>
                DexIntValue(v.getValue.asInstanceOf[value.Int].getValue)
            case encoded_value.VALUE_LONG =>
                DexLongValue(v.getValue.asInstanceOf[value.Long].getValue)
            case encoded_value.VALUE_FLOAT =>
                DexFloatValue(v.getValue.asInstanceOf[value.Float].getValue)
            case encoded_value.VALUE_DOUBLE =>
                DexDoubleValue(v.getValue.asInstanceOf[value.Double].getValue)
            case encoded_value.VALUE_STRING =>
                DexStringValue(dex.stringTable.get(v.getValue.asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_TYPE =>
                DexTypeValue(types(Id(dex.dexId, v.getValue.asInstanceOf[value.Int].getValue)))
            case encoded_value.VALUE_FIELD =>
                DexFieldValue(fields(v.getValue.asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_METHOD =>
                DexMethodValue(methods(v.getValue.asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_ENUM =>
                DexEnumValue(fields(v.getValue.asInstanceOf[value.Int].getValue))
            case encoded_value.VALUE_ARRAY =>
                val elems = v.getValue.asInstanceOf[encoded_array].values()
                DexArrayValue((0 until elems.length) map { i => decodeDexValue(dex, elems(i)) })
            case encoded_value.VALUE_ANNOTATION =>
                val annots = decodeEncodedAnnotation(dex, v.getValue.asInstanceOf[encoded_annotation])
                DexAnnotationValue(annots)
            case encoded_value.VALUE_NULL =>
                DexNullValue
            case encoded_value.VALUE_BOOLEAN =>
                DexBooleanValue(v.getValue.asInstanceOf[value.Byte].getValue == 1)
        }
    }

    private def decodeEncodedAnnotation(dex: DexFile, annot: encoded_annotation) =
        (annot.elements() map { e =>
            dex.stringTable.get(e.name_idx()) -> decodeDexValue(dex, e.value())
        }).toSeq

    private def decodeAnnotations(dex: DexFile, annotations: AnnotationsInfo#Annotations): DexAnnotations = {
        DexAnnotations(annotations.annotations() map { annot =>
            DexAnnotationItem(annot.visibility(), decodeEncodedAnnotation(dex, annot.annotation()))
        })
    }

    private implicit val ord = Id

    val classes: Map[Id, DexClass] = (dexFiles flatMap { dex =>
        val fieldIdsByCls = (0 until dex.fieldTable.size()) groupBy { fieldId =>
            dex.fieldTable.get(fieldId).getClassIdx
        } mapValues { _.toSet }

        val methodIdsByCls = (0 until dex.methodTable.size()) groupBy { methodId =>
            dex.methodTable.get(methodId).getClassIdx
        } mapValues { _.toSet }

        // TODO 클래스가 아닌 타입을 가리키고 있으면 문제

        (0 until dex.classTable.size()) map { classId =>
            val classDef = dex.classTable.getClassByClassId(classId)
            val classTypeId = classDef.getClassTypeId
            check(classDef.getClassTypeName.length() > 0 && classDef.getClassTypeName.charAt(0) == 'L')
            val accessFlags = DexAccessFlags(classDef.getAccessFlags)
            val className = classDef.getClassTypeName
            val superClass: Option[DexClassType] = classDef.getSuperclassTypeId match {
                case -1 => None
                case superClassTypeId => Some(types(Id(dex.dexId, superClassTypeId)).asInstanceOf[DexClassType])
            }
            val implements: Seq[DexClassType] = classDef.getInterfaceTypeIds match {
                case null => Seq()
                case implementTypes => implementTypes map { itemId => types(Id(dex.dexId, itemId)).asInstanceOf[DexClassType] }
            }
            val sourceFile: Option[String] = Option(classDef.getSourceFileName)

            val (classAnnotations: Option[DexAnnotations], fieldAnnotationsMap: Map[Int, DexAnnotations], methodAnnotationsMap: Map[Int, DexAnnotations], paramAnnotationsMap: Map[Int, IndexedSeq[Option[DexAnnotations]]]) = classDef.getAnnotations match {
                case null =>
                    (None, Map(), Map(), Map())
                case annotationsInfo =>
                    val classAnnotations = Option(annotationsInfo.getAnnotationsOnClass) map { decodeAnnotations(dex, _) }
                    val fieldAnnotationsMap = (annotationsInfo.getAnnotationsOnFields map { fa =>
                        fa.field_idx() -> decodeAnnotations(dex, fa.annotations())
                    }).toMap
                    val methodAnnotationsMap = (annotationsInfo.getAnnotationsOnMethods map { ma =>
                        ma.method_idx() -> decodeAnnotations(dex, ma.annotations())
                    }).toMap
                    val paramAnnotationsMap = (annotationsInfo.getAnnotationsOnParameters map { pa =>
                        pa.method_idx() -> ((0 until pa.length()) map { i => Option(pa.get(i)) map { decodeAnnotations(dex, _) } })
                    }).toMap

                    (classAnnotations, fieldAnnotationsMap, methodAnnotationsMap, paramAnnotationsMap)
            }

            // staticFieldId -> Value
            val staticValues: IndexedSeq[DexValue] = {
                val static_values_item: encoded_array_item = classDef.static_values()
                if (static_values_item != null) {
                    val static_values: encoded_array = static_values_item.value()
                    val values: IndexedSeq[encoded_value] = static_values.values().toIndexedSeq
                    check(values.length == static_values.size())
                    values map { decodeDexValue(dex, _) }
                } else {
                    IndexedSeq()
                }
            }

            val class_data = classDef.class_data()
            if (class_data != null) {
                check(class_data.static_fields().length == class_data.static_fields_size())
                check(class_data.instance_fields().length == class_data.instance_fields_size())
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

                check(fieldAnnotationsMap.keys forall { fieldId => dex.fieldTable.get(fieldId).getClassIdx == classId })
                check(methodAnnotationsMap.keys forall { methodId => dex.methodTable.get(methodId).getClassIdx == classId })
                check(paramAnnotationsMap forall { pa =>
                    val (methodId, paramAnnots) = pa
                    (dex.methodTable.get(methodId).getClassIdx == classId) &&
                        (methods(methodId).paramTypes.length == paramAnnots.length)
                })

                def extractEncodedField(fieldId: Int, efield: encoded_field): (Option[DexAnnotations], DexAccessFlags) =
                    (fieldAnnotationsMap get fieldId, DexAccessFlags(efield.access_flags()))

                val staticFields = decodeFields(class_data.static_fields()) { (idx, field0, efield) =>
                    val defaultValue = if (idx < staticValues.length) Some(staticValues(idx)) else None
                    val (annotations, accessFlags) = extractEncodedField(field0.fieldId.itemId, efield)
                    DexStaticField(field0.fieldId, field0.fieldName, field0.fieldType, defaultValue, annotations, accessFlags)
                }
                check(staticValues.length <= staticFields.length)
                // check if every staticFields and Values matches type

                val instanceFields = decodeFields(class_data.instance_fields()) { (idx, field0, efield) =>
                    val (annotations, accessFlags) = extractEncodedField(field0.fieldId.itemId, efield)
                    DexInstanceField(field0.fieldId, field0.fieldName, field0.fieldType, annotations, accessFlags)
                }

                val inheritedFields = {
                    val definedFieldIds = ((staticFields ++ instanceFields) map { _.fieldId.itemId }).toSet
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
                    val annotations: Option[DexAnnotations] = methodAnnotationsMap get method0.methodId.itemId
                    val parameters: Seq[DexParameter] = paramAnnotationsMap get method0.methodId.itemId match {
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
                    val codeitem = Option(emethod.code_item()) map DexCodeItem
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
                    val definedMethodIds = ((directMethods ++ virtualMethods) map { _.methodId.itemId }).toSet
                    ((methodIdsByCls.getOrElse(classId, Set()) -- definedMethodIds) map { methodId =>
                        val method0 = methods(methodId)
                        val (annotations, parameters) = extractMethod0(method0)
                        DexInheritedMethod(method0.methodId, method0.methodName, parameters, method0.returnType, annotations)
                    }).toSeq sortBy { _.methodId }
                }

                new DexDefinedClass(
                    Id(dex.dexId, classId), Id(dex.dexId, classTypeId), className,
                    accessFlags,
                    classAnnotations,
                    superClass,
                    implements,
                    sourceFile,
                    inheritedFields,
                    staticFields,
                    instanceFields,
                    inheritedMethods,
                    directMethods,
                    virtualMethods
                )
            } else {
                check(fieldAnnotationsMap.isEmpty)
                check(methodAnnotationsMap.isEmpty)
                check(paramAnnotationsMap.isEmpty)
                check(staticValues.isEmpty)
                new DexMarkerClass(
                    Id(dex.dexId, classId), Id(dex.dexId, classTypeId), className,
                    accessFlags,
                    classAnnotations,
                    superClass,
                    implements,
                    sourceFile
                )
            }
        }
    } map { c => c.classId -> c }).toMap

    lazy val classByType: Map[DexClassType, DexClass] = (classes map { c => types(c._1).asInstanceOf[DexClassType] -> c._2 })
    lazy val fieldByName: Map[String, DexField0] = (fields map { f => f.fieldName -> f }).toMap
}
