package runner;

import java.io.File;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;

public class ProdCompTest {
	
	private JsonFactory jFactory;
	private ObjectMapper mapper;

	public ProdCompTest() {
		jFactory = new JsonFactory();
		mapper = new ObjectMapper();
	}
	
	public void intFromJSON(File testName) {
		if(testName.exists()) {
			//JSON read and we'll just wrap the tree
		}
	}

	public boolean hasElements() {
		// TODO Auto-generated method stub
		return false;
	}
}
