package com.giyeok.dexdio.views;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.giyeok.dexdio.MainView;
import com.giyeok.dexdio.Utils;
import com.giyeok.dexdio.dexreader.ClassTable;
import com.giyeok.dexdio.dexreader.ClassTable.ClassDef;
import com.giyeok.dexdio.dexreader.DalvikExecutable;
import com.giyeok.dexdio.dexreader.FieldTable;
import com.giyeok.dexdio.dexreader.FieldTable.Field;
import com.giyeok.dexdio.dexreader.MethodTable;
import com.giyeok.dexdio.dexreader.MethodTable.Method;
import com.giyeok.dexdio.dexreader.ProtoTable;
import com.giyeok.dexdio.dexreader.ProtoTable.Proto;
import com.giyeok.dexdio.dexreader.StringTable;
import com.giyeok.dexdio.dexreader.TypeTable;
import com.giyeok.dexdio.dexreader.structs.class_data_item;
import com.giyeok.dexdio.dexreader.structs.encoded_field;
import com.giyeok.dexdio.dexreader.structs.encoded_method;
import com.giyeok.dexdio.dexreader.structs.header_item;
import com.giyeok.dexdio.model.DexAccessFlags;
import com.giyeok.dexdio.widgets.ListTitleItem;
import com.giyeok.dexdio.widgets.ListWidget;
import com.giyeok.dexdio.widgets.ListWidgetSelectionListener;
import com.giyeok.dexdio.widgets.OneSelectionListEventListener;
import com.giyeok.dexdio.widgets.TextColumnListWidget;

public class DexHexStructureViewer extends JPanel {
	private static final long serialVersionUID = -5621170751240045576L;
	
	private TabFolder tabFolder;
	
	public TabFolder getTabFolder() {
		return tabFolder;
	}
	
	public DexHexStructureViewer(Composite parent, MainView mainView, DalvikExecutable dex) {
		
		tabFolder = new TabFolder(parent, SWT.NONE);
		
		addTab("header", new HeaderList(tabFolder, SWT.NONE, dex.getHeader()));
		addTab("String", new StringList(tabFolder, SWT.NONE, dex.getStringTable()));
		addTab("Type", new TypeList(tabFolder, SWT.NONE, dex.getTypeTable()));
		addTab("Proto", new ProtoList(tabFolder, SWT.NONE, dex.getProtoTable()));
		addTab("Field", new FieldList(tabFolder, SWT.NONE, dex.getFieldTable()));
		addTab("Method", new MethodList(tabFolder, SWT.NONE, dex.getMethodTable()));
		addTab("Class", new ClassInfoTable(tabFolder, SWT.NONE, dex.getClassTable()).getComposite());
	}
	
	private void addTab(String title, Control control) {
		TabItem tabItem;
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(title);
		tabItem.setControl(control);
	}
}

class HeaderList extends ListWidget {
	private final String[] names = {
			"magic", "checksum", "signature", "file_size", "header_size", 
			"endian_tag", "link_size", "link_off", "map_off", "string_ids_size", 
			"string_ids_off", "type_ids_size", "type_ids_off", "proto_ids_size", "proto_ids_off", 
			"field_ids_size", "field_ids_off", "method_ids_size", "method_ids_off", "class_defs_size", 
			"class_defs_off", "data_size", "data_off"
		};
	private String hex[];
	private String value[];
	
	protected ListTitleItem[] title;
	
	public HeaderList(Composite parent, int style, header_item header) {
		super(parent, style);
		
		this.hex = new String[23];
		this.value = new String[23];
		hex[0] = byteArrayToHexString(header.magic());
		value[0] = null;
		set(1, header.checksum());
		hex[2] = byteArrayToHexString(header.signature());
		value[2] = null;
		set(3, header.file_size());
		set(4, header.header_size());
		set(5, header.endian_tag());
		set(6, header.link_size());
		set(7, header.link_off());
		set(8, header.map_off());
		set(9, header.string_ids_size());
		set(10, header.string_ids_off());
		set(11, header.type_ids_size());
		set(12, header.type_ids_off());
		set(13, header.proto_ids_size());
		set(14, header.proto_ids_off());
		set(15, header.field_ids_size());
		set(16, header.field_ids_off());
		set(17, header.method_ids_size());
		set(18, header.method_ids_off());
		set(19, header.class_defs_size());
		set(20, header.class_defs_off());
		set(21, header.data_size());
		set(22, header.data_off());
		
		title = new ListTitleItem[3];
		title[0] = new ListTitleItem(5, "name");
		title[1] = new ListTitleItem(5, "hex");
		title[2] = new ListTitleItem(5, "value");
		setTitle(title);
		
		setListSize(23);
		
		addKeyListener(new DefaultKeyListener());
	}
	
