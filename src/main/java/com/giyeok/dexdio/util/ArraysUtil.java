package com.giyeok.dexdio.util;

import java.util.Arrays;


public class ArraysUtil {
	
	public static <T> boolean existsExact(T[] array, T target) {
		return indexOfExact(array, target) >= 0;
	}

	public static <T> int indexOf(T[] array, T target) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(target)) {
				return i;
			}
		}
		return -1;
	}

	public static <T> int indexOfExact(T[] array, T target) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == target) {
				return i;
			}
		}
		return -1;
	}

	public static <T> T[] concat(T[] array1, T[] array2) {
		T[] newarray = Arrays.copyOf(array1, array1.length + array2.length);
		
		for (int i = 0; i < array2.length; i++) {
			newarray[array1.length + i] = array2[i];
		}
		return newarray;
	}
}
