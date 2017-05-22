package runner;

import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class ProdCompTestCorrelator implements FileVisitor<Path> {

	private Path resultsPath;
	private Path runsPath;

	private ObjectNode rootNode;
	private JsonFactory jFactory;
	private ObjectMapper mapper;
	private ODFEResults odferesults;
	
	private int testNum = 1;
	private TestIncrements tesIncrements;

	public ProdCompTestCorrelator(Path testsRoot) {
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


	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		// TODO Auto-generated method stub
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (file.getFileName().toString().endsWith(".json")) {
			intFromJSON(file);
			ArrayNode incrementValues = (ArrayNode) getCodeIncrement(file);
			String docName = getLastDocumentName();
			//Need to have an ODFEResult object in the background to iterate.
			//May be able to pass in instead of open and read each time.
			if(incrementValues != null) {
				tesIncrements.addTest(file.getFileName().toString(), incrementValues);
				if (docName.length() > 0) {
					JsonNode docData = odferesults.getIncrementFor(docName);
					String runName = odferesults.getCurrentRunName();
					appendProductionCoverageData(incrementValues, docData);
					tesIncrements.setRunName(runName);
					System.out.println(testNum + " " + file.getFileName().toString() + " " + incrementValues);
				}
				testNum++;
			} else {
				System.out.println(testNum + " no agg " + file.getFileName().toString() + " " + incrementValues);
			}
		}
		return FileVisitResult.CONTINUE;
	}

	private void appendProductionCoverageData(ArrayNode incrementValues, JsonNode docData) {
		incrementValues.add(docData.get("elsPct").asText());
		incrementValues.add(docData.get("attrPct").asText());
	}

	private String getLastDocumentName() {
		String docName = "";
		JsonNode docsoutArray = rootNode.findValue("docsout");
		if(docsoutArray != null && docsoutArray.isArray()) {
			int num = docsoutArray.size();
			if (num > 0) {
				docName =  docsoutArray.get(num - 1).asText();
			}
		} else {
			JsonNode docsInArray = rootNode.findValue("docsin");			
			int num = docsInArray.size();
			if (num > 0) {
				docName =  docsInArray.get(num - 1).asText();
			}
		}
		return docName;
	}

	private JsonNode getCodeIncrement(Path file) {
		JsonNode incValues = null;
		String testName = rootNode.findValue("name").asText();
		Path outFile = Paths.get(testName);
		String NumTestName = String.format("%02d_", testNum) + outFile.getFileName().toString() + "_Covg.json";
		JSONTestCoverage jtc = new JSONTestCoverage(NumTestName);
		jtc.read(resultsPath);
		if(jtc.isReadAble()) {
			incValues = jtc.getTestIncrement("All Packages");
			System.out.println(incValues);
		}
		return incValues;
	}

	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		if (exc instanceof FileSystemLoopException) {
			System.err.println("cycle detected: " + file);
		} else {
			System.err.format("Unable to correlate: %s: %s%n", file, exc);
		}
		return FileVisitResult.CONTINUE;
	}

	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public void setResultsPath(Path p) {
		resultsPath = p;		
	}

	public void setODFEResults(ODFEResults odfe) {
		odferesults = odfe;
	}

	public void setTestIncrements(TestIncrements tis) {
		tesIncrements = tis;	
	}


}
