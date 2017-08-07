package com.slf4jmigrator.util;

import com.slf4jmigrator.model.JavaFile;
import com.slf4jmigrator.model.Line;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

import static com.slf4jmigrator.util.StringUtils.nonCommentedCharacterCount;

public final class FileLoader {

	public static Stream<File> loadAll(Path path, String extension) {
		try {
			return Files.find(path, 255, (p, bfa) -> bfa.isRegularFile() && p.toString().endsWith(extension))
					.map(Path::toFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Stream.empty();
	}

	public static JavaFile readFile(File file) {
		final JavaFile javaFile = new JavaFile();

		int parenthesisBalance = 0;
		Line previousLine;
		Line currentLine = null;

		try {
			final Scanner scanner = new Scanner(file);
			for (int index = 1; scanner.hasNextLine(); index++) {
				final String content = scanner.nextLine();
				previousLine = currentLine;
				currentLine = new Line(index, content);

				if (parenthesisBalance != 0) {
					previousLine.setNextLine(currentLine);
				}
				parenthesisBalance += nonCommentedCharacterCount(content, '(');
				parenthesisBalance -= nonCommentedCharacterCount(content, ')');
				javaFile.putInside(currentLine);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return javaFile;
	}
}
