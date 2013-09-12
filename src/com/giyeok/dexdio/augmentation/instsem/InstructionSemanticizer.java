package com.giyeok.dexdio.augmentation.instsem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.giyeok.dexdio.augmentation.Augmentation;
import com.giyeok.dexdio.augmentation.instsem.InstSemBinaryOp.BinaryOperator;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemElement;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemLHS;
import com.giyeok.dexdio.augmentation.instsem.InstructionSemantic.InstSemOperand;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexCodeItem.DexRegister;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexParameter;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstArrayLength;
import com.giyeok.dexdio.model.insns.DexInstArrayOp;
import com.giyeok.dexdio.model.insns.DexInstBinaryOp;
import com.giyeok.dexdio.model.insns.DexInstBinaryOpLit;
import com.giyeok.dexdio.model.insns.DexInstCheckCast;
import com.giyeok.dexdio.model.insns.DexInstCompare;
import com.giyeok.dexdio.model.insns.DexInstFillArrayData;
import com.giyeok.dexdio.model.insns.DexInstFilledNewArray;
import com.giyeok.dexdio.model.insns.DexInstGoto;
import com.giyeok.dexdio.model.insns.DexInstIf;
import com.giyeok.dexdio.model.insns.DexInstIfZero;
import com.giyeok.dexdio.model.insns.DexInstInstanceOf;
import com.giyeok.dexdio.model.insns.DexInstInstanceOp;
import com.giyeok.dexdio.model.insns.DexInstInvoke;
import com.giyeok.dexdio.model.insns.DexInstMonitor;
import com.giyeok.dexdio.model.insns.DexInstMove;
import com.giyeok.dexdio.model.insns.DexInstMoveConst;
import com.giyeok.dexdio.model.insns.DexInstMoveException;
import com.giyeok.dexdio.model.insns.DexInstMoveResult;
import com.giyeok.dexdio.model.insns.DexInstNewArray;
import com.giyeok.dexdio.model.insns.DexInstNewInstance;
import com.giyeok.dexdio.model.insns.DexInstNop;
import com.giyeok.dexdio.model.insns.DexInstReturn;
import com.giyeok.dexdio.model.insns.DexInstStaticOp;
import com.giyeok.dexdio.model.insns.DexInstSwitch;
import com.giyeok.dexdio.model.insns.DexInstThrow;
import com.giyeok.dexdio.model.insns.DexInstUnaryOp;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.model.insns.Operand;
import com.giyeok.dexdio.model.insns.OperandConstantPool;
import com.giyeok.dexdio.model.insns.OperandIntegerConstant;
import com.giyeok.dexdio.model.insns.DexInstInvoke.InvokeType;
import com.giyeok.dexdio.util.ArraysUtil;

public class InstructionSemanticizer extends Augmentation {

	public static InstructionSemanticizer get(DexProgram program) {
		return (InstructionSemanticizer) Augmentation.getAugmentation(InstructionSemanticizer.class, program);
	}
	
	@Override
	public String getAugmentationName() {
		return "Instruction semanticizer";
	}
	
	public InstructionSemanticizer(DexProgram program) {
		super(program);
		
		visit(program);
	}
	
	private Map<DexCodeItem, MethodSemantics> instsems;
	
	public InstSemStatement[] getSemantics(DexCodeItem codeitem) {
		return instsems.get(codeitem).getStatements();
	}
	
	public MethodSemantics getMethodSemantics(DexCodeItem codeitem) {
		return instsems.get(codeitem);
	}
	
	private void visit(DexProgram program) {
		instsems = new HashMap<DexCodeItem, MethodSemantics>();
		
		visitMethods(program, new MethodVisitor() {
			
			@Override
			public void visit(DexProgram program, DexMethod method) {
				DexCodeItem codeitem = method.getCodeItem();
				
				if (codeitem != null) {
					instsems.put(codeitem, new MethodSemantics(
							codeitem,
							codeitem.getInstanceRegister(), 
							method.getParameters(), 
							InstructionSemanticizer.this.visit(codeitem)));
				}
			}
		});
	}
	
