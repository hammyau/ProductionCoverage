package runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public class ODFEresults {

	private Path resultsPath;
	private Path runsPath;
	private JsonFactory jFactory;
	private ObjectMapper mapper;
	private ObjectNode gaugesRoot;
	private ObjectNode resultsRoot;
	private int numRuns;
	private Path lastRunPath;
	private ArrayNode summaryNode;
	private JsonGenerator generator;

	public ODFEresults() {
		
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
	}

	public String getString() {
		String results = "";
		return resultsPath.toString();
	}
	
	public int getNumRuns() {
		return numRuns;
	}

	public void setRunsPath(Path p) {
		runsPath = p;		
	}
	
	public void setResultsOUtputPath(Path path) {
		try {
			generator = jFactory.createJsonGenerator(new FileWriter(path.toFile()));
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			generator.setCodec(mapper);
			generator.useDefaultPrettyPrinter();

			resultsRoot = mapper.createObjectNode();
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void read() {
		if(runsPath.toFile().exists())	{
			JsonParser jParser;
			try {
				jParser = jFactory.createJsonParser(runsPath.resolve("odferuns.json").toFile());
				jParser.setCodec(mapper);
				JsonNode rootNode =  jParser.readValueAsTree();
				if(rootNode != null) {
					ArrayNode runsArray = (ArrayNode) rootNode.get("runs");
					numRuns = runsArray.size();
					JsonNode lastRunNode = runsArray.get(20);
					lastRunPath = runsPath.resolve(lastRunNode.get("extract").asText());
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void getStatsOfLastRun() {
		if(runsPath.toFile().exists())	{
			JsonParser jParser;
			try {
				jParser = jFactory.createJsonParser(lastRunPath.resolve("odfegauges.json").toFile());
				jParser.setCodec(mapper);
				gaugesRoot = (ObjectNode) jParser.readValueAsTree();
				if(gaugesRoot != null){
					summaryNode = (ArrayNode) gaugesRoot.get("summary");
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void processSummary() {
		//want to build up an output JSON file
		//for the moment 
		resultsRoot.put("test", gaugesRoot.get("document"));
		ArrayNode dataArray = resultsRoot.putArray("data");
	    float summaryHits = 0;
	    float summaryElements = 0;
	    float summaryAttrHits = 0;
	    float summaryAttrElements = 0;
	
		for(int i=0; i< summaryNode.size(); i++) {
			JsonNode ns = summaryNode.get(i);
			ObjectNode nsResult = dataArray.addObject();
			nsResult.put("name", ns.get("ns"));
			JsonNode elementsHit = ns.get("elementsHit");
			float elHits = 0 ;
			if (elementsHit.isArray()) {
				elHits = elementsHit.get(elementsHit.size() - 1).asInt();
			}
			summaryHits += elHits;
			float numElements = ns.get("elements").asInt();
			summaryElements += numElements;
			nsResult.put("elsPct", (elHits/numElements)*100);
			float numAttributes = ns.get("attributes").asInt();
			if(numAttributes > 0) {
				JsonNode attrsHit = ns.get("attrsHit");
				float aHits = 0 ;
				if (attrsHit.isArray()) {
					aHits = attrsHit.get(attrsHit.size() - 1).asInt();
				}
				summaryAttrHits += elHits;
				summaryAttrElements += numAttributes;
				nsResult.put("attrPct", (aHits/numAttributes)*100);				
			} else {
				nsResult.put("attrPct", 0);								
			}
		}
		ObjectNode summaryResult = dataArray.addObject();
		summaryResult.put("name", "Summary");
		summaryResult.put("elsPct", (summaryHits/summaryElements) *100);
		summaryResult.put("attrPct", (summaryAttrHits/summaryAttrElements) *100);	
	}

	public void write() {
		try {
			generator.writeTree(resultsRoot);
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