	private String byteArrayToHexString(byte b[]) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < b.length; i++) {
			if (i > 0) {
				buf.append(" ");
			}
			buf.append(uintToHexString(b[i], 2));
		}
		return buf.toString();
	}
	
	private String uintToHexString(long uint, int digit) {
		StringBuffer buf = new StringBuffer();
		for (int i = 4 * (digit - 1); i >= 0; i-=4) {
			buf.append("0123456789abcdef".charAt((int) ((uint & (0xf << i)) >> i)));
		}
		return buf.toString();
	}
	
	private void set(int index, long v) {
		hex[index] = uintToHexString(v, 8);
		value[index] = "" + v;
	}
	
	@Override
	protected void calculateFontSize(GC g) {
		super.calculateFontSize(g);
		
		title[0].x = 5;
		title[1].x = title[0].x + 15 + charwidth * 15;
		title[2].x = title[1].x + 15 + charwidth * 8;
	}

	@Override
	public void drawItem(GC g, int index, int left, int top,
			int vcenter, int width, int height) {
		g.drawString(names[index], left + title[0].x, vcenter, true);
		g.drawString(hex[index], left + title[1].x, vcenter, true);
		if (value[index] != null) {
			g.drawString(value[index], left + title[2].x, vcenter, true);
		}
	}
}

class StringList extends TextColumnListWidget {
	private StringTable stringTable;
	
	public StringList(Composite parent, int style, StringTable stringTable) {
		super(parent, SWT.NONE, new String[] {"id", "length", "string"}, stringTable.size());
		
		this.stringTable = stringTable;
		
		addListClickedListener(new OneSelectionListEventListener());
		addKeyListener(new DefaultKeyListener());
	}

	@Override
	public String[] getItem(int index) {
		return new String[] {
				"" + index, 
				"" + stringTable.getLengthOf(index), 
				stringTable.get(index)
		};
	}
}

class TypeList extends TextColumnListWidget {
	private TypeTable typeTable;
	
	public TypeList(Composite parent, int style, TypeTable typeTable) {
		super(parent, style, new String[] {"id", "descriptor"}, typeTable.size());

		this.typeTable = typeTable;

		addListClickedListener(new OneSelectionListEventListener());
		addKeyListener(new DefaultKeyListener());
	}
	
	@Override
	public String[] getItem(int index) {
		return new String[] {
				"" + index, 
				"S" + typeTable.get(index) + ":" + typeTable.getTypeName(index)
		};
	}
}

class ProtoList extends TextColumnListWidget {
	private ProtoTable protoTable;
	
	public ProtoList(Composite parent, int style, ProtoTable protoTable) {
		super(parent, style, new String[] {"id", "shorty", "return", "parameters"}, protoTable.size());
		
		this.protoTable = protoTable;
		
		addListClickedListener(new OneSelectionListEventListener());
		addKeyListener(new DefaultKeyListener());
	}
	
	private String getProtoShortyDescription(Proto proto) {
		return "S" + proto.getShortyIdx() + ":" + proto.getShortyDescriptor();
	}
	
	private String getProtoReturnTypeDescription(Proto proto) {
		return "T" + proto.getReturnTypeIdx() + ":" + proto.getReturnType();
	}
	
	@Override
	public String[] getItem(int index) {
		Proto proto = protoTable.get(index);
		return new String[] {
				"" + index,
				getProtoShortyDescription(proto),
				getProtoReturnTypeDescription(proto),
				"" + proto.getParametersCount()
		};
	}
}

class FieldList extends TextColumnListWidget {
	private FieldTable fieldTable;
	
	public FieldList(Composite parent, int style, FieldTable fieldTable) {
		super(parent, style, new String[] {"id", "class", "type", "name"}, fieldTable.size());
		
		this.fieldTable = fieldTable;

		addListClickedListener(new OneSelectionListEventListener());
		addKeyListener(new DefaultKeyListener());
	}
	
	private String getFieldClassDescription(Field field) {
		return "T" + field.getClassIdx() + ":" + field.getClassTypeName();
	}
	
	private String getFieldTypeDescription(Field field) {
		return "T" + field.getTypeIdx() + ":" + field.getTypeName();
	}
	
	private String getFieldNameDescription(Field field) {
		return "S" + field.getNameIdx() + ":" + field.getName();
	}

	@Override
	public String[] getItem(int index) {
		Field field = fieldTable.get(index);
		return new String[] {
				"" + index,
				getFieldClassDescription(field),
				getFieldTypeDescription(field),
				getFieldNameDescription(field)
		};
	}
}

class MethodList extends TextColumnListWidget {
	private MethodTable methodTable;
	
