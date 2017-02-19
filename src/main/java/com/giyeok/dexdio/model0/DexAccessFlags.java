package com.giyeok.dexdio.model0;

import org.eclipse.draw2d.ColorConstants;

import com.giyeok.dexdio.widgets.Label;

public class DexAccessFlags {
	public static final int ACC_PUBLIC = 0x1;
	public static final int ACC_PRIVATE = 0x2;
	public static final int ACC_PROTECTED = 0x4;
	public static final int ACC_STATIC = 0x8;
	public static final int ACC_FINAL = 0x10;
	public static final int ACC_SYNCHRONIZED = 0x20;
	public static final int ACC_VOLATILE = 0x40;
	public static final int ACC_BRIDGE = 0x40;
	public static final int ACC_TRANSIENT = 0x80;
	public static final int ACC_VARARGS = 0x80;
	public static final int ACC_NATIVE = 0x100;
	public static final int ACC_INTERFACE = 0x200;
	public static final int ACC_ABSTRACT = 0x400;
	public static final int ACC_STRICT = 0x800;
	public static final int ACC_SYNTHETIC = 0x1000;
	public static final int ACC_ANNOTATION = 0x2000;
	public static final int ACC_ENUM = 0x4000;
	public static final int ACC_CONSTRUCTOR = 0x10000;
	public static final int ACC_DECLARED_SYNCHRONIZED = 0x20000;
	
	private boolean defined;
	private int value;
	
	public DexAccessFlags(int value) {
		this.value = value;
		this.defined = true;
	}
	
	public DexAccessFlags() {
		this.value = 0;
		this.defined = false;
	}
	
	public boolean isUndefined() {
		return ! defined;
	}
	
	public boolean isZero() {
		return value == 0;
	}
	
	public boolean isPublic() {
		return (value & ACC_PUBLIC) != 0;
	}
	
	public boolean isPrivate() {
		return (value & ACC_PRIVATE) != 0;
	}
	
	public boolean isProtected() {
		return (value & ACC_PROTECTED) != 0;
	}
	
	public boolean isStatic() {
		return (value & ACC_STATIC) != 0;
	}
	
	public boolean isFinal() {
		return (value & ACC_FINAL) != 0;
	}
	
	public boolean isEnum() {
		return (value & ACC_ENUM) != 0;
	}
	
	public boolean isInterface() {
		return (value & ACC_INTERFACE) != 0;
	}
	
	public boolean isConstructor() {
		return (value & ACC_CONSTRUCTOR) != 0;
	}
	
	private static String stringifyAccessFlags(int access_flags, Object map[]) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < map.length; i += 2) {
			if ((access_flags & ((Integer) map[i])) != 0) {
				buf.append(" ");
				buf.append((String) map[i + 1]);
			}
		}
		return buf.toString().trim();
	}
	
	public static Label labelizeAccessFlagsForClasses(int access_flags) {
		String string = stringifyAccessFlagsForClasses(access_flags);
		if (! string.isEmpty()) {
			string += " ";
		}
		return Label.newLabel(string, ColorConstants.darkGreen);
	}
	
	public static Label labelizeAccessFlagsForFields(int access_flags) {
		String string = stringifyAccessFlagsForFields(access_flags);
		if (! string.isEmpty()) {
			string += " ";
		}
		return Label.newLabel(string, ColorConstants.darkGreen);
	}
	
	public static Label labelizeAccessFlagsForMethods(int access_flags) {
		String string = stringifyAccessFlagsForMethods(access_flags);
		if (! string.isEmpty()) {
			string += " ";
		}
		return Label.newLabel(string, ColorConstants.darkGreen);
	}
	
	public static String stringifyAccessFlagsForClasses(int access_flags) {
		final Object map[] = new Object[] {
				ACC_PUBLIC, "public",
				ACC_PRIVATE, "private",
				ACC_PROTECTED, "protected",
				ACC_STATIC, "static",
				ACC_FINAL, "final",
				ACC_ABSTRACT, "abstract",
				ACC_SYNTHETIC, "synthetic",
				ACC_ANNOTATION, "annotated",
				ACC_INTERFACE, "interface",
				ACC_ENUM, "enum"
		};
		return stringifyAccessFlags(access_flags, map);
	}
	
	public static String stringifyAccessFlagsForFields(int access_flags) {
		final Object map[] = new Object[] {
				ACC_PUBLIC, "public",
				ACC_PRIVATE, "private",
				ACC_PROTECTED, "protected",
				ACC_STATIC, "static",
				ACC_FINAL, "final",
				ACC_VOLATILE, "volatile",
				ACC_TRANSIENT, "transient",
				ACC_SYNTHETIC, "synthetic",
				ACC_ENUM, "enum"
		};
		return stringifyAccessFlags(access_flags, map);
	}
	
	public static String stringifyAccessFlagsForMethods(int access_flags) {
		final Object map[] = new Object[] {
				ACC_PUBLIC, "public",
				ACC_PRIVATE, "private",
				ACC_PROTECTED, "protected",
				ACC_STATIC, "static",
				ACC_FINAL, "final",
				ACC_SYNCHRONIZED, "synchronized",
				ACC_BRIDGE, "bridge",
				ACC_VARARGS, "varargs",
				ACC_NATIVE, "native",
				ACC_ABSTRACT, "abstract",
				ACC_STRICT, "strictfp",
				ACC_SYNTHETIC, "synthetic",
				ACC_CONSTRUCTOR, "constructor",
				ACC_DECLARED_SYNCHRONIZED, "declared_synchronized"
		};
		return stringifyAccessFlags(access_flags, map);
	}
	
	public Label labelizeForClasses() {
		return labelizeAccessFlagsForClasses(this.value);
	}
	
	public Label labelizeForFields() {
		return labelizeAccessFlagsForFields(this.value);
	}
	
	public Label labelizeForMethods() {
		return labelizeAccessFlagsForMethods(this.value);
	}

	public String stringifyForClasses() {
		return stringifyAccessFlagsForClasses(this.value);
	}

	public String stringifyForFields() {
		return stringifyAccessFlagsForFields(this.value);
	}

	public String stringifyForMethods() {
		return stringifyAccessFlagsForMethods(this.value);
	}
}
