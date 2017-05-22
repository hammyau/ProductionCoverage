package runner;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class TestIncrements {
	
	private JsonFactory jFactory;
	private ObjectMapper mapper;
	private JsonGenerator generator;
	private ObjectNode incrementsRoot;
	private ArrayNode incrementsArray;
	private ObjectNode testDetail;
	private Path path;

	// Increments is an array of objects
	//	testName
	//  run name?
	//  dataArray 

	public TestIncrements() {
		
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
	}
	
	public void createJSON(Path p) {
		try {
			path = p;
			generator = jFactory.createJsonGenerator(new FileWriter(path.toFile()));
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();

			incrementsRoot = mapper.createObjectNode();
			incrementsArray = incrementsRoot.putArray("increments");
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public void addTest(String test, ArrayNode incrementValues) {
		testDetail = incrementsArray.addObject();
		testDetail.put("test", test);
		testDetail.put("data", incrementValues);
	}

	public void setRunName(String runName) {
		testDetail.put("run", runName);		
	}
	
	public void write() {
		try {
			generator.writeTree(incrementsRoot);
			generator.close(); 
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void toCSV() throws IOException {
		Path csvPath = Paths.get(path.toString().replaceAll("json", "csv"));
		FileWriter csvWriter = new FileWriter(csvPath.toFile());
		String elsPct = "";
		String attrsPct = "";
		for(int i=0; i<incrementsArray.size(); i++) {
			JsonNode increment = incrementsArray.get(i);
			String outline = increment.get("test").asText();
			JsonNode dataArray = increment.get("data");
			if(increment.get("run") != null) {
				elsPct = dataArray.get(2).asText();
				attrsPct = dataArray.get(3).asText();
			}
			outline += "," + dataArray.get(0).asText() + "," + dataArray.get(1).asText() + "," + elsPct+ "," + attrsPct +"\n";				
			csvWriter.write(outline);
		}
		csvWriter.close();
	}

	
}
