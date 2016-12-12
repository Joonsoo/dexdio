package com.giyeok.dexdio.dexreader;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.structs.header_item;
import com.giyeok.dexdio.dexreader.structs.proto_id_item;
import com.giyeok.dexdio.dexreader.structs.type_list;
import com.giyeok.dexdio.dexreader.value.Array;

public class ProtoTable {
	private StringTable stringTable;
	private TypeTable typeTable;

	public ProtoTable(StringTable stringTable, TypeTable typeTable) {
		this.stringTable = stringTable;
		this.typeTable = typeTable;
	}
	
	public class Proto {
		private int shorty_idx;
		private int return_type_idx;
		private int[] parameters;
		
		public Proto(int shorty_idx, int return_type_idx, int[] parameters) {
			this.shorty_idx = shorty_idx;
			this.return_type_idx = return_type_idx;
			this.parameters = parameters;
		}
		
		public int getShortyIdx() {
			return shorty_idx;
		}
		
		public String getShortyDescriptor() {
			return stringTable.get(shorty_idx);
		}
		
		public int getReturnTypeIdx() {
			return return_type_idx;
		}
		
		public String getReturnType() {
			return typeTable.getTypeName(return_type_idx);
		}
		
		public int getParametersCount() {
			if (parameters != null) {
				return parameters.length;
			}
			return 0;
		}
		
		public int getParameter(int i) {
			return parameters[i];
		}
	}

	private Proto[] protoTable;
	
	public int size() {
		return protoTable.length;
	}
	
	public Proto get(int i) {
		return protoTable[i];
	}

	boolean loadProtos(header_item header, EndianRandomAccessFile file) throws IOException {
		int proto_ids_size = (int) header.proto_ids_size();
		long proto_ids_off = header.proto_ids_off();

		file.seek(proto_ids_off);
		
		Array proto_ids = new Array(proto_id_item.class, proto_ids_size);
		protoTable = new Proto[proto_ids_size];
		
		proto_ids.read(file);
		type_list type_list = new type_list();
		for (int i = 0; i < proto_ids_size; i++) {
			proto_id_item proto_id_item = ((proto_id_item) proto_ids.item(i));
			/*
			System.out.println(stringTable.get(((Int) proto_id_item.find("shorty_idx")).getValue()));
			System.out.println(typeTable.getTypeName(((Int) proto_id_item.find("return_type_idx")).getValue()));
			System.out.println(((Int) proto_id_item.find("parameters_off")).getUnsignedValue());
			*/
			
			int parameters[];
			long parameters_off = proto_id_item.parameters_off();
			if (parameters_off > 0) {
				file.seek(parameters_off);
				
				type_list.read(file);
				if (type_list.size() < 0) {
					System.out.println("There exists too long string");
					return false;
				}

				parameters = type_list.asIntArray();
			} else {
				parameters = null;
			}
			protoTable[i] = new Proto((int) proto_id_item.shorty_idx(), 
					(int) proto_id_item.return_type_idx(), parameters);
		}
		return true;
	}
}
