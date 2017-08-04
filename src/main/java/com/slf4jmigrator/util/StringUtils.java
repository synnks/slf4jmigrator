package com.slf4jmigrator.util;

public final class StringUtils {

	public static String capitalize(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	public static int characterFrequency(String string, char chr) {
		int count = 0;
		for (char c : string.toCharArray()) {
			if (c == chr) {
				count++;
			}
		}
		return count;
	}
}
