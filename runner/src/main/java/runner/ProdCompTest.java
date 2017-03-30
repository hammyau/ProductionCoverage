package runner;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.codehaus.jackson.JsonNode;
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
	private Path odfExplorerBase;
	private Path odfExplorerDocuments;

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
		//First see if there are any associated documents
		ArrayNode docsin = (ArrayNode) rootNode.findValue("docsin");
		int docCount = 0;
		if(docsin != null)
			docCount = docsin.size();
		ArrayNode docsout = (ArrayNode) rootNode.findValue("docsout");
		if(docsout != null)
			docCount += docsout.size();
		if (docCount > 0) {
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

				CoberturaStats cs = new CoberturaStats();
				cs.setSite(coverageSite);
				JSONTestCoverage jsonCov = new JSONTestCoverage(testName);
				jsonCov.open(resultsPath);
				jsonCov.add(cs.getResults());
				jsonCov.writeToFile();

				odfeEachDocument(testName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("There are no documents associated with test " + testName);			
		}
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
	}

	private void executeODFEforEachDocument(String testName, ArrayNode docsin) {
		for(int i=0; i<docsin.size(); i++) {
			String args = "java -jar odfe.jar -a -o prodcov -f ";
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

	public void setODFExplorerBase(Path odfe) {
		odfExplorerBase = odfe;		
	}

	public void setODFExplorerDocuments(Path docs) {
		odfExplorerDocuments = docs;	
	}
}


