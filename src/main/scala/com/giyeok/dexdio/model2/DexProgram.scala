package com.giyeok.dexdio.model2

import com.giyeok.dexdio.dexreader.DalvikExecutable
import com.giyeok.dexdio.dexreader.structs.encoded_field
import com.giyeok.dexdio.dexreader.structs.encoded_method
import com.giyeok.dexdio.dexreader.ClassTable.AnnotationsInfo

class DexProgram(dex: DalvikExecutable) {
    val stringTable = dex.getStringTable()
    val typeTable = dex.getTypeTable()
    val protoTable = dex.getProtoTable()
    val fieldTable = dex.getFieldTable()
    val methodTable = dex.getMethodTable()
    val classTable = dex.getClassTable()

    private def check(pred: => Boolean): Unit = {}

    val types = {
        val typesMap = ((0 until typeTable.size()) map { i =>
            i -> typeTable.getTypeName(i)
        }).toMap
        check(typesMap.values.toSeq.distinct == typesMap.values.toSeq)
        val reverseTypesMap = typesMap map { kv => kv._2 -> kv._1 }

        var memo = Map[Int, DexType]()
        def dexTypeOf(typeName: String, typeId: Int): DexType =
            memo get typeId match {
                case Some(memo) => memo
                case None =>
                    val typ = (typeName.charAt(0), typeName) match {
                        case ('V', "V") => DexVoid(typeId)
                        case ('Z', "Z") => DexBoolean(typeId)
                        case ('B', "B") => DexByte(typeId)
                        case ('S', "S") => DexShort(typeId)
                        case ('C', "C") => DexChar(typeId)
                        case ('I', "I") => DexInt(typeId)
                        case ('J', "J") => DexLong(typeId)
                        case ('F', "F") => DexFloat(typeId)
                        case ('D', "D") => DexDouble(typeId)
                        case ('L', lClassName) =>
                            val className = lClassName.substring(1)
                            val classDef = classTable.getClassDefByTypeId(typeId)
                            if (classDef != null) {
                                check(classDef.getClassTypeId() == typeId)
                                check(typeTable.getTypeName(classDef.getClassTypeId) == typeName)
                                DexInternalClassType(typeId, className)
                            } else {
                                DexExternalClassType(typeId, className)
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
                    }
                    memo += (typeId -> typ)
                    typ
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
            val class_data = classDef.class_data()
            if (class_data != null) {
                check(class_data.static_fields().length == class_data.static_fields_size())
                check(class_data.instance_fields().length == class_data.static_fields_size())
                check(class_data.direct_methods().length == class_data.direct_methods_size())
                check(class_data.virtual_methods().length == class_data.virtual_methods_size())

                def decodeFields[T <: DexField](efields: Seq[encoded_field])(func: (DexField0, encoded_field) => T): IndexedSeq[T] = {
                    val (_, result) = efields.foldLeft((0, Vector[T]())) { (m, efield) =>
                        val (_fieldId, cc) = m
                        val fieldId = _fieldId + efield.field_idx_diff()
                        val field = func(fields(fieldId), efield)
                        (fieldId, cc :+ field)
                    }
                    result
                }

                // defaultValue, annotations만 하면 될듯

                def decodeAnnotations(annotations: AnnotationsInfo#Annotations): DexAnnotations =
                    ???

                val annotationsInfo = classDef.getAnnotations()
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

                check(fieldAnnotationsMap.keys forall { fieldId => fieldTable.get(fieldId).getClassIdx == classId })
                check(methodAnnotationsMap.keys forall { methodId => methodTable.get(methodId).getClassIdx == classId })
                check(paramAnnotationsMap forall { pa =>
                    val (methodId, paramAnnots) = pa
                    (methodTable.get(methodId).getClassIdx() == classId) &&
                        (methods(methodId).paramTypes.length == paramAnnots.length)
                })

                // codeitem하고..

                def extractEncodedField(fieldId: Int, efield: encoded_field): (Option[DexAnnotations], DexAccessFlags) =
                    (fieldAnnotationsMap get fieldId, DexAccessFlags(efield.access_flags()))
                val staticFields = decodeFields(class_data.static_fields()) { (field0, efield) =>
                    val defaultValue = ???
                    val (annotations, accessFlags) = extractEncodedField(field0.fieldId, efield)
                    DexStaticField(field0.fieldId, field0.fieldName, field0.fieldType, defaultValue, annotations, accessFlags)
                }
                val instanceFields = decodeFields(class_data.instance_fields()) { (field0, efield) =>
                    val (annotations, accessFlags) = extractEncodedField(field0.fieldId, efield)
                    DexInstanceField(field0.fieldId, field0.fieldName, field0.fieldType, annotations, accessFlags)
                }
                val inheritedFields = ((fieldIdsByCls(classId) -- ((staticFields ++ instanceFields) map { _.fieldId })) map { fieldId =>
                    val field0 = fields(fieldId)
                    val annotations = fieldAnnotationsMap get fieldId
                    DexInheritedField(field0.fieldId, field0.fieldName, field0.fieldType, annotations)
                }).toSeq sortBy { _.fieldId }

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
                    val codeitem = noneIfNull(emethod.code_item()) map { _ => ??? }
                    (accessFlags, codeitem)
                }
                val directMethods = dexMethodsFrom(class_data.direct_methods()) { (method0, emethod) =>
                    val (annotations, parameters) = extractMethod0(method0)
                    val (accessFlags, codeitem) = extractEncodedMethod(emethod)
                    DexDirectMethod(method0.methodId, method0.methodName, parameters, method0.returnType, annotations, accessFlags, codeitem)
                }
                val virtualMethods = dexMethodsFrom(class_data.virtual_methods()) { (method0, emethod) =>
                    val (annotations, accessFlags, parameters, codeitem) = extractEncodedMethod(method0, emethod)
                    DexVirtualMethod(method0.methodId, method0.methodName, parameters, method0.returnType, annotations, accessFlags, codeitem)
                }
                val inheritedMethods = (methodIdsByCls(classId) -- ((directMethods ++ virtualMethods) map { _.methodId })) map { methodId =>
                    val method0 = methods(methodId)
                    val (annotations, parameters) = extractMethod0(method0)
                    DexInheritedMethod(method0.methodId, method0.methodName, parameters, method0.returnType, annotations)
                }
            }
        }
    }
}
