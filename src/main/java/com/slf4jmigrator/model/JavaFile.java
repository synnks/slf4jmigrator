package com.slf4jmigrator.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JavaFile {

	private final HashMap<Category, Set<Line>> lineMap;

	public JavaFile() {
		lineMap = new HashMap<>();
		Arrays.stream(Category.values())
				.forEach(value -> lineMap.put(value, new TreeSet<>(Comparator.comparing(Line::getLineNumber))));
	}

	public JavaFile(JavaFile otherFile) {
		lineMap = new HashMap<>(otherFile.lineMap);
	}

	public Set<Line> get(Category category) {
		return lineMap.get(category);
	}

	public void putInside(Line line) {
		lineMap.get(matchLine(line)).add(line);
	}

	public void removeFromInside(Line line) {
		final Set<Line> lines = lineMap.get(matchLine(line));
		lines.remove(line);

		changeLinesOverIndex(line.getLineNumber(), Line::decrementLineNumber);

		if (lines.isEmpty()) {
			lineMap.values().stream()
					.flatMap(Set::stream)
					.filter(l -> l.getLineNumber() == line.getLineNumber() - 1 && matchLine(l).equals(Category.Empty))
					.findFirst()
					.ifPresent(this::removeFromInside);
		}
	}

	public void replace(String string, int lineNumber) {
		final Optional<Line> lineToBeReplaced = lineMap.values().stream()
				.flatMap(Set::stream)
				.filter(l -> l.getLineNumber() == lineNumber)
				.findFirst();

		lineToBeReplaced.ifPresent(line -> line.setContent(string));
	}

	public void insertAlphabetically(String string) {
		final Category insertCategory = Category.match(string);
		final Set<Line> lines = lineMap.get(insertCategory);

		// If the bucket is empty, it has to be created
		if (lines.isEmpty()) {
			// Get the first previous bucket that is not empty
			// in order to get the number of the last line
			getFirstNonEmptyBucketBefore(insertCategory).ifPresent(previousNonEmptyBucket -> {
				final int lastLineNumber = getLastLineOfBucket(previousNonEmptyBucket).getLineNumber();
				// Increment all the lineMap following the last line before the insert
				// by an amount equal to empty lineMap to be inserted + 1
				changeLinesOverIndex(lastLineNumber, line -> line.incrementLineNumber(2));

				// Add a number of empty lineMap before the new line
				final Set<Line> emptyLines = lineMap.get(Category.Empty);
				emptyLines.add(new Line(lastLineNumber + 1, ""));
				lines.add(new Line(lastLineNumber + 2, string));
			});
		} else {
			// Retrieve the number of the first line the bucket
			// in case the new line to be inserted has to be first
			lines.stream()
					.mapToInt(Line::getLineNumber)
					.min()
					.ifPresent(firstLine -> {
						// Get the number of the last line alphabetically before the new line
						final int lineNumber = lines.stream()
								.filter(line -> line.getContent().compareTo(string) < 0)
								.mapToInt(Line::getLineNumber)
								.max().orElse(firstLine);

						// Increment all lineMap by only 1 since there are no empty lineMap being added
						changeLinesOverIndex(lineNumber, Line::incrementLineNumber);
						lines.add(new Line(lineNumber + 1, string));
					});
		}
	}

	public Stream<String> reconstructFile() {
		return lineMap.values().stream()
				.flatMap(Set::stream)
				.sorted(Comparator.comparingInt(Line::getLineNumber))
				.map(Line::getContent);
	}

	private void changeLinesOverIndex(int index, Consumer<Line> action) {
		lineMap.values().stream()
				.flatMap(Set::stream)
				.filter(line -> line.getLineNumber() > index)
				.forEach(action);
	}

	private Category matchLine(Line line) {
		return Category.match(line.getContent());
	}

	private Optional<Category> getFirstNonEmptyBucketBefore(Category referenceCategory) {
		Category currentCategory = referenceCategory;
		do {
			currentCategory = currentCategory.getPrevious();
		} while (currentCategory != null && lineMap.get(currentCategory).isEmpty());
		return Optional.ofNullable(currentCategory);
	}

	private Line getLastLineOfBucket(Category category) {
		return lineMap.get(category).stream()
				.max(Comparator.comparingInt(Line::getLineNumber))
				.orElseThrow(() -> new RuntimeException("Category cannot be empty"));
	}
}
