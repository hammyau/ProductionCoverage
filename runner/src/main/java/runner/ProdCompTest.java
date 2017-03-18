package runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;


// We can make this a File Visitor and manage it that way
// Looks much better.
public class ProdCompTest implements FileVisitor {
	
	private ObjectNode rootNode;
	private JsonFactory jFactory;
	private ObjectMapper mapper;
	
	private Path odfToolkitTestBase;
	private Path odfeBase;
	private Path testStore;
	private Path odfProjectBase;
	private Path testClassesBase;
	private Path coverageSite;
	private Path resultsPath;

	public ProdCompTest(Path testsRoot) {
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
	}
	
	public void intFromJSON(Path test) {
		if(Files.exists(test)) {
			JsonParser jParser;
			try {
				jParser = jFactory.createJsonParser(test.toFile());
				jParser.setCodec(mapper);
				rootNode = (ObjectNode) jParser.readValueAsTree();
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean hasIncrements() {
		ArrayNode runsArray = (ArrayNode) rootNode.findValue("increments");
		return runsArray.size() > 0;
	}

	public void run() {
		String testName = rootNode.findValue("name").asText();
		System.out.println("Run Test " + testName);
		Path source = testStore.resolve(testName);
		Path target = odfToolkitTestBase.resolve(testName);
		try {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			runMavenTest();
			Files.move(target, source, StandardCopyOption.REPLACE_EXISTING);
			String className = testName.replace(".java", ".class");
			Path targetClass = testClassesBase.resolve(className);
			Files.delete(targetClass);
			
			//once we get the coverage results
			//what do we do with them?
			// write then to a json results file in the results directory
			// current has the raw coveage numbers too
			
			//Probably best to have the test results as a list of results values
			//easier to iterate and write to JSON - or have it create JSON nodes already?
			CoberturaStats cs = new CoberturaStats();
			cs.setSite(coverageSite);
			JSONTestCoverage jsonCov = new JSONTestCoverage(testName);
			jsonCov.open(resultsPath);
			jsonCov.add(cs.getResults());
			jsonCov.writeToFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void runMavenTest() {
		CommandRunner runner = new CommandRunner();
		String command = "mvn.cmd cobertura:cobertura";
		try {
			runner.run(command, odfProjectBase.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(runner.getCmdOutput());
	}

	public void setODFTestsBase(Path originalTestsPath) {
		odfToolkitTestBase = originalTestsPath;
	}

	public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
		intFromJSON((Path)file);
		run();
		return FileVisitResult.CONTINUE;
	}

	public void setTestStore(Path ts) {
		testStore = ts;
	}

	public void setODFProjectBase(Path projectBase) {
		odfProjectBase = projectBase;
	}

	public void setTestClassesBase(Path originalTestClasses) {
		testClassesBase = originalTestClasses;	
	}
	
	public void setCoverageSite(Path site) {
		coverageSite = site;
	}

	public void setResultsPath(Path p) {
		resultsPath = p;
	}
}


