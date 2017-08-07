package com.slf4jmigrator.util;

public final class StringUtils {

	public static String capitalize(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	public static int characterCountBeforeExpression(String string, char chr, String regex) {
		int count = 0;
		if (!string.matches(regex)) {
			for (char c : string.split(regex)[0].toCharArray()) {
				if (c == chr) {
					count++;
				}
			}
		}
		return count;
	}
}