	public MethodList(Composite parent, int style, MethodTable methodTable) {
		super(parent, style, new String[] {"id", "class", "proto", "name"}, methodTable.size());
		
		this.methodTable = methodTable;
		
		addListClickedListener(new OneSelectionListEventListener());
		addKeyListener(new DefaultKeyListener());
	}
	
	private String getMethodClassDescription(Method method) {
		return "T" + method.getClassIdx() + ":" + method.getClassTypeName();
	}
	
	private String getMethodProtoDescription(Method method) {
		return "P" + method.getProtoIdx() + ":" + method.getProto().getShortyDescriptor();
	}
	
	private String getMethodNameDescription(Method method) {
		return "S" + method.getNameIdx() + ":" + method.getName();
	}

	@Override
	public String[] getItem(int index) {
		Method method = methodTable.get(index);
		return new String[] {
				"" + index,
				getMethodClassDescription(method),
				getMethodProtoDescription(method),
				getMethodNameDescription(method)
		};
	}
}

class ClassInfoTable implements ListWidgetSelectionListener {
	private ClassTable classTable;
	private ClassDef selectedClass;
	
	private ClassList classList;
	private ClassDefList classDef;
	
	private Composite composite;
	
	public Composite getComposite() {
		return composite;
	}
	
	public ClassInfoTable(Composite parent, int style, ClassTable classTable) {
		this.classTable = classTable;
		
		composite = new Composite(parent, style);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		classList = new ClassList(composite, style);
		classList.addListClickedListener(new OneSelectionListEventListener());
		classList.addListSelectionListener(this);
		
		classDef = new ClassDefList(composite, style);
		classDef.addListClickedListener(new OneSelectionListEventListener());
		
		selectedClass = null;
	}
	
	public class ClassList extends TextColumnListWidget {
		public ClassList(Composite parent, int style) {
			super(parent, style, new String[] {"id", "type_id", "name"}, classTable.size());
			
			addKeyListener(new DefaultKeyListener());
		}
		
		@Override
		public String[] getItem(int index) {
			ClassDef def = classTable.getClassByClassId(index);
			
			return new String[] {
					"" + index,
					"" + def.getClassTypeId(),
					def.getClassTypeName()
			};
		}
	}
	
	private void updateSelection(ClassDef newSelection) {
		this.selectedClass = newSelection;
		
		classDef.updateSelection();
	}
	
	public class ClassDefList extends ListWidget {
		protected ListTitleItem[] title;
		
		public ClassDefList(Composite parent, int style) {
			super(parent, style);
			
			title = new ListTitleItem[2];
			title[0] = new ListTitleItem(5, "name");
			title[1] = new ListTitleItem(5, "value");
			setTitle(title);
			
			addKeyListener(new DefaultKeyListener());
		}
		
		@Override
		public void calculateFontSize(GC g) {
			super.calculateFontSize(g);
			
			title[0].x = 5;
			title[1].x = title[0].x + 15 + charwidth * 17;
		}
		
		private String stringifiedAccessFlags;
		private int interfaces[] = null;
		private encoded_field static_fields[] = null;
		private encoded_field instance_fields[] = null;
		private encoded_method direct_methods[] = null;
		private encoded_method virtual_methods[] = null;
		
		public void updateSelection() {
			if (selectedClass == null) {
				setListSize(0);
			} else {
				interfaces = selectedClass.getInterfaceTypeIds();

				class_data_item data = selectedClass.class_data();
				
				static_fields = data.static_fields();
				assert static_fields.length == data.static_fields_size();
				
				instance_fields = data.instance_fields();
				assert instance_fields.length == data.instance_fields_size();
				
				direct_methods = data.direct_methods();
				assert direct_methods.length == data.direct_methods_size();
				
				virtual_methods = data.virtual_methods();
				assert virtual_methods.length == data.virtual_methods_size();
				
				setListSize(3 + Math.max(1, interfaces.length) + 1 + 
						Math.max(1, static_fields.length) +
						Math.max(1, instance_fields.length) + 
						Math.max(1, direct_methods.length) +
						Math.max(1, virtual_methods.length));
				stringifiedAccessFlags = DexAccessFlags.stringifyAccessFlagsForClasses(selectedClass.getAccessFlags());
			}
			this.clearHighlights();
			redraw();
		}

