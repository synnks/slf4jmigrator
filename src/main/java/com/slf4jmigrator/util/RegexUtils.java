package com.slf4jmigrator.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.slf4jmigrator.util.Constants.LEVEL_MAPPINGS;
import static com.slf4jmigrator.util.StringUtils.capitalize;

public final class RegexUtils {

	public static final Pattern loggerDeclarationPattern =
			Pattern.compile("(.*[\\s]+)Logger\\.getLogger\\((\\w+\\.class)(?:\\.\\w+\\(.*\\))?\\);(.*)");
	public static final Pattern isLoggableCallPattern =
			Pattern.compile("(.*[\\s(]+)(\\w+)\\.isLoggable\\((?:Level\\.)?([A-Z]+)\\)(.*)");
	public static final Pattern levelLoggingCallPattern =
			Pattern.compile("(.*[\\s]+)(\\w+)\\.log\\((?:Level\\.)?([A-Z]+), (.*)\\);(.*)");
	public static final Pattern JULLoggingCallPattern =
			Pattern.compile("(.*[\\s]+)(\\w+)\\.(fine(?:r|st)?|config|info|warning|severe)\\((.*)\\);(.*)");
	public static final Pattern formattedLoggingCallPattern =
			Pattern.compile("(.*[\\s]+)(\\w+)\\.(info|warn|trace|debug|error)\\((\".*\\{.+}.*\"), (?:new \\w+\\[]\\{)?([\\w., ()]+)(?:})?\\);(.*)");
	public static final Pattern stringConcatenationLoggingCallPattern =
			Pattern.compile("(.*[\\s]+)(\\w+)\\.(info|warn|trace|debug|error)\\((.*\\+.*)\\);(.*)");
	public static final Pattern stringFormatLoggingCallPattern =
			Pattern.compile("(.*[\\s]+)(\\w+)\\.(info|warn|trace|debug|error)\\(.*(?:String\\.)?format\\((.*)\\).*\\);(.*)");

	public static final Function<Matcher, String> loggerDeclarationMapper =
			matcher -> String.format("LoggerFactory.getLogger(%s);",
									 matcher.group(2));

	public static final Function<Matcher, String> isLoggableCallMapper =
			matcher -> String.format("%s.is%sEnabled()",
									 matcher.group(2),
									 capitalize(LEVEL_MAPPINGS.get(matcher.group(3))));

	public static final Function<Matcher, String> levelLoggingCallMapper =
			matcher -> String.format("%s.%s(%s);",
									 matcher.group(2),
									 LEVEL_MAPPINGS.get(matcher.group(3)),
									 matcher.group(4));

	public static final Function<Matcher, String> JULLoggingCallMapper =
			matcher -> String.format("%s.%s(%s);",
									 matcher.group(2),
									 LEVEL_MAPPINGS.get(matcher.group(3).toUpperCase()),
									 matcher.group(4));

	public static final Function<Matcher, String> formattedLoggingCallMapper =
			matcher -> String.format("%s.%s(%s, %s);",
									 matcher.group(2),
									 matcher.group(3),
									 matcher.group(4).replaceAll("\\{\\d+}", "\\{}"),
									 matcher.group(5));

	public static final Function<Matcher, String> stringConcatenationLoggingCallMapper =
			matcher -> String.format("%s.%s(%s);",
									 matcher.group(2),
									 matcher.group(3),
									 Formatter.formatWithParameters(matcher.group(4)));

	public static final Function<Matcher, String> stringFormatLoggingCallMapper =
			matcher -> String.format("%s.%s(%s);",
									 matcher.group(2),
									 matcher.group(3),
									 matcher.group(4)
											 .replaceAll("%n", "\\\n")
											 .replaceAll("%[a-zA-Z]", "{}"));

	private static class Formatter {

		private static final String parameterRegex = ".*[^\"]$";
		private static final String tokenDelimiter = " ?\\+ ?";
		private static final String parameterDelimiter = ", ";

		static String formatWithParameters(String string) {
			final String[] tokens = string.split(tokenDelimiter);
			if (tokens.length == 1) {
				return tokens[0];
			}
			final StringBuilder stringBuilder = new StringBuilder("\"");
			final List<String> parameters = new ArrayList<>();
			for (int i = 0; i < tokens.length; i++) {
				final String currentToken = tokens[i];

				if (currentToken.matches(parameterRegex)) {
					parameters.add(currentToken);
					if (i < tokens.length - 1) {
						final String nextToken = tokens[i + 1];
						if (!nextToken.matches(parameterRegex)) {
							stringBuilder.append("{}");
						}
					}
				} else {
					stringBuilder.append(currentToken.replaceAll("(?:\\\\)?\"", ""));
					if (i < tokens.length - 1) {
						final String nextToken = tokens[i + 1];
						if (nextToken.matches(parameterRegex)) {
							stringBuilder.append("{}");
						}
					}
				}
			}
			stringBuilder.append("\"");
			parameters.forEach(parameter -> stringBuilder.append(parameterDelimiter).append(parameter));
			return stringBuilder.toString().replaceAll("(\\{}){2,}", "{}");
		}
	}
}