	public static class MethodSemantics {
		private InstSemParameter instance;
		private InstSemParameter parameters[];
		private InstSemStatement sems[];
		
		public MethodSemantics(DexCodeItem codeitem, DexRegister instance, DexParameter parameters[], InstSemStatement sems[]) {
			if (instance != null) {
				this.instance = new InstSemParameter(codeitem, instance, true);
				this.instance.setOperandTypeData(codeitem.getBelongedMethod().getBelongedType());
			} else {
				this.instance = null;
			}
			this.parameters = new InstSemParameter[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				this.parameters[i] = new InstSemParameter(codeitem, parameters[i]);
				this.parameters[i].setOperandTypeData(parameters[i].getType());
			}
			this.sems = sems;
		}

		public InstSemStatement[] getStatements() {
			return sems;
		}
		
		private InstSemParameter parametersWithInstance[];
		public InstSemParameter[] getParametersIncludingInstanceRegister() {
			if (instance == null) {
				return parameters;
			} else {
				if (parametersWithInstance == null) {
					parametersWithInstance = Arrays.copyOf(parameters, parameters.length + 1);
					parametersWithInstance[parameters.length] = instance;
				}
				return parametersWithInstance;
			}
		}
		
		public InstSemParameter[] getParameters() {
			return parameters;
		}
		
