package com.slf4jmigrator.model;

import java.util.Arrays;

public enum Category {
	Empty("\\s*", null),
	Package("package \\w+[._a-zA-Z0-9]*;", null),
	ThirdPartyImports("import (?!static )(?!java.+).+;", Package),
	JDKImports("import (?!static )java.*;", ThirdPartyImports),
	StaticImports("import static .*;", JDKImports),
	Annotations("@\\w+(?:\\(.*\\))?", StaticImports),
	ClassName("public class .* \\{", Annotations),
	JULLoggerDeclarations("\\s*(?:public |protected |private )?(?:final )?(?:static )?(?:final )?Logger \\w+ = .*\\(.*", null),
	JULIsLoggableCalls("\\s*.*\\w+\\.isLoggable\\(.*\\).*", null),
	LoggingCalls("\\s*.*\\w+\\.(log|fine(?:r|st)?|config|info|warn(?:ing)?|severe|trace|debug|error)\\(.*", null),
	Other(".*", null);

	private final String regex;
	private final Category previous;

	Category(String regex, Category previous) {
		this.regex = regex;
		this.previous = previous;
	}

	public static Category match(String string) {
		return Arrays.stream(values())
				.filter(val -> string.matches(val.regex))
				.findFirst().orElse(Other);
	}

	public String getRegex() {
		return regex;
	}

	public Category getPrevious() {
		return previous;
	}
}
