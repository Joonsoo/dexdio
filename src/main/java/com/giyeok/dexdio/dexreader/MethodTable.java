package com.giyeok.dexdio.dexreader;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.structs.header_item;
import com.giyeok.dexdio.dexreader.structs.method_id_item;
import com.giyeok.dexdio.dexreader.value.Array;

public class MethodTable {
	private StringTable stringTable;
	private TypeTable typeTable;
	private ProtoTable protoTable;

	public MethodTable(StringTable stringTable, TypeTable typeTable, ProtoTable protoTable) {
		this.stringTable = stringTable;
		this.typeTable = typeTable;
		this.protoTable = protoTable;
	}

	public class Method {
		private int class_idx;
		private int proto_idx;
		private int name_idx;

		public Method(int class_idx, int proto_idx, int name_idx) {
			this.class_idx = class_idx;
			this.proto_idx = proto_idx;
			this.name_idx = name_idx;
		}

		public int getClassIdx() {
			return class_idx;
		}

		public String getClassTypeName() {
			return typeTable.getTypeName(class_idx);
		}

		public int getProtoIdx() {
			return proto_idx;
		}

		public ProtoTable.Proto getProto() {
			return protoTable.get(proto_idx);
		}

		public int getNameIdx() {
			return name_idx;
		}

		public String getName() {
			return stringTable.get(name_idx);
		}
	}

	private Method[] methodTable;

	public int size() {
		return methodTable.length;
	}

	public Method get(int i) {
		return methodTable[i];
	}

	boolean loadMethods(header_item header, RandomAccessible file) throws IOException {
		int method_ids_size = (int) header.method_ids_size();
		long method_ids_off = header.method_ids_off();

		file.seek(method_ids_off);

		Array method_ids = new Array(method_id_item.class, method_ids_size);
		methodTable = new Method[method_ids_size];

		method_ids.read(file);
		for (int i = 0; i < method_ids_size; i++) {
			method_id_item method_id_item = (method_id_item) method_ids.item(i);

			methodTable[i] = new Method(method_id_item.class_idx(), method_id_item.proto_idx(),
					(int) method_id_item.name_idx());
			// System.out.println(methodTable[i].getClassTypeName() + "." +
			// methodTable[i].getName());
			// System.out.println(methodTable[i].getProto().getShortyDescriptor());
		}
		return true;
	}
}