		public InstSemParameter getParameterForRegister(DexRegister register) {
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i].getRegister() == register) {
					return parameters[i];
				}
			}
			if (instance.getRegister() == register) {
				return instance;
			}
			return null;
		}
		
		/**
		 * returns the instruction semantic statement belonged to instruction
		 * it will return null if there is no statement of the instruction
		 * @param instruction
		 * @return
		 */
		public InstSemStatement findStatmentByInstruction(DexInstruction instruction) {
			int index = getIndexOfStatementByInstruction(instruction);
			
			if (index < 0) {
				return null;
			} else {
				return sems[index];
			}
		}
		
		/**
		 * returns the index of instruction semantic which belongs to the given instruction
		 * it will return -1 if there is no statement of the instruction
		 * @param instruction
		 * @return
		 */
		public int getIndexOfStatementByInstruction(DexInstruction instruction) {
			// TODO may need improvement
			for (int i = 0; i < sems.length; i++) {
				if (sems[i].getInstruction() == instruction) {
					return i;
				}
			}
			return -1;
		}
		
		/**
		 * returns the index of the given instruction semantic statmenet
		 * it will return -1 if there is no statement of the instruction
		 * @param statement
		 * @return
		 */
		public int getIndexOf(InstSemStatement statement) {
			return ArraysUtil.indexOfExact(sems, statement);
		}
	}
	
	private InstSemStatement[] visit(DexCodeItem codeitem) {
		ArrayList<InstSemStatement> list;
		DexInstruction[] instructions = codeitem.getInstructionsArray();
		
		list = new ArrayList<InstSemStatement>();
		for (int i = 0; i < instructions.length; i++) {
			InstSemStatement is;
			
			is = visit(codeitem, instructions[i]);
			if (is != null) {
				is.setInstruction(instructions[i]);
				list.add(is);
			}
		}
		return list.toArray(new InstSemStatement[0]);
	}
	
	private InstSemRegister generateRegisterOrPair(DexCodeItem codeitem, DexRegister register, boolean isWide) {
		if (! isWide) {
			return new InstSemRegister(codeitem, register);
		} else {
			return new InstSemRegisterPair(codeitem, register);
		}
	}

	private InstSemStatement visit(DexCodeItem codeitem, DexInstruction inst) {
		DexProgram program = inst.getProgram();
		switch (inst.getInstructionType()) {
		case MOVE_CONST: {
			InstSemLHS dst = generateRegisterOrPair(codeitem, ((DexInstMoveConst) inst).getDestinationRegister(), ((DexInstMoveConst) inst).isWide());
			Operand source = ((DexInstMoveConst) inst).getSource();
			InstSemConst val = null;
			
			if (source instanceof OperandIntegerConstant) {
				val = new InstSemConst(codeitem, (OperandIntegerConstant) source);
			} else if (source instanceof OperandConstantPool) {
				val = new InstSemConst(codeitem, (OperandConstantPool) source);
				switch (((OperandConstantPool) source).getConstantKind()) {
				case STRING:
					dst.setOperandTypeData(program.getStringClassType());
					val.setOperandTypeData(program.getStringClassType());
					break;
				}
			} else {
				assert false;
			}
			
			return new InstSemAssignment(codeitem, dst, val);
		}
		case MOVE: {
			InstSemRegister dst = generateRegisterOrPair(codeitem, ((DexInstMove) inst).getDestinationRegister(), ((DexInstMove) inst).isWide());
			InstSemRegister src = generateRegisterOrPair(codeitem, ((DexInstMove) inst).getSourceRegister(), ((DexInstMove) inst).isWide());
			
			dst.setOperandTypeData(src.getOperandTypeData());
			
			return new InstSemAssignment(codeitem, dst, src);
		}
		case ARRAY_LENGTH: {
			InstSemRegister dst = generateRegisterOrPair(codeitem, ((DexInstArrayLength) inst).getDestinationRegister(), false);
			InstSemRegister array = generateRegisterOrPair(codeitem, ((DexInstArrayLength) inst).getSourceArrayRegister(), false);
			
			dst.setOperandTypeData(program.getIntegerPrimitiveType());
			
			return new InstSemAssignment(codeitem, dst, new InstSemInstanceField(codeitem, array, "length"));
		}
		case ARRAY_OP: {
			DexInstArrayOp arrayop = (DexInstArrayOp) inst;
			InstSemRegister target = generateRegisterOrPair(codeitem, arrayop.getSourceOrDestinationRegister(), arrayop.isWide());
			InstSemRegister array = generateRegisterOrPair(codeitem, arrayop.getArrayRegister(), false);
			InstSemRegister index = generateRegisterOrPair(codeitem, arrayop.getIndexRegister(), false);
			InstSemArrayReference arrayref = new InstSemArrayReference(codeitem, array, index);
			
			index.setOperandTypeData(program.getIntegerPrimitiveType());
			// target.setOperandTypeData(type)
			
			if (arrayop.isGetOperation()) {
				return new InstSemAssignment(codeitem, target, arrayref);
			} else {
				return new InstSemAssignment(codeitem, arrayref, target);
			}
		}
		case BINARY_OP: {
			DexInstBinaryOp binop = (DexInstBinaryOp) inst;
			boolean isWide = binop.isWide();
			
			InstSemRegister dst = generateRegisterOrPair(codeitem, binop.getDestinationRegister(), isWide);
			InstSemRegister op1 = generateRegisterOrPair(codeitem, binop.getFirstOperandRegister(), isWide);
			InstSemRegister op2 = generateRegisterOrPair(codeitem, binop.getSecondOperandRegister(), isWide);
			InstSemBinaryOp bop = new InstSemBinaryOp(codeitem, op1, inst.getOpcode(), op2);
			
			dst.setOperandTypeData(binop.getOperandsType());
			bop.setOperandTypeData(binop.getOperandsType());
			op1.setOperandTypeData(binop.getOperandsType());
			op2.setOperandTypeData(binop.getOperandsType());
			
			return new InstSemAssignment(codeitem, dst, bop);
		}
		case BINARY_OP_LIT: {
			InstSemRegister dst = generateRegisterOrPair(codeitem, ((DexInstBinaryOpLit) inst).getDestinationRegister(), ((DexInstBinaryOpLit) inst).isWide());
			InstSemRegister op1 = generateRegisterOrPair(codeitem, ((DexInstBinaryOpLit) inst).getFirstOperandRegister(), ((DexInstBinaryOpLit) inst).isWide());
			InstSemConst op2 = new InstSemConst(codeitem, ((DexInstBinaryOpLit) inst).getSecondOperand());
			
			dst.setOperandTypeData(((DexInstBinaryOpLit) inst).getOperandsType());
			op1.setOperandTypeData(((DexInstBinaryOpLit) inst).getOperandsType());
			op2.setOperandTypeData(((DexInstBinaryOpLit) inst).getOperandsType());
			
			return new InstSemAssignment(codeitem, dst, new InstSemBinaryOp(codeitem, op1, inst.getOpcode(), op2));
		}
		case CHECK_CAST: {
			InstSemRegister target = generateRegisterOrPair(codeitem, ((DexInstCheckCast) inst).getInstanceRegister(), false);
			InstSemConst type = new InstSemConst(codeitem, ((DexInstCheckCast) inst).getType());
			
			// TODO operand type data
			
			return new InstSemEtcStatement(codeitem, "check-cast", new InstSemElement[] { target, type });
		}
		case COMPARE: {
			DexInstCompare compare = (DexInstCompare) inst;
			InstSemRegister dst = generateRegisterOrPair(codeitem, compare.getDestinationRegister(), false);
			InstSemRegister op1 = generateRegisterOrPair(codeitem, compare.getFirstOperandRegister(), compare.isWide());
			InstSemRegister op2 = generateRegisterOrPair(codeitem, compare.getSecondOperandRegister(), compare.isWide());
			
			// dst.setOperandTypeData
			op1.setOperandTypeData(compare.getOperandsType());
			op2.setOperandTypeData(compare.getOperandsType());
			
			return new InstSemAssignment(codeitem, dst, new InstSemBinaryOp(codeitem, op1, inst.getOpcode(), op2));
		}
		case FILL_ARRAY_DATA: {
			InstSemRegister array = generateRegisterOrPair(codeitem, ((DexInstFillArrayData) inst).getArrayRegister(), false);
			InstSemConst data = new InstSemConst(codeitem, ((DexInstFillArrayData) inst).getPayloadAddress());
			
			return new InstSemEtcStatement(codeitem, "fill-array-data", new InstSemElement[] { array, data });
		}
		case FILLED_NEW_ARRAY: {
			DexInstFilledNewArray filledarray = (DexInstFilledNewArray) inst;
			DexInstruction followers[] = inst.getPossibleNextInstructions();
			DexType arraytype = filledarray.getType();
			
			if (followers.length == 1 && followers[0] instanceof DexInstMoveResult) {
				DexInstMoveResult move = (DexInstMoveResult) followers[0];
				InstSemRegister dst = generateRegisterOrPair(codeitem, move.getDestinationRegister(), move.isWide());
				
				dst.setOperandTypeData(arraytype);
				return new InstSemAssignment(codeitem, dst, new InstSemNewArray(codeitem, arraytype, InstSemRegister.genFromDexRegisters(codeitem, filledarray.getArguments(), 0)));
			} else {
				// this should not be happened - filled-new-array instruction ignored!
				return null;
			}
		}
		case GOTO: {
			InstSemConst target = new InstSemConst(codeitem, ((DexInstGoto) inst).getBranchTarget());
			
			return new InstSemEtcStatement(codeitem, "goto", new InstSemElement[] { target });
		}
		case IF: {
			DexInstIf branch = (DexInstIf) inst;
			
			return new InstSemEtcStatement(codeitem, inst.getOpcodeMnemonic(), new InstSemElement[] { 
				generateRegisterOrPair(codeitem, branch.getFirstOperandRegister(), false),
				generateRegisterOrPair(codeitem, branch.getSecondOperandRegister(), false),
				new InstSemConst(codeitem, branch.getBranchTarget())
			});
		}
		case IF_ZERO: {
			DexInstIfZero brz = (DexInstIfZero) inst;
			
			return new InstSemEtcStatement(codeitem, inst.getOpcodeMnemonic(), new InstSemElement[] { 
				generateRegisterOrPair(codeitem, brz.getOperandRegister(), false),
				new InstSemConst(codeitem, brz.getBranchTarget())
			});
		}
		case INSTANCE_OF: {
			DexInstInstanceOf iof = (DexInstInstanceOf) inst;
			InstSemRegister dst = generateRegisterOrPair(codeitem, iof.getDestinationRegister(), false);
			InstSemRegister instance = generateRegisterOrPair(codeitem, iof.getInstanceRegister(), false);
			InstSemConst type = new InstSemConst(codeitem, iof.getType());
			
			dst.setOperandTypeData(program.getBooleanPrimitiveType());
			// TODO instance도 타입이 필요한지 고민해볼 것
			
			return new InstSemAssignment(codeitem, dst, new InstSemBinaryOp(codeitem, instance, BinaryOperator.INSTANCE_OF, type));
		}
		case INSTANCE_OP: {
			DexInstInstanceOp iop = (DexInstInstanceOp) inst;
			
			InstSemRegister target = generateRegisterOrPair(codeitem, iop.getSourceOrDestinationRegister(), iop.isWide());
			InstSemRegister instance = generateRegisterOrPair(codeitem, iop.getInstanceRegister(), false);
			DexField field = iop.getField();
			InstSemInstanceField ifield = new InstSemInstanceField(codeitem, instance, field);
			
			instance.setOperandTypeData(field.getBelongedClass());
			target.setOperandTypeData(field.getType());
			ifield.setOperandTypeData(field.getType());
			
			if (iop.isGetOperation()) {
				return new InstSemAssignment(codeitem, target, ifield);
			} else {
				return new InstSemAssignment(codeitem, ifield, target);
			}
		}
		case INVOKE: {
			DexInstInvoke invoke = (DexInstInvoke) inst;
			DexMethod invokingMethod = invoke.getInvokingMethod();
			DexInstruction followers[] = inst.getPossibleNextInstructions();
			InstSemRegister dest = null;
			InstSemOperand rhs;
			
			if (followers.length == 1 && followers[0] instanceof DexInstMoveResult) {
				DexInstMoveResult move = (DexInstMoveResult) followers[0];
				dest = generateRegisterOrPair(codeitem, move.getDestinationRegister(), move.isWide());
				dest.setOperandTypeData(invokingMethod.getReturnType());
			}
			// System.out.println(invoke.getCodeItem().getBelongedMethod().getBelongedType().getTypeFullNameBeauty() + "." + invoke.getCodeItem().getBelongedMethod().getName() + " " + Integer.toHexString(invoke.getAddress()));
			if (invoke.getInvokeType() == InvokeType.STATIC) {
				InstSemStaticMethodInvoke sinvoke;
				
				sinvoke = new InstSemStaticMethodInvoke(codeitem, invokingMethod, 
						InstSemRegister.genFromDexRegisters(codeitem, invoke.getArgumentRegisters(), invokingMethod.getParametersType(), 0));
				sinvoke.setOperandTypeData(invokingMethod.getReturnType());
				if (dest == null) {
					return sinvoke;
				} else {
					rhs = sinvoke;
				}
			} else {
				InstSemInstanceMethodInvoke iinvoke;
				DexRegister args[] = invoke.getArgumentRegisters();
				InstSemRegister instance = generateRegisterOrPair(codeitem, args[0], false);
				
				instance.setOperandTypeData(invokingMethod.getBelongedType());
				
				iinvoke = new InstSemInstanceMethodInvoke(codeitem, instance, invokingMethod, 
						InstSemRegister.genFromDexRegisters(codeitem, args, invokingMethod.getParametersType(), 1));
				iinvoke.setOperandTypeData(invokingMethod.getReturnType());
				if (dest == null) {
					return iinvoke;
				} else {
					rhs = iinvoke;
				}
			}
			return new InstSemAssignment(codeitem, dest, rhs);
		}
		case MONITOR: {
			return new InstSemEtcStatement(codeitem, inst.getOpcodeMnemonic(), new InstSemElement[] { generateRegisterOrPair(codeitem, ((DexInstMonitor) inst).getTargetRegister(), false) });
		}
		case MOVE_EXCEPTION: {
			InstSemRegister dst = generateRegisterOrPair(codeitem, ((DexInstMoveException) inst).getTargetRegister(), false);
			
			// TODO dst.setOperandType(something)
			
			return new InstSemAssignment(codeitem, dst, new InstSemEtcOperand(codeitem, inst.getOpcodeMnemonic()));
		}
		case MOVE_RESULT: {
			return null;
		}
		case NEW_ARRAY: {
			DexInstNewArray newarray = (DexInstNewArray) inst;
			
			InstSemRegister dst = generateRegisterOrPair(codeitem, newarray.getDestinationRegister(), false);
			InstSemRegister size = generateRegisterOrPair(codeitem, newarray.getSizeRegister(), false);
			
			dst.setOperandTypeData(newarray.getType());
			size.setOperandTypeData(program.getIntegerPrimitiveType());
			
			return new InstSemAssignment(codeitem, dst, new InstSemNewArray(codeitem, newarray.getType(), size));
		}
		case NEW_INSTANCE: {
			DexInstNewInstance newinstance = (DexInstNewInstance) inst;
			
			InstSemRegister dst = generateRegisterOrPair(codeitem, newinstance.getDestinationRegister(), false);
			InstSemNewInstance newop = new InstSemNewInstance(codeitem, newinstance.getType());
			
			dst.setOperandTypeData(newinstance.getType());
			newop.setOperandTypeData(newinstance.getType());
			
			return new InstSemAssignment(codeitem, dst, newop);
		}
		case RETURN: {
			DexRegister register = ((DexInstReturn) inst).getValueRegister();
			
			if (register == null) {
				return new InstSemEtcStatement(codeitem, "return", new InstSemElement[0]);
			} else {
				InstSemRegister returning = generateRegisterOrPair(codeitem, register, ((DexInstReturn) inst).isWide());
				returning.setOperandTypeData(inst.getCodeItem().getBelongedMethod().getReturnType());
				return new InstSemEtcStatement(codeitem, "return", new InstSemElement[] { returning });
			}
		}
		case STATIC_OP: {
			DexInstStaticOp sop = (DexInstStaticOp) inst;
			
			InstSemRegister target = generateRegisterOrPair(codeitem, sop.getSourceOrDestinationRegister(), sop.isWide());
			InstSemStaticField field = new InstSemStaticField(codeitem, sop.getField());
			DexType type = sop.getField().getType();
			
			target.setOperandTypeData(type);
			field.setOperandTypeData(type);
			
			if (sop.isGetOperation()) {
				return new InstSemAssignment(codeitem, target, field);
			} else {
				return new InstSemAssignment(codeitem, field, target);
			}
		}
		case SWITCH: {
			return new InstSemEtcStatement(codeitem, "switch", new InstSemElement[] { 
					generateRegisterOrPair(codeitem, ((DexInstSwitch) inst).getValueRegister(), false),
					new InstSemConst(codeitem, ((DexInstSwitch) inst).getPayloadAddress())
			});
		}
		case THROW: {
			InstSemRegister exception = generateRegisterOrPair(codeitem, ((DexInstThrow) inst).getExceptionRegister(), false);
			
			// TODO
			// exception.setOperandTypeData(program.getExceptionClassType());
			
			return new InstSemEtcStatement(codeitem, "throw", new InstSemElement[] { exception });
		}
		case UNARY_OP: {
			DexInstUnaryOp unop = (DexInstUnaryOp) inst;
			
			InstSemRegister src = generateRegisterOrPair(codeitem, unop.getSourceRegister(), unop.isSourceWide());
			src.setOperandTypeData(unop.getSourceType());

			InstSemUnaryOp uop = new InstSemUnaryOp(codeitem, inst.getOpcode(), src);
			uop.setOperandTypeData(unop.getDestinationType());
			
			InstSemRegister dst = generateRegisterOrPair(codeitem, unop.getDestinationRegister(), unop.isDestinationWide());
			dst.setOperandTypeData(unop.getDestinationType());
			
			return new InstSemAssignment(codeitem, dst, uop);
		}
		default:
			assert inst instanceof DexInstNop;
			return null;
		}
	}
}
