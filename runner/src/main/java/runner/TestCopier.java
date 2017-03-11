package runner;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class TestCopier implements FileVisitor<Path> {
	private final Path source;
	private final Path target;

	TestCopier(Path source, Path target) {
		this.source = source;
		this.target = target;
	}

	static void copyFile(Path source, Path target) {
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			Files.delete(source);
		} catch (IOException x) {
			System.err.format("Unable to copy: %s: %s%n", source, x);
		}
	}

	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		// before visiting entries in a directory we copy the directory
		// (okay if directory already exists).
		CopyOption[] options = new CopyOption[0];

		Path newdir = target.resolve(source.relativize(dir));
		try {
			Files.copy(dir, newdir, options);
		} catch (FileAlreadyExistsException x) {
			// ignore
		} catch (IOException x) {
			System.err.format("Unable to create: %s: %s%n", newdir, x);
			return FileVisitResult.SKIP_SUBTREE;
		}
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		if (file.getFileName().toString().contains("Test")) {
			copyFile(file, target.resolve(source.relativize(file)));
		}
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		if (exc instanceof FileSystemLoopException) {
			System.err.println("cycle detected: " + file);
		} else {
			System.err.format("Unable to copy: %s: %s%n", file, exc);
		}
		return FileVisitResult.CONTINUE;
	}
}
