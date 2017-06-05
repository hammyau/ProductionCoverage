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
import java.util.ArrayList;
import java.util.List;

public class TestCounter implements FileVisitor<Path> {
	private final Path target;
	
	private List<String> tests = new ArrayList<String>();

	TestCounter(Path target) {
		this.target = target;
	}


	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		if (file.getFileName().toString().contains("Test")) {
			tests.add(file.toString());
		}
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		if (exc instanceof FileSystemLoopException) {
			System.err.println("cycle detected: " + file);
		}
		return FileVisitResult.CONTINUE;
	}


	public int getNumTests() {
		return tests.size();
	}


	public void reportTests() {
		for(String t: tests) {
			//System.out.println(t);
		}
		
	}
}
