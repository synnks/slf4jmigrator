package com.slf4jmigrator.util;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

	public static final Map<String, String> LEVEL_MAPPINGS = new HashMap<>();
	public static final String JDK_LOGGING_PACKAGE = "java.util.logging";
	public static final String LOMBOK_LOGGING_PACKAGE = "lombok.extern";
	public static final String LOMBOK_SLF4J_ANNOTATION = "@Slf4j";
	public static final String LOMBOK_IMPORT = "import lombok.extern.slf4j.Slf4j;";
	public static final String[] SLF4J_IMPORTS = new String[]{"import org.slf4j.Logger;",
															  "import org.slf4j.LoggerFactory;"};
	public static final String[] LOMBOK_ANNOTATIONS = new String[]{"@Log", "@Log4j", "@Log4j2", "@Slf4j", "@XSlf4j"};

	static {
		LEVEL_MAPPINGS.put("FINEST", "trace");
		LEVEL_MAPPINGS.put("FINER", "debug");
		LEVEL_MAPPINGS.put("FINE", "debug");
		LEVEL_MAPPINGS.put("CONFIG", "info");
		LEVEL_MAPPINGS.put("INFO", "info");
		LEVEL_MAPPINGS.put("WARNING", "warn");
		LEVEL_MAPPINGS.put("SEVERE", "error");
	}
}
