package runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;


// We can make this a File Visitor and manage it that way
// Looks much better.
public class ProdCompTest implements FileVisitor {
	
	private static final String TEMP_TEMP = "temptemp/";

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
	private Path odfExplorerBase;
	private Path odfExplorerDocuments;
	
	private int testNum = 1;
	private String projectName;

	private Path testFilePath;

	public ProdCompTest(Path testsRoot) {
		testFilePath = testsRoot;
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
	}
	
	public void intFromJSON() {
		if(Files.exists(testFilePath)) {
			JsonParser jParser;
			try {
				jParser = jFactory.createJsonParser(testFilePath.toFile());
				jParser.setCodec(mapper);
				rootNode = (ObjectNode) jParser.readValueAsTree();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
			mavenTheTest(testName, source, target);
			String NumTestName = saveJSONCoverage(testName);

			if (addDocsin() + addDocsout() > 0) {
				odfeEachDocument(NumTestName);
			} else {
				System.out.println("There are no documents associated with test " + testName);
			}
			updateJSON();
			testNum++;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void mavenTheTest(String testName, Path source, Path target) throws IOException {
		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		runMavenTest();
		Files.move(target, source, StandardCopyOption.REPLACE_EXISTING);
		String className = testName.replace(".java", ".class");
		Path targetClass = testClassesBase.resolve(className);
		Files.delete(targetClass);
	}

	private String saveJSONCoverage(String testName) {
		CoberturaStats cs = new CoberturaStats();
		cs.setSite(coverageSite);
		testName = String.format("%02d_", testNum) + testName;
		Path outFile = Paths.get(testName);
		String NumTestName;
		NumTestName = String.format("%02d_", testNum) + outFile.getFileName().toString() + "_Covg.json";
		JSONTestCoverage jsonCov = new JSONTestCoverage(NumTestName);
		jsonCov.open(resultsPath);
		jsonCov.add(cs.getResults());
		jsonCov.writeToFile();
		return NumTestName;
	}

	private void updateJSON() throws IOException {
		JsonGenerator generator = jFactory.createJsonGenerator(new FileWriter(testFilePath.toFile()));
		mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		generator.setCodec(mapper);
		generator.useDefaultPrettyPrinter();
		rootNode.put("testRan", true);
		generator.writeTree(rootNode);
		generator.close();
	}

	private int addDocsin() {
		ArrayNode docsin = (ArrayNode) rootNode.findValue("docsin");
		if(docsin != null)
			return docsin.size();
		else
		 return 0;
	}

	private int addDocsout() {
		ArrayNode docsout = (ArrayNode) rootNode.findValue("docsout");
		if(docsout != null)
			return docsout.size();
		else
		 return 0;
	}

	private void runODFE(String testName, String args) {
		CommandRunner runner = new CommandRunner();
		try {
			runner.run(args, odfExplorerBase.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			String logName = testName.replace('/','_');
			PrintWriter log = new PrintWriter("Logs/" + logName);
			log.println(runner.getCmdOutput());
			log.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ODFE Completed for " + testName);
	}

	private void odfeEachDocument(String testName) {
		//maybe we should just use the properties file
		ArrayNode testIncrement = (ArrayNode) rootNode.findValue("increments");
		ArrayNode docsin = (ArrayNode) testIncrement.findValue("docsin");
		if(docsin != null)
			executeODFEforEachDocument(testName, docsin);
		
		ArrayNode docsout = (ArrayNode) testIncrement.findValue("docsout");
		if(docsout != null)
			executeODFEforEachDocument(testName, docsout);
		
		ArrayNode tempdocsout = (ArrayNode) testIncrement.findValue("tempdocsout");
		if(tempdocsout != null)
			executeODFEforEachTempDocument(testName, tempdocsout);
	}

	private void executeODFEforEachTempDocument(String testName, ArrayNode tempdocsout) {
		// the temp files are now written to a temp directory under test-classes
		// for the moment let's just ignore them and manually run ODFE on them at the end
		// to see if any additional coverage it added.
	}

	private void executeODFEforEachDocument(String testName, ArrayNode docsin) {
		for(int i=0; i<docsin.size(); i++) {
			String args = "java -jar odfe.jar -a -o " + projectName + " -f ";
			String documentName = docsin.get(i).asText();
			Path docPAth = odfExplorerDocuments.resolve(documentName);
			if(docPAth.toFile().exists()) {
				args += docPAth.toString();
				String logName = testName+"_"+documentName+".txt";
				runODFE(logName ,args);
			} else {
				System.out.println("ODFE not run. Document does not exist " + docPAth.toString());
			}
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

	public void cleanMaven() {
		CommandRunner runner = new CommandRunner();
		String command = "mvn.cmd clean";
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
		testFilePath = (Path)file;
		intFromJSON();
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

	public void setODFExplorerBase(Path odfe) {
		odfExplorerBase = odfe;		
	}

	public void setODFExplorerDocuments(Path docs) {
		odfExplorerDocuments = docs;	
	}

	public void setProjectName(String project) {
		projectName = project;		
	}
}