		@Override
		public void drawItem(GC g, int index, int left, int top,
				int vcenter, int width, int height) {
			if (index == 0) {
				g.drawString("class_idx", left + title[0].x, vcenter, true);
				g.drawString(selectedClass.getClassTypeId() + " " + selectedClass.getClassTypeName(), left + title[1].x, vcenter, true);
				return;
			}
			if (index == 1) {
				g.drawString("access_flags", left + title[0].x, vcenter, true);
				g.drawString(selectedClass.getAccessFlags() + " " + stringifiedAccessFlags, left + title[1].x, vcenter, true);
				return;
			}
			if (index == 2) {
				g.drawString("superclass_idx", left + title[0].x, vcenter, true);
				g.drawString(selectedClass.getSuperclassTypeId() + " " + selectedClass.getSuperclassName(), left + title[1].x, vcenter, true);
				return;
			}
			if (index == 3) {
				g.drawString("interfaces", left + title[0].x, vcenter, true);
			}
			if (interfaces.length > 0) {
				if (index < 3 + interfaces.length) {
					g.drawString(interfaces[index - 3] + " " + classTable.getTypeTable().getTypeName(interfaces[index - 3]), left + title[1].x, vcenter, true);
					return;
				}
				index -= (3 + interfaces.length);
			} else {
				if (index == 3) {
					return;
				}
				index -= 4;
			}
			if (index == 0) {
				g.drawString("source_file_idx", left + title[0].x, vcenter, true);
				g.drawString("" + selectedClass.getSourceFileNameStringIdx() + " " + selectedClass.getSourceFileName(), left + title[1].x, vcenter, true);
				return;
			}
			// annotation passed
			
			// static_fields
			if (index == 1) {
				g.drawString("static_fields", left + title[0].x, vcenter, true);
			}
			if (static_fields.length > 0) {
				if (index < 1 + static_fields.length) {
					int fieldId = 0;
					
					for (int i = 0; i <= index - 1; i++) {
						fieldId += static_fields[i].field_idx_diff();
					}
					g.drawString(DexAccessFlags.stringifyAccessFlagsForFields(static_fields[index - 1].access_flags()) + " " + classTable.getFieldTable().get(fieldId).getName() + " : " + classTable.getFieldTable().get(fieldId).getTypeName() + " (" + fieldId + ")", left + title[1].x, vcenter, true);
					return;
				}
				index -= (1 + static_fields.length);
			} else {
				if (index == 1) {
					return;
				}
				index -= 2;
			}
			
			// instance_fields
			if (index == 0) {
				g.drawString("instance_fields", left + title[0].x, vcenter, true);
			}
			if (instance_fields.length > 0) {
				if (index < instance_fields.length) {
					int fieldId = 0;
					
					for (int i = 0; i <= index; i++) {
						fieldId += instance_fields[i].field_idx_diff();
					}
					g.drawString(DexAccessFlags.stringifyAccessFlagsForFields(instance_fields[index].access_flags()) + " " + classTable.getFieldTable().get(fieldId).getName() + " : " + classTable.getFieldTable().get(fieldId).getTypeName() + " (" + fieldId + ")", left + title[1].x, vcenter, true);
					return;
				}
				index -= (instance_fields.length);
			} else {
				if (index == 0) {
					return;
				}
				index -= 1;
			}
			
			// direct_methods
			if (index == 0) {
				g.drawString("direct_methods", left + title[0].x, vcenter, true);
			}
			if (direct_methods.length > 0) {
				if (index < direct_methods.length) {
					int methodId = 0;
					
					for (int i = 0; i <= index; i++) {
						methodId += direct_methods[i].method_idx_diff();
					}
					g.drawString(DexAccessFlags.stringifyAccessFlagsForMethods(direct_methods[index].access_flags()) + " " + classTable.getMethodTable().get(methodId).getName() + " : " + classTable.getMethodTable().get(methodId).getProto().getShortyDescriptor() + " (" + methodId + ")", left + title[1].x, vcenter, true);
					return;
				}
				index -= (direct_methods.length);
			} else {
				if (index == 0) {
					return;
				}
				index -= 1;
			}
			
			// virtual_methods
			if (index == 0) {
				g.drawString("virtual_methods", left + title[0].x, vcenter, true);
			}
			if (virtual_methods.length > 0) {
				if (index < virtual_methods.length) {
					int methodId = 0;
					
					for (int i = 0; i <= index; i++) {
						methodId += virtual_methods[i].method_idx_diff();
					}
					g.drawString(DexAccessFlags.stringifyAccessFlagsForMethods(virtual_methods[index].access_flags()) + " " + classTable.getMethodTable().get(methodId).getName() + " : " + classTable.getMethodTable().get(methodId).getProto().getShortyDescriptor() + " (" + methodId + ")", left + title[1].x, vcenter, true);
					return;
				}
				index -= (virtual_methods.length);
			} else {
				if (index == 0) {
					return;
				}
				index -= 1;
			}
		}
	}
	
	@Override
	public void itemSelected(ListWidget widget, int index) {
		if (! classList.isHighlighted(index)){
			updateSelection(null);
		} else {
			updateSelection(classTable.getClassByClassId(index));
		}
	}
}
