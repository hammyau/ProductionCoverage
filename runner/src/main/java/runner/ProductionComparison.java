package runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class ProductionComparison {

	private static final String ODFTOOLKIT_PROJECT = "odftoolkitProject";
	private static final String ODFE_PROJECT = "odfeProject";
	private static final String USER_DIR = "user.dir";
	private Properties props;
	private final static   Logger LOGGER = Logger.getLogger(ProductionComparison.class.getName());
	
	private List<File> tests;
	private String runDir;
	
	public void run() {
		System.out.println("Production Comparison");
		System.out.println("ODF Toolkit @ " + props.getProperty(ODFTOOLKIT_PROJECT));
		System.out.println("ODFE @ " + props.getProperty(ODFE_PROJECT));
		
		findTests();
	}
	
	public int numTests() {
		return tests.size();
	}
	
	private void findTests() {
		System.out.println("Tests:");
		tests = new ArrayList<File>();
		addtestsFrom(new File(runDir + "/../tests"));
		
	}
	
	public void addtestsFrom(File node){

		System.out.println(node.getAbsoluteFile());
		tests.add(node);

		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				addtestsFrom(new File(node, filename));
			}
		}

	}

	public void readProperties(String propsName) throws IOException, FileNotFoundException {
		runDir = System.getProperty(USER_DIR);
		props = new Properties();
		File runFile = new File(runDir);
		File propsFile = new File(runFile, propsName);
		try {
		       //load a properties file from class path, inside static method
			props.load(new FileReader(propsFile));
		}
		catch (IOException e) {
			LOGGER.config("Generating properties file " + propsFile.getAbsolutePath());
		}
	}

	public boolean isODFToolkitDirSet() {
		return props.getProperty(ODFTOOLKIT_PROJECT) != null;
	}
	
	public boolean isODFEDirSet() {
		return props.getProperty(ODFE_PROJECT) != null;
	}

	public boolean haveResults() {
		// TODO Auto-generated method stub
		return false;
	}
}
