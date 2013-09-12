package com.giyeok.dexdio;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Image;

import com.giyeok.dexdio.widgets.Label;

public class Utils {

	public static String joinStrings(String delim, String[] parts) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) {
				buf.append(delim);
			}
			buf.append(parts[i]);
		}
		return buf.toString();
	}
	
	public static class ImageResource {
		public static final String ICON_CLASS_PUBLIC = "./icons/innerclass_public_obj.gif";
		public static final String ICON_CLASS_PROTECTED = "./icons/innerclass_protected_obj.gif";
		public static final String ICON_CLASS_DEFAULT = "./icons/innerclass_default_obj.gif";
		public static final String ICON_CLASS_PRIVATE = "./icons/innerclass_private_obj.gif";

		public static final String ICON_INTERFACE_PUBLIC = "./icons/innerinterface_public_obj.gif";
		public static final String ICON_INTERFACE_PROTECTED = "./icons/innerinterface_protected_obj.gif";
		public static final String ICON_INTERFACE_DEFAULT = "./icons/innerinterface_default_obj.gif";
		public static final String ICON_INTERFACE_PRIVATE = "./icons/innerinterface_private_obj.gif";

		public static final String ICON_ENUM_PUBLIC = "./icons/enum_public_obj.gif";
		public static final String ICON_ENUM_PROTECTED = "./icons/enum_protected_obj.gif";
		public static final String ICON_ENUM_DEFAULT = "./icons/enum_default_obj.gif";
		public static final String ICON_ENUM_PRIVATE = "./icons/enum_private_obj.gif";

		public static final String ICON_METHOD_PUBLIC = "./icons/methpub_obj.gif";
		public static final String ICON_METHOD_PROTECTED = "./icons/methpro_obj.gif";
		public static final String ICON_METHOD_DEFAULT = "./icons/methdef_obj.gif";
		public static final String ICON_METHOD_PRIVATE = "./icons/methpri_obj.gif";
		
		public static final String ICON_FIELD_PUBLIC = "./icons/field_public_obj.gif";
		public static final String ICON_FIELD_PROTECTED = "./icons/field_protected_obj.gif";
		public static final String ICON_FIELD_DEFAULT = "./icons/field_default_obj.gif";
		public static final String ICON_FIELD_PRIVATE = "./icons/field_private_obj.gif";
		
		public static final String ICON_ABSTRACT = "./icons/abstract_co.gif";
		public static final String ICON_CONSTRUCTOR = "./icons/constr_ovr.gif";
		public static final String ICON_IMPLEMENT = "./icons/implm_co.gif";
		public static final String ICON_FINAL = "./icons/final_co.gif";
		public static final String ICON_NATIVE = "./icons/native_co.gif";
		public static final String ICON_OVERRIDE = "./icons/over_co.gif";
		public static final String ICON_STATIC = "./icons/static_co.gif";
		public static final String ICON_SYNCHRONIZED = "./icons/synch_co.gif";
		public static final String ICON_TRANSIENT = "./icons/transient_co.gif";
		public static final String ICON_VOLATILE = "./icons/volatile_co.gif";
		
		private static Map<String, Image> images = new HashMap<String, Image>();
		
		public static Image getImage(String path) {
			if (images.containsKey(path)) {
				return images.get(path);
			}
			Image image = new Image(null, path);
			images.put(path, image);
			return image;
		}
	}
}
