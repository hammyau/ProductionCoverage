package runner;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

public class ProdCompTestReporter implements FileVisitor<Path> {

	private ObjectNode testDetailsRootNode;
	
	private ObjectNode rootNode;
	private JsonFactory jFactory;
	private ObjectMapper mapper;
	
	private List<ObjectNode> tests = new ArrayList<ObjectNode>();

	private ArrayNode testDetailsArray;

	public ProdCompTestReporter(Path testsRoot) {
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
		testDetailsRootNode = JsonNodeFactory.instance.objectNode();
		testDetailsArray = testDetailsRootNode.putArray("details");
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
			tests.add(rootNode);
		}
		return FileVisitResult.CONTINUE;
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
	
	public void listTests() throws IOException {
		int num = 1;
		for(ObjectNode test : tests) {
			String line = String.format("%-50s", test.get("name").asText()) + " -> ";
			if(test.get("testRan") != null && test.get("testRan").asBoolean() == false) {
				line += test.get("testRan").asText();
				System.out.println(num + " " + line);
			} 
			testDetailsArray.add(test);
			num++;
		}
		JsonGenerator generator = jFactory.createJsonGenerator(new FileWriter("testDetails.json"));
		mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		generator.setCodec(mapper);
		generator.useDefaultPrettyPrinter();
		generator.writeTree(testDetailsRootNode);
		generator.close();
	}

}
