package com.slf4jmigrator.util;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

	private static final Pattern commentPattern = Pattern.compile("^\\s+\\*|(.*)[/*]{2,}.*");

	public static String capitalize(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	public static int nonCommentedCharacterCount(String string, char chr) {
		final Matcher matcher = commentPattern.matcher(string);
		String substring = string;

		if (matcher.find() && characterCount(matcher.group(1), '"') % 2 == 0) {
			substring = matcher.group(1);
		}

		return nonStringCharacterCount(substring, chr);
	}

	public static int nonStringCharacterCount(String string, char chr) {
		return characterCount(string, chr, str -> characterCount(str, '"') % 2 == 0);
	}

	public static int characterCount(String string, char chr) {
		return characterCount(string, chr, str -> true);
	}

	private static int characterCount(String string, char chr, Predicate<String> predicate) {
		int count = 0;
		if (string != null) {
			char[] charArray = string.toCharArray();
			for (int i = 0; i < charArray.length; i++) {
				if (charArray[i] == chr) {
					if (predicate.test(string.substring(0, i))) {
						count++;
					}
				}
			}
		}
		return count;
	}
}
