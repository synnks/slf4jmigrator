package com.slf4jmigrator;

import com.slf4jmigrator.model.Category;
import com.slf4jmigrator.model.JavaFile;
import com.slf4jmigrator.model.Line;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.slf4jmigrator.util.Constants.*;
import static com.slf4jmigrator.util.RegexUtils.*;

public class Migrator {

	public static Stream<String> processFile(JavaFile file) {
		final JavaFile newFile = new JavaFile(file);
		final Set<Line> loggingCalls = newFile.get(Category.LoggingCalls);
		final Set<Line> isLoggableCalls = newFile.get(Category.JULIsLoggableCalls);

		if (loggingCalls.size() > 0) {

			handleImports(file);
			shrinkLines(file);

			isLoggableCalls.forEach(call -> resolve(newFile, call, isLoggableCallPattern, isLoggableCallMapper));

			loggingCalls.forEach(call -> {
				resolve(newFile, call, levelLoggingCallPattern, levelLoggingCallMapper);
				resolve(newFile, call, JULLoggingCallPattern, JULLoggingCallMapper);
				resolve(newFile, call, formattedLoggingCallPattern, formattedLoggingCallMapper);
				resolve(newFile, call, stringConcatenationLoggingCallPattern, stringConcatenationLoggingCallMapper);
			});
		}
		return newFile.reconstructFile();
	}

	private static void resolve(JavaFile file, Line line, Pattern pattern, Function<Matcher, String> mapper) {
		final Matcher matcher = pattern.matcher(line.getContent());
		if (matcher.find()) {
			final String prefix = matcher.group(1);
			final String suffix = matcher.group(matcher.groupCount());
			final String newLine = mapper.apply(matcher);
			file.replace(prefix + newLine + suffix, line.getLineNumber());
		}
	}

	private static void handleImports(JavaFile file) {
		final Set<Line> thirdPartyImports = file.get(Category.ThirdPartyImports);
		final Set<Line> JDKImports = file.get(Category.JDKImports);
		final Set<Line> staticImports = file.get(Category.StaticImports);
		final Set<Line> annotations = file.get(Category.Annotations);
		final Set<Line> loggerDeclarations = file.get(Category.JULLoggerDeclarations);

		final Set<Line> toRemove = new HashSet<>();
		final TriConsumer<Collection<Line>, Set<Line>, Predicate<String>> addToRemovalQueue =
				(queue, set, predicate) -> set.stream()
						.filter(line -> predicate.test(line.getContent()))
						.forEach(queue::add);

		if (annotations.stream()
				.map(Line::getContent)
				.anyMatch(element -> Arrays.stream(LOMBOK_ANNOTATIONS)
						.anyMatch(element::equals))) {

			Arrays.stream(LOMBOK_ANNOTATIONS)
					.forEach(annotation -> addToRemovalQueue.accept(toRemove, annotations, line -> line.contains(annotation)));
			addToRemovalQueue.accept(toRemove, thirdPartyImports, line -> line.contains(LOMBOK_LOGGING_PACKAGE));

			file.insertAlphabetically(LOMBOK_IMPORT);
			file.insertAlphabetically(LOMBOK_SLF4J_ANNOTATION);
		} else {
			Arrays.stream(SLF4J_IMPORTS).forEach(file::insertAlphabetically);
			loggerDeclarations.forEach(line -> resolve(file, line, loggerDeclarationPattern, loggerDeclarationMapper));
		}

		addToRemovalQueue.accept(toRemove, JDKImports, line -> line.contains(JDK_LOGGING_PACKAGE));
		addToRemovalQueue.accept(toRemove, staticImports, line -> line.contains(JDK_LOGGING_PACKAGE));
		toRemove.forEach(file::removeFromInside);
	}

	private static void shrinkLines(JavaFile file) {
		final Set<Line> loggingCalls = file.get(Category.LoggingCalls);
		final Set<Line> toRemove = new HashSet<>();
		loggingCalls.forEach(call -> toRemove.addAll(shrinkLine(call)));
		toRemove.forEach(file::removeFromInside);
	}

	private static Collection<Line> shrinkLine(Line line) {
		final Set<Line> toRemove = new HashSet<>();
		shrinkLine(toRemove, line);
		return toRemove;
	}

	private static Line shrinkLine(Collection<Line> toRemove, Line line) {
		if (line.isMultiLine()) {
			final Line nextLine = shrinkLine(toRemove, line.getNextLine());
			line.setContent(line.getContent() + nextLine.getContent()
					.replaceAll("\\t", "")
					.replaceAll(" {2,}", " "));
			line.setNextLine(null);
			toRemove.add(nextLine);
		}
		return line;
	}

	@FunctionalInterface
	private interface TriConsumer<T, U, V> {

		void accept(T t, U u, V v);
	}
}
