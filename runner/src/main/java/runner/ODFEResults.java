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

public class ODFEResults {

	private Path resultsPath;
	private Path runsPath;
	private JsonFactory jFactory;
	private ObjectMapper mapper;
	private ObjectNode gaugesRoot;
	private ObjectNode resultsRoot;
	private int numRuns;
	private JsonGenerator generator;
	private ArrayNode resultRunsArray;
	private ObjectNode runObject;
	private int runNumber;
	private String currentRunName;

	public ODFEResults() {
		
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
		runNumber = 0;
		currentRunName = "";
	}

	public String getString() {
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
			resultRunsArray = resultsRoot.putArray("runs");
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
/*	public void read() {
		if(runsPath.toFile().exists())	{
			JsonParser jParser;
			try {
				jParser = jFactory.createJsonParser(runsPath.resolve("odferuns.json").toFile());
				jParser.setCodec(mapper);
				JsonNode rootNode =  jParser.readValueAsTree();
				if(rootNode != null) {
					ArrayNode runsArray = (ArrayNode) rootNode.get("runs");
					numRuns = runsArray.size();
					JsonNode lastRunNode = runsArray.get(numRuns - 1);
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
	}*/

/*	public void getStatsOfLastRun() {
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
	}*/
	
	public void processSummary(ObjectNode gaugesRoot) {
		//want to build up an output JSON file
		//for the moment 
		ArrayNode summaryArray = (ArrayNode) gaugesRoot.get("summary");
		runObject.put("testDoc", gaugesRoot.get("document"));
		ArrayNode dataArray = runObject.putArray("data");
	    float summaryHits = 0;
	    float summaryElements = 0;
	    float summaryAttrHits = 0;
	    float summaryAttrElements = 0;
	
		for(int i=0; i< summaryArray.size(); i++) {
			JsonNode ns = summaryArray.get(i);
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

	public void getStatsOfRun(JsonNode runNode) {
		if(runsPath.toFile().exists())	{
			JsonParser jParser;
			try {
				runObject = resultRunsArray.addObject();
				runObject.put("run", runNode.get("extract").asText());
				Path runPath = runsPath.resolve(runNode.get("extract").asText());
				jParser = jFactory.createJsonParser(runPath.resolve("odfegauges.json").toFile());
				jParser.setCodec(mapper);
				gaugesRoot = (ObjectNode) jParser.readValueAsTree();
				if(gaugesRoot != null){
					processSummary(gaugesRoot);
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
	
	public void getStats() {
		if(runsPath.toFile().exists())	{
			JsonParser jParser;
			try {
				jParser = jFactory.createJsonParser(runsPath.resolve("odferuns.json").toFile());
				jParser.setCodec(mapper);
				JsonNode rootNode =  jParser.readValueAsTree();
				if(rootNode != null) {
					ArrayNode runsArray = (ArrayNode) rootNode.get("runs");
					getRunStats(runsArray);
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

	private void getRunStats(ArrayNode runsArray) {
		int numRuns = runsArray.size();
		for(int r=0; r<numRuns; r++) {
			getStatsOfRun(runsArray.get(r));
		}
	}

	public JsonNode getIncrementFor(String docName) {
		while(runNumber < resultRunsArray.size()) {
			JsonNode runNode = resultRunsArray.get(runNumber);
			if( runNode.get("testDoc").asText().equals(docName) ) {
				currentRunName = runNode.get("run").asText();
				System.out.println("Document Match for " + docName + " @ " + runNumber);
				JsonNode dataArray = runNode.get("data");
				return dataArray.get(dataArray.size() - 1); //Summary is last one
			}			
			runNumber++;
		}
		System.out.println("No Match for " + docName);
		return null;
	}

	public String getCurrentRunName() {
		return currentRunName;
	}

}
