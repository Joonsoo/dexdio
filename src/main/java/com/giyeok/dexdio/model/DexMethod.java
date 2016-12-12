package com.giyeok.dexdio.model;

import com.giyeok.dexdio.Utils;
import com.giyeok.dexdio.dexreader.MethodTable.Method;
import com.giyeok.dexdio.dexreader.ProtoTable.Proto;

public class DexMethod {
	private DexProgram program;

	private int methodId;
	private DexMethodContainingType belongedType;
	private String name;
	private DexType returnType;
	private DexParameter parameters[];
	private DexAnnotations annotations;
	private DexAccessFlags accessFlags;
	private MethodKind methodKind;		// directmethod이면 true, virtual method이면 false
	private DexCodeItem codeitem;
	
	public static enum MethodKind {
		INHERITED_METHOD,
		DIRECT_METHOD,
		VIRTUAL_METHOD
	}

	public DexMethod(DexProgram program, int methodId, Method method) throws DexException {
		this.program = program;
		
		DexType belongedType = program.getTypeByTypeId(method.getClassIdx());
		
		if (! (belongedType instanceof DexMethodContainingType)) {
			throw new DexException("Method " + method.getName() + " is not belonged to a class or array");
		}
		
		this.methodId = methodId;
		this.belongedType = (DexMethodContainingType) belongedType;
		this.name = method.getName();
		
		Proto proto = method.getProto();
		this.returnType = program.getTypeByTypeId(proto.getReturnTypeIdx());
		
		int paramcount = proto.getParametersCount();
		parameters = new DexParameter[paramcount];
		for (int i = 0; i < paramcount; i++) {
			parameters[i] = new DexParameter(program.getTypeByTypeId(proto.getParameter(i)));
		}
		
		accessFlags = new DexAccessFlags();
		annotations = null;
		methodKind = MethodKind.INHERITED_METHOD;
		codeitem = null;
		
		this.belongedType.addMethod(this);
	}
	
	public DexProgram getProgram() {
		return program;
	}
	
	public String getVisibility() {
		StringBuffer buf = new StringBuffer();
		if (accessFlags.isStatic()) {
			buf.append("_");
		}
		if (accessFlags.isPublic()) {
			buf.append("+");
		} else if (accessFlags.isPrivate()) {
			buf.append("-");
		} else if (accessFlags.isProtected()) {
			buf.append("#");
		} else {
			buf.append("~");
		}
		return buf.toString();
	}
	
	public String getParametersTypeFullBeauty() {
		String[] params = new String[parameters.length];
		
		for (int i = 0; i < params.length; i++) {
			params[i] = parameters[i].getType().getTypeFullNameBeauty();
		}
		return Utils.joinStrings(", ", params);
	}
	
	public String getParametersTypeShortBeauty() {
		String[] params = new String[parameters.length];
		
		for (int i = 0; i < params.length; i++) {
			params[i] = parameters[i].getType().getTypeShortNameBeauty();
		}
		return Utils.joinStrings(", ", params);
	}

	void setAnnotations(DexAnnotations annotations) {
		this.annotations = annotations;
	}
	
	void setAccessFlags(int accessFlags) {
		this.accessFlags = new DexAccessFlags(accessFlags);
	}
	
	void setMethodKind(MethodKind methodKind) {
		this.methodKind = methodKind;
	}
	
	void setCodeItem(DexCodeItem codeitem) {
		this.codeitem = codeitem;
	}
	
	public int getMethodId() {
		return methodId;
	}
	
	public DexMethodContainingType getBelongedType() {
		return belongedType;
	}
	
	public String getName() {
		return name;
	}
	
	public DexType getReturnType() {
		return returnType;
	}
	
	public DexParameter[] getParameters() {
		return parameters;
	}
	
	public DexType[] getParametersType() {
		DexType types[] = new DexType[parameters.length];
		
		for (int i = 0; i < parameters.length; i++) {
			types[i] = parameters[i].getType();
		}
		return types;
	}
	
	public DexAnnotations getAnnotations() {
		return annotations;
	}
	
	public DexAccessFlags getAccessFlags() {
		return accessFlags;
	}
	
	public MethodKind getMethodKind() {
		return methodKind;
	}
	
	public boolean isIntanceMethod() {
		return ! accessFlags.isStatic();
	}
	
	public DexCodeItem getCodeItem() {
		return codeitem;
	}
}
