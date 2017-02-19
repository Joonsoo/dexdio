package com.giyeok.dexdio.model0.insns;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.giyeok.dexdio.model0.DexCodeItem;
import com.giyeok.dexdio.model0.DexException;

public class DexDisassembler {
	public static enum InstFormat {
		FORMAT_10x,
		FORMAT_12x, FORMAT_11n,
		FORMAT_11x, FORMAT_10t,
		FORMAT_20t,
		FORMAT_20bc,
		FORMAT_22x, FORMAT_21t, FORMAT_21s, FORMAT_21h_32, FORMAT_21h_64, FORMAT_21c,
		FORMAT_23x, FORMAT_22b,
		FORMAT_22t, FORMAT_22s, FORMAT_22c, FORMAT_22cs,
		FORMAT_30t,
		FORMAT_32x,
		FORMAT_31i, FORMAT_31t, FORMAT_31c,
		FORMAT_35c, FORMAT_35ms, FORMAT_35mi,
		FORMAT_3rc, FORMAT_3rms, FORMAT_3rmi,
		FORMAT_51l
	}
	
	/**
	 * Dalvik VM Instructions Formats http://source.android.com/tech/dalvik/instruction-formats.html
	 * @param insns
	 * @param pointer
	 * @param format
	 * @return
	 * @throws DexException 
	 */
	private static InstructionData organize(DexCodeItem codeitem, int[] insns, int pointer, InstFormat format) throws DexException {
		int length = 0;
		
		switch (format) {
		case FORMAT_10x: case FORMAT_12x: case FORMAT_11n: case FORMAT_11x: case FORMAT_10t:
			length = 1;
			break;
		case FORMAT_20t: case FORMAT_20bc: case FORMAT_22x: case FORMAT_21t: 
		case FORMAT_21s: case FORMAT_21h_32: case FORMAT_21h_64: case FORMAT_21c: 
		case FORMAT_23x: 
		case FORMAT_22b: case FORMAT_22t: case FORMAT_22s: case FORMAT_22c: case FORMAT_22cs:
			length = 2;
			break;
		case FORMAT_30t: case FORMAT_32x: case FORMAT_31i: case FORMAT_31t:
		case FORMAT_31c: case FORMAT_35c: case FORMAT_35ms: case FORMAT_35mi:
		case FORMAT_3rc: case FORMAT_3rms: case FORMAT_3rmi:
			length = 3;
			break;
		case FORMAT_51l:
			length = 5;
			break;
		default:
			assert false;
			return null;
		}
		assert length == 1 || length == 2 || length == 3 || length == 5;
		
		if (insns.length < pointer + length) {
			throw new DexException("Code is ended unexpectedly");
		}
		
		int data[] = Arrays.copyOfRange(insns, pointer, pointer + length);
		int opcode;
		opcode = data[0] & 0xff;
		
		int values[];
		
		switch (format) {
		case FORMAT_10x:
			values = null;
			break;
		case FORMAT_12x: case FORMAT_11n:
			values = new int[] { ((data[0] & 0xf00) >> 8), ((data[0] & 0xf000) >> 12) };
			break;
		case FORMAT_11x: case FORMAT_10t:
			values = new int[] { ((data[0] & 0xff00) >> 8) };
			break;
		case FORMAT_20t:
			values = new int[] { data[1] };
			break;
		case FORMAT_20bc:
			values = new int[] { ((data[0] & 0xff00) >> 8), data[1] };
			break;
		case FORMAT_22x: case FORMAT_21t: case FORMAT_21s: case FORMAT_21h_32: case FORMAT_21h_64: case FORMAT_21c:
			values = new int[] { ((data[0] & 0xff00) >> 8), data[1] };
			break;
		case FORMAT_23x: case FORMAT_22b:
			values = new int[] { ((data[0] & 0xff00) >> 8), (data[1] & 0xff), ((data[1] & 0xff00) >> 8) };
			break;
		case FORMAT_22t: case FORMAT_22s: case FORMAT_22c: case FORMAT_22cs:
			values = new int[] { ((data[0] & 0xf00) >> 8), ((data[0] & 0xf000) >> 12), data[1] };
			break;
		case FORMAT_30t:
			values = new int[] { (data[1] | (data[2] << 16)) };
			break;
		case FORMAT_32x:
			values = new int[] { data[1], data[2] };
			break;
		case FORMAT_31i: case FORMAT_31t: case FORMAT_31c:
			values = new int[] { ((data[0] & 0xff00) >> 8), (data[1] | (data[2] << 16)) };
			break;
		case FORMAT_35c: case FORMAT_35ms: case FORMAT_35mi:
			values = new int[] { ((data[0] & 0xf000) >> 12), data[1], (data[2] & 0xf),
					((data[2] & 0xf0) >> 4), ((data[2] & 0xf00) >> 8), ((data[2] & 0xf000) >> 12), 
					((data[0] & 0xf00) >> 8) };
			break;
		case FORMAT_3rc: case FORMAT_3rms: case FORMAT_3rmi:
			values = new int[] { ((data[0] & 0xff00) >> 8), data[1], data[2] };
			break;
		case FORMAT_51l:
			values = new int[] { ((data[0] & 0xff00) >> 8), (data[1] | (data[2] << 16)), (data[3] | (data[4] << 16)) };
			break;
		default:
			values = null;
			assert false;
		}
		
		Operand operands[] = null;

		switch (format) {
		case FORMAT_10x:
			operands = null;
			break;
		case FORMAT_12x:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1])
			};
			break;
		case FORMAT_11n:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]), 
					new OperandIntegerConstant(codeitem, values[1], 'n')
			};
			break;
		case FORMAT_11x:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0])
			};
			break;
		case FORMAT_10t:
			operands = new Operand[] {
					new OperandBranchTarget(codeitem, values[0], 1)
			};
			break;
		case FORMAT_20t:
			operands = new Operand[] {
					new OperandBranchTarget(codeitem, values[0], 2)
			};
			break;
		case FORMAT_20bc:
			// not used here!
			assert false;
			operands = new Operand[] {
					new OperandIntegerConstant(codeitem, values[0], 'b'),
					new OperandConstantPool(codeitem, values[1])
			};
			break;
		case FORMAT_22x:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1])
			};
			break;
		case FORMAT_21t:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandBranchTarget(codeitem, values[1], 2)
			};
			break;
		case FORMAT_21s:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]), 
					new OperandIntegerConstant(codeitem, values[1], 's')
			};
			break;
		case FORMAT_21h_32:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]), 
					new OperandIntegerConstant(codeitem, values[1], 'h')
			};
			break;
		case FORMAT_21h_64:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]), 
					new OperandIntegerConstant(codeitem, values[1], 'H')
			};
			break;
		case FORMAT_21c:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]), 
					new OperandConstantPool(codeitem, values[1])
			};
			break;
		case FORMAT_23x:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1]),
					new OperandRegister(codeitem, values[2])
			};
			break;
		case FORMAT_22b:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1]),
					new OperandIntegerConstant(codeitem, values[2], 'b')
			};
			break;
		case FORMAT_22t:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1]),
					new OperandBranchTarget(codeitem, values[2], 2)
			};
			break;
		case FORMAT_22s:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1]),
					new OperandIntegerConstant(codeitem, values[2], 's')
			};
			break;
		case FORMAT_22c:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1]),
					new OperandConstantPool(codeitem, values[2])
			};
			break;
		case FORMAT_22cs:
			// not used here!
			assert false;
			break;
		case FORMAT_30t:
			operands = new Operand[] {
					new OperandBranchTarget(codeitem, values[0], 4)
			};
			break;
		case FORMAT_32x:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandRegister(codeitem, values[1])
			};
			break;
		case FORMAT_31i:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandIntegerConstant(codeitem, values[1], 'i')
			};
			break;
		case FORMAT_31t:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandBranchTarget(codeitem, values[1], 4)
			};
			break;
		case FORMAT_31c:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandConstantPool(codeitem, values[1])
			};
			break;
		case FORMAT_35c:
			operands = new Operand[values[0] + 1];
			operands[0] = new OperandConstantPool(codeitem, values[1]);
			for (int i = 0; i < values[0]; i++) {
				operands[i + 1] = new OperandRegister(codeitem, values[i + 2]);
			}
			break;
		case FORMAT_35ms:
		case FORMAT_35mi:
			// not used here!
			assert false;
			break;
		case FORMAT_3rc:
			operands = new Operand[] {
					new OperandConstantPool(codeitem, values[1]),
					new OperandRegisterRange(codeitem, values[2], values[0])
			};
			break;
		case FORMAT_3rms:
		case FORMAT_3rmi:
			// not used here!
			assert false;
			break;
		case FORMAT_51l:
			operands = new Operand[] {
					new OperandRegister(codeitem, values[0]),
					new OperandIntegerConstant(codeitem, ((long) values[1]) | (((long) values[2]) << 32), 'l')
			};
			break;
		default:
			assert false;
		}
		return new InstructionData(length, data, opcode, operands);
	}
	
	public static class InstFormatData {
		private int opcode;
		private Class cls;
		private InstFormat format;
		
		public InstFormatData(int opcode, Class cls, InstFormat format) {
			this.opcode = opcode;
			this.cls = cls;
			this.format = format;
		}
	}
	
	private static final InstFormatData formats[] = new InstFormatData[] {
			new InstFormatData(0x00, DexInstNop.class, InstFormat.FORMAT_10x),					// nop
			new InstFormatData(0x01, DexInstMove.class, InstFormat.FORMAT_12x),					// move vA, vB
			new InstFormatData(0x02, DexInstMove.class, InstFormat.FORMAT_22x),					// move/from16 vAA, vBBBB
			new InstFormatData(0x03, DexInstMove.class, InstFormat.FORMAT_32x),					// move/16 vAAAA, vBBBB
			new InstFormatData(0x04, DexInstMove.class, InstFormat.FORMAT_12x),					// move-wide vA, vB
			new InstFormatData(0x05, DexInstMove.class, InstFormat.FORMAT_22x),					// move-wide/from16 vAA, vBBBB
			new InstFormatData(0x06, DexInstMove.class, InstFormat.FORMAT_32x),					// move-wide/16 vAAAA, vBBBB
			new InstFormatData(0x07, DexInstMove.class, InstFormat.FORMAT_12x),					// move-object vA, vB
			new InstFormatData(0x08, DexInstMove.class, InstFormat.FORMAT_22x),					// move-object/from16 vAA, vBBBB
			new InstFormatData(0x09, DexInstMove.class, InstFormat.FORMAT_32x),					// move-object/16 vAAAA, vBBBB
			new InstFormatData(0x0a, DexInstMoveResult.class, InstFormat.FORMAT_11x),			// move-result vAA
			new InstFormatData(0x0b, DexInstMoveResult.class, InstFormat.FORMAT_11x),			// move-result-wide vAA
			new InstFormatData(0x0c, DexInstMoveResult.class, InstFormat.FORMAT_11x),			// move-result-object vAA
			new InstFormatData(0x0d, DexInstMoveException.class, InstFormat.FORMAT_11x),		// move-exception vAA
			new InstFormatData(0x0e, DexInstReturn.class, InstFormat.FORMAT_10x),				// return-void
			new InstFormatData(0x0f, DexInstReturn.class, InstFormat.FORMAT_11x),				// return vAA
			new InstFormatData(0x10, DexInstReturn.class, InstFormat.FORMAT_11x),				// return-wide vAA
			new InstFormatData(0x11, DexInstReturn.class, InstFormat.FORMAT_11x),				// return-object vAA
			new InstFormatData(0x12, DexInstMoveConst.class, InstFormat.FORMAT_11n),					// const/4 vA, #+B
			new InstFormatData(0x13, DexInstMoveConst.class, InstFormat.FORMAT_21s),					// const/16 vAA, #+BBBB
			new InstFormatData(0x14, DexInstMoveConst.class, InstFormat.FORMAT_31i),					// const vAA, #+BBBBBBBB
			new InstFormatData(0x15, DexInstMoveConst.class, InstFormat.FORMAT_21h_32),				// const/high16 vAA, #+BBBB0000
			new InstFormatData(0x16, DexInstMoveConst.class, InstFormat.FORMAT_21s),					// const-wide/16 vAA, #+BBBB
			new InstFormatData(0x17, DexInstMoveConst.class, InstFormat.FORMAT_31i),					// const-wide/32 vAA, #+BBBBBBBB
			new InstFormatData(0x18, DexInstMoveConst.class, InstFormat.FORMAT_51l),					// const-wide vAA, #+BBBBBBBBBBBBBBBB
			new InstFormatData(0x19, DexInstMoveConst.class, InstFormat.FORMAT_21h_64),				// const-wide/high16 vAA, #+BBBB000000000000
			new InstFormatData(0x1a, DexInstMoveConst.class, InstFormat.FORMAT_21c),					// const-string vAA, string@BBBB
			new InstFormatData(0x1b, DexInstMoveConst.class, InstFormat.FORMAT_31c),					// const-string/jumbo vAA, string@BBBBBBBB
			new InstFormatData(0x1c, DexInstMoveConst.class, InstFormat.FORMAT_21c),					// const-class vAA, type@BBBB
			new InstFormatData(0x1d, DexInstMonitor.class, InstFormat.FORMAT_11x),				// monitor-enter vAA
			new InstFormatData(0x1e, DexInstMonitor.class, InstFormat.FORMAT_11x),				// monitor-exit vAA
			new InstFormatData(0x1f, DexInstCheckCast.class, InstFormat.FORMAT_21c),			// check-cast vAA, type@BBBB
			new InstFormatData(0x20, DexInstInstanceOf.class, InstFormat.FORMAT_22c),			// instance-of vA, vB, type@CCCC
			new InstFormatData(0x21, DexInstArrayLength.class, InstFormat.FORMAT_12x),			// array-length vA, vB
			new InstFormatData(0x22, DexInstNewInstance.class, InstFormat.FORMAT_21c),			// new-instance vAA, type@BBBB
			new InstFormatData(0x23, DexInstNewArray.class, InstFormat.FORMAT_22c),				// new-array vA, vB, type@CCCC
			new InstFormatData(0x24, DexInstFilledNewArray.class, InstFormat.FORMAT_35c),		// filled-new-array {vC, vD, vE, vF, vG}, type@BBBB
			new InstFormatData(0x25, DexInstFilledNewArray.class, InstFormat.FORMAT_3rc),		// filled-new-array/range {vCCCC .. vNNNN}, type@BBBB
			new InstFormatData(0x26, DexInstFillArrayData.class, InstFormat.FORMAT_31t),		// fill-array-data vAA, +BBBBBBBB(with supplemental data as specified below in "fill-array-data-payload Format")
			new InstFormatData(0x27, DexInstThrow.class, InstFormat.FORMAT_11x),				// throw vAA
			new InstFormatData(0x28, DexInstGoto.class, InstFormat.FORMAT_10t),					// goto +AA
			new InstFormatData(0x29, DexInstGoto.class, InstFormat.FORMAT_20t),					// goto/16 +AAAA
			new InstFormatData(0x2a, DexInstGoto.class, InstFormat.FORMAT_30t),					// goto/32 +AAAAAAAA
			new InstFormatData(0x2b, DexInstSwitch.class, InstFormat.FORMAT_31t),				// packed-switch vAA, +BBBBBBBB (with supplemental data as specified below in "packed-switch-payload Format")
			new InstFormatData(0x2c, DexInstSwitch.class, InstFormat.FORMAT_31t),				// sparse-switch vAA, +BBBBBBBB (with supplemental data as specified below in "sparse-switch-payload Format")
			// 2d..31 ========== cmpkind vAA, vBB, vCC
			new InstFormatData(0x2d, DexInstCompare.class, InstFormat.FORMAT_23x),		// cmpl-float (lt bias)
			new InstFormatData(0x2e, DexInstCompare.class, InstFormat.FORMAT_23x),		// cmpg-float (gt bias)
			new InstFormatData(0x2f, DexInstCompare.class, InstFormat.FORMAT_23x),		// cmpl-double (lt bias)
			new InstFormatData(0x30, DexInstCompare.class, InstFormat.FORMAT_23x),		// cmpg-double (gt bias)
			new InstFormatData(0x31, DexInstCompare.class, InstFormat.FORMAT_23x),		// cmp-long
			// 32..37 ========== if-test vA, vB, +CCCC
			new InstFormatData(0x32, DexInstIf.class, InstFormat.FORMAT_22t),		// if-eq
			new InstFormatData(0x33, DexInstIf.class, InstFormat.FORMAT_22t),		// if-ne
			new InstFormatData(0x34, DexInstIf.class, InstFormat.FORMAT_22t),		// if-lt
			new InstFormatData(0x35, DexInstIf.class, InstFormat.FORMAT_22t),		// if-ge
			new InstFormatData(0x36, DexInstIf.class, InstFormat.FORMAT_22t),		// if-gt
			new InstFormatData(0x37, DexInstIf.class, InstFormat.FORMAT_22t),		// if-le
			// 38..3d ========== if-testz vAA, +BBBB
			new InstFormatData(0x38, DexInstIfZero.class, InstFormat.FORMAT_21t),		// if-eqz
			new InstFormatData(0x39, DexInstIfZero.class, InstFormat.FORMAT_21t),		// if-nez
			new InstFormatData(0x3a, DexInstIfZero.class, InstFormat.FORMAT_21t),		// if-ltz
			new InstFormatData(0x3b, DexInstIfZero.class, InstFormat.FORMAT_21t),		// if-gez
			new InstFormatData(0x3c, DexInstIfZero.class, InstFormat.FORMAT_21t),		// if-gtz
			new InstFormatData(0x3d, DexInstIfZero.class, InstFormat.FORMAT_21t),		// if-lez
			// 3e..43 ========== (unused)
			null, null, null, null, null, null,
			// 44..51 ========== arrayop vAA, vBB, vCC
			new InstFormatData(0x44, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aget
			new InstFormatData(0x45, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aget-wide
			new InstFormatData(0x46, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aget-object
			new InstFormatData(0x47, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aget-boolean
			new InstFormatData(0x48, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aget-byte
			new InstFormatData(0x49, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aget-char
			new InstFormatData(0x4a, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aget-short
			new InstFormatData(0x4b, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aput
			new InstFormatData(0x4c, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aput-wide
			new InstFormatData(0x4d, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aput-object
			new InstFormatData(0x4e, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aput-boolean
			new InstFormatData(0x4f, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aput-byte
			new InstFormatData(0x50, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aput-char
			new InstFormatData(0x51, DexInstArrayOp.class, InstFormat.FORMAT_23x),		// aput-short
			// 52..5f ========== iinstanceop vA, vB, field@CCCC
			new InstFormatData(0x52, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iget
			new InstFormatData(0x53, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iget-wide
			new InstFormatData(0x54, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iget-object
			new InstFormatData(0x55, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iget-boolean
			new InstFormatData(0x56, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iget-byte
			new InstFormatData(0x57, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iget-char
			new InstFormatData(0x58, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iget-short
			new InstFormatData(0x59, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iput
			new InstFormatData(0x5a, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iput-wide
			new InstFormatData(0x5b, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iput-object
			new InstFormatData(0x5c, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iput-boolean
			new InstFormatData(0x5d, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iput-byte
			new InstFormatData(0x5e, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iput-char
			new InstFormatData(0x5f, DexInstInstanceOp.class, InstFormat.FORMAT_22c),		// iput-short
			// 60..6d ========== sstaticop vAA, field@BBBB
			new InstFormatData(0x60, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sget
			new InstFormatData(0x61, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sget-wide
			new InstFormatData(0x62, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sget-object
			new InstFormatData(0x63, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sget-boolean
			new InstFormatData(0x64, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sget-byte
			new InstFormatData(0x65, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sget-char
			new InstFormatData(0x66, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sget-short
			new InstFormatData(0x67, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sput
			new InstFormatData(0x68, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sput-wide
			new InstFormatData(0x69, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sput-object
			new InstFormatData(0x6a, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sput-boolean
			new InstFormatData(0x6b, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sput-byte
			new InstFormatData(0x6c, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sput-char
			new InstFormatData(0x6d, DexInstStaticOp.class, InstFormat.FORMAT_21c),		// sput-short
			// 6e..72 ========== invoke-kind {vC, vD, vE, vF, vG}, meth@BBBB
			new InstFormatData(0x6e, DexInstInvoke.class, InstFormat.FORMAT_35c),		// invoke-virtual
			new InstFormatData(0x6f, DexInstInvoke.class, InstFormat.FORMAT_35c),		// invoke-super
			new InstFormatData(0x70, DexInstInvoke.class, InstFormat.FORMAT_35c),		// invoke-direct
			new InstFormatData(0x71, DexInstInvoke.class, InstFormat.FORMAT_35c),		// invoke-static
			new InstFormatData(0x72, DexInstInvoke.class, InstFormat.FORMAT_35c),		// invoke-interface
			// 73..73 ========== (unused)
			null,
			// 74..78 ========== invoke-kind/range {vCCCC .. vNNNN}, meth@BBBB
			new InstFormatData(0x74, DexInstInvoke.class, InstFormat.FORMAT_3rc),		// invoke-virtual/range
			new InstFormatData(0x75, DexInstInvoke.class, InstFormat.FORMAT_3rc),		// invoke-super/range
			new InstFormatData(0x76, DexInstInvoke.class, InstFormat.FORMAT_3rc),		// invoke-direct/range
			new InstFormatData(0x77, DexInstInvoke.class, InstFormat.FORMAT_3rc),		// invoke-static/range
			new InstFormatData(0x78, DexInstInvoke.class, InstFormat.FORMAT_3rc),		// invoke-interface/range
			// 79..7a ========== (unused)
			null, null,
			// 7b..8f ========== unop vA, vB
			new InstFormatData(0x7b, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// neg-int
			new InstFormatData(0x7c, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// not-int
			new InstFormatData(0x7d, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// neg-long
			new InstFormatData(0x7e, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// not-long
			new InstFormatData(0x7f, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// neg-float
			new InstFormatData(0x80, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// neg-double
			new InstFormatData(0x81, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// int-to-long
			new InstFormatData(0x82, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// int-to-float
			new InstFormatData(0x83, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// int-to-double
			new InstFormatData(0x84, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// long-to-int
			new InstFormatData(0x85, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// long-to-float
			new InstFormatData(0x86, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// long-to-double
			new InstFormatData(0x87, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// float-to-int
			new InstFormatData(0x88, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// float-to-long
			new InstFormatData(0x89, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// float-to-double
			new InstFormatData(0x8a, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// double-to-int
			new InstFormatData(0x8b, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// double-to-long
			new InstFormatData(0x8c, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// double-to-float
			new InstFormatData(0x8d, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// int-to-byte
			new InstFormatData(0x8e, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// int-to-char
			new InstFormatData(0x8f, DexInstUnaryOp.class, InstFormat.FORMAT_12x),		// int-to-short
			// 90..af ========== binop vAA, vBB, vCC
			new InstFormatData(0x90, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// add-int
			new InstFormatData(0x91, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// sub-int
			new InstFormatData(0x92, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// mul-int
			new InstFormatData(0x93, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// div-int
			new InstFormatData(0x94, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// rem-int
			new InstFormatData(0x95, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// and-int
			new InstFormatData(0x96, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// or-int
			new InstFormatData(0x97, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// xor-int
			new InstFormatData(0x98, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// shl-int
			new InstFormatData(0x99, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// shr-int
			new InstFormatData(0x9a, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// ushr-int
			new InstFormatData(0x9b, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// add-long
			new InstFormatData(0x9c, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// sub-long
			new InstFormatData(0x9d, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// mul-long
			new InstFormatData(0x9e, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// div-long
			new InstFormatData(0x9f, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// rem-long
			new InstFormatData(0xa0, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// and-long
			new InstFormatData(0xa1, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// or-long
			new InstFormatData(0xa2, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// xor-long
			new InstFormatData(0xa3, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// shl-long
			new InstFormatData(0xa4, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// shr-long
			new InstFormatData(0xa5, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// ushr-long
			new InstFormatData(0xa6, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// add-float
			new InstFormatData(0xa7, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// sub-float
			new InstFormatData(0xa8, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// mul-float
			new InstFormatData(0xa9, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// div-float
			new InstFormatData(0xaa, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// rem-float
			new InstFormatData(0xab, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// add-double
			new InstFormatData(0xac, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// sub-double
			new InstFormatData(0xad, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// mul-double
			new InstFormatData(0xae, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// div-double
			new InstFormatData(0xaf, DexInstBinaryOp.class, InstFormat.FORMAT_23x),		// rem-double
			// b0..cf ========== binop/2addr vA, vB
			new InstFormatData(0xb0, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// add-int/2addr
			new InstFormatData(0xb1, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// sub-int/2addr
			new InstFormatData(0xb2, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// mul-int/2addr
			new InstFormatData(0xb3, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// div-int/2addr
			new InstFormatData(0xb4, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// rem-int/2addr
			new InstFormatData(0xb5, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// and-int/2addr
			new InstFormatData(0xb6, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// or-int/2addr
			new InstFormatData(0xb7, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// xor-int/2addr
			new InstFormatData(0xb8, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// shl-int/2addr
			new InstFormatData(0xb9, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// shr-int/2addr
			new InstFormatData(0xba, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// ushr-int/2addr
			new InstFormatData(0xbb, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// add-long/2addr
			new InstFormatData(0xbc, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// sub-long/2addr
			new InstFormatData(0xbd, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// mul-long/2addr
			new InstFormatData(0xbe, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// div-long/2addr
			new InstFormatData(0xbf, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// rem-long/2addr
			new InstFormatData(0xc0, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// and-long/2addr
			new InstFormatData(0xc1, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// or-long/2addr
			new InstFormatData(0xc2, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// xor-long/2addr
			new InstFormatData(0xc3, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// shl-long/2addr
			new InstFormatData(0xc4, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// shr-long/2addr
			new InstFormatData(0xc5, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// ushr-long/2addr
			new InstFormatData(0xc6, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// add-float/2addr
			new InstFormatData(0xc7, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// sub-float/2addr
			new InstFormatData(0xc8, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// mul-float/2addr
			new InstFormatData(0xc9, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// div-float/2addr
			new InstFormatData(0xca, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// rem-float/2addr
			new InstFormatData(0xcb, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// add-double/2addr
			new InstFormatData(0xcc, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// sub-double/2addr
			new InstFormatData(0xcd, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// mul-double/2addr
			new InstFormatData(0xce, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// div-double/2addr
			new InstFormatData(0xcf, DexInstBinaryOp.class, InstFormat.FORMAT_12x),		// rem-double/2addr
			// d0..d7 ========== binop/lit16 vA, vB, #+CCCC
			new InstFormatData(0xd0, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// add-int/lit16
			new InstFormatData(0xd1, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// rsub-int (reverse subtract)
			new InstFormatData(0xd2, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// mul-int/lit16
			new InstFormatData(0xd3, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// div-int/lit16
			new InstFormatData(0xd4, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// rem-int/lit16
			new InstFormatData(0xd5, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// and-int/lit16
			new InstFormatData(0xd6, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// or-int/lit16
			new InstFormatData(0xd7, DexInstBinaryOpLit.class, InstFormat.FORMAT_22s),		// xor-int/lit16
			// d8..e2 ========== binop/lit8 vAA, vBB, #+CC
			new InstFormatData(0xd8, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// add-int/lit8
			new InstFormatData(0xd9, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// rsub-int/lit8
			new InstFormatData(0xda, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// mul-int/lit8
			new InstFormatData(0xdb, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// div-int/lit8
			new InstFormatData(0xdc, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// rem-int/lit8
			new InstFormatData(0xdd, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// and-int/lit8
			new InstFormatData(0xde, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// or-int/lit8
			new InstFormatData(0xdf, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// xor-int/lit8
			new InstFormatData(0xe0, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// shl-int/lit8
			new InstFormatData(0xe1, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// shr-int/lit8
			new InstFormatData(0xe2, DexInstBinaryOpLit.class, InstFormat.FORMAT_22b),		// ushr-int/lit8
	};
	
	public static DexInstruction disassemble1(DexCodeItem codeitem, int[] insns, int pointer) throws DexException {
		if (pointer >= insns.length) {
			// reached an wrong path
			return null;
		}
		
		/*
		for (int i = 0; i < formats.length; i++) {
			if (formats[i] != null) {
				assert formats[i].opcode == i;
			}
		}
		*/
		int opcode = insns[pointer] & 0xff;
		if (opcode > formats.length || formats[opcode] == null) {
			throw new DexException("Unknown opcode: " + DexInstruction.getOpcodeHex(opcode));
		}
		
		InstFormatData format = formats[opcode];
		assert (opcode == format.opcode);
		
		DexInstruction instruction;
		
		try {
			InstructionData data = organize(codeitem, insns, pointer, format.format);
			Constructor ctor;
			
			ctor = format.cls.getDeclaredConstructor(DexCodeItem.class, int.class, InstructionData.class);
			instruction = (DexInstruction) ctor.newInstance(codeitem, pointer, data);
			
			data.setBelongedInstruction(instruction);
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new DexException("Wrong instruction format for opcode " + opcode);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new DexException("Wrong instruction format for opcode " + opcode);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new DexException("Wrong instruction format for opcode " + opcode);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new DexException("Wrong instruction format for opcode " + opcode);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new DexException("Wrong instruction format for opcode " + opcode);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new DexException("Wrong instruction format for opcode " + opcode);
		}
		return instruction;
	}

}
