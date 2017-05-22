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

public class SiteDeleter implements FileVisitor<Path> {

	SiteDeleter() {
	}

	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		String fname = file.getFileName().toString();
		if (fname.contains("html") || fname.contains("css") || fname.contains("png") || fname.contains("js")) {
			try {
				Files.delete(file);
			} catch (IOException e) {
				System.err.format("Unable to delete: %s%n", fname);
			}
		}
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		try {
			Files.delete(dir);
		} catch (IOException e) {
			System.err.format("Unable to delete: %s%n", dir);
		}
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		if (exc instanceof FileSystemLoopException) {
			System.err.println("cycle detected: " + file);
		} else {
			System.err.format("Unable to delete: %s: %s%n", file, exc);
		}
		return FileVisitResult.CONTINUE;
	}
}
