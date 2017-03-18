package runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class JSONTestCoverage {

	private JsonGenerator generator;
	private ObjectNode rootNode;
	private String testName;
	private ObjectMapper mapper;

	public JSONTestCoverage(String name) {
		testName = name;
	}

	public void open(Path dir) {
		
		JsonFactory f = new JsonFactory();
		try {
			Path outFile = Paths.get(testName);
			generator = f.createJsonGenerator(new FileWriter(dir.resolve(outFile.getFileName().toString() + "_Covg.json").toFile()));
			mapper = new ObjectMapper();
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();

			rootNode = mapper.createObjectNode();
			rootNode.put("name", testName);
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(Map<String, String[]> results) {	
		ArrayNode namespaces = rootNode.putArray("namespaces");
		for(Entry<String, String[]> entry : results.entrySet()) {
			ArrayNode resultPercentages = mapper.createArrayNode();
			resultPercentages.add(entry.getValue()[0]);
			resultPercentages.add(entry.getValue()[1]);
		    ObjectNode node = mapper.createObjectNode();
		    node.put(entry.getKey(), resultPercentages);
		    namespaces.add(node);
		}
	}
	
	public void writeToFile() {
		try {
			generator.writeTree(rootNode);
			generator.close();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
