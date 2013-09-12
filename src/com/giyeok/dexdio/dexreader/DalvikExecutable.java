/**
 * @author Joonsoo<joonsoo.jeon@gmail.com>
 * Dalvik Executable reader based on the format guided in 
 * http://source.android.com/tech/dalvik/dex-format.html
 */

package com.giyeok.dexdio.dexreader;

import java.io.IOException;

import com.giyeok.dexdio.dexreader.structs.header_item;

public class DalvikExecutable {
	private String filepath;

	private DalvikExecutable(String filepath) {
		this.filepath = filepath;
	}
	
	public String getFilepath() {
		return filepath;
	}
	
	public static DalvikExecutable load(String filepath) throws IOException {
		DalvikExecutable dex = new DalvikExecutable(filepath);
		
		if (dex.load(new EndianRandomAccessFile(filepath, "r"))) {
			return dex;
		} else {
			return null;
		}
	}
	
	private header_item header;
	private StringTable stringTable;
	private TypeTable typeTable;
	private ProtoTable protoTable;
	private FieldTable fieldTable;
	private MethodTable methodTable;
	private ClassTable classTable;
	
	public header_item getHeader() {
		return header;
	}
	
	public StringTable getStringTable() {
		return stringTable;
	}
	
	public TypeTable getTypeTable() {
		return typeTable;
	}
	
	public ProtoTable getProtoTable() {
		return protoTable;
	}
	
	public FieldTable getFieldTable() {
		return fieldTable;
	}
	
	public MethodTable getMethodTable() {
		return methodTable;
	}
	
	public ClassTable getClassTable() {
		return classTable;
	}
	
	private boolean load(EndianRandomAccessFile file) throws IOException {
		file.seek(0);
		file.setEndian(false);
		
		header = new header_item();
		
		header.read(file);
		
		if (header.magic().equals(DEX_FILE_MAGIC)) {
			System.out.println("Magic number is invalid");
			return false;
		}
		header.checksum();		// TODO implement validation
		header.signature();		// TODO implement validation
		if (header.file_size() != file.length()) {
			System.out.println("File size is invalid");
			return false;
		}
		if (header.header_size() != 0x70) {
			System.out.println("Header size is invalid");
			return false;
		}
		if (header.endian_tag() != ENDIAN_CONSTANT) {
			System.out.println("Invalid endian tag");
			return false;
		}
		
		stringTable = new StringTable();
		if (! (stringTable.loadStrings(header, file))) {
			System.out.println("Broken or unsupported string table");
			return false;
		}
		
		typeTable = new TypeTable(stringTable);
		if (! (typeTable.loadTypes(header, file))) {
			System.out.println("Broken or unsupported type table");
			return false;
		}
		
		protoTable = new ProtoTable(stringTable, typeTable);
		if (! (protoTable.loadProtos(header, file))) {
			System.out.println("Broken or unsupported proto table");
			return false;
		}
		
		fieldTable = new FieldTable(stringTable, typeTable);
		if (! fieldTable.loadFields(header, file)) {
			System.out.println("Broken or unsupported field table");
			return false;
		}
		
		methodTable = new MethodTable(stringTable, typeTable, protoTable);
		if (! methodTable.loadMethods(header, file)) {
			System.out.println("Broken or unsupported method table");
			return false;
		}
		
		classTable = new ClassTable(stringTable, typeTable, protoTable, fieldTable, methodTable);
		if (! classTable.loadClasses(header, file)) {
			System.out.println("Broken or unsupported class table");
			return false;
		}
		
		return true;
	}
	
	
	private final long ENDIAN_CONSTANT = 0x12345678;
	private final byte[] DEX_FILE_MAGIC = { 0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00 };
}


// map_list
// map_item
