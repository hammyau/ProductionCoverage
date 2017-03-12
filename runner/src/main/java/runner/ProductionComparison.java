package runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class ProductionComparison {
	
	private static final String ODFTOOLKIT_PROJECT = "odftoolkitProject";
	private static final String ODFE_PROJECT = "odfeProject";
	private static final String USER_DIR = "user.dir";
	private Properties props;
	private final static   Logger LOGGER = Logger.getLogger(ProductionComparison.class.getName());
	
	private List<File> tests = new ArrayList<File>();
	private String runDir;
	private Path originalTestsPath;
	private Path testStore;
	
	ProductionComparison() throws FileNotFoundException, IOException {
		readProperties("prodComp.properties");
		setTestPaths();				
	}

	
	public void run() {
		System.out.println("Production Comparison");
		System.out.println("ODF Toolkit @ " + props.getProperty(ODFTOOLKIT_PROJECT));
		System.out.println("ODFE @ " + props.getProperty(ODFE_PROJECT));
		
		moveTestsToStore();	
		runTests();
	}
	
	public void moveTestsToStore() {
        moveTests(originalTestsPath, testStore);	
	}
	
	public int getNumberOfOriginalTests() {
		return getNumberofJavaTests(originalTestsPath);
	}
	
	public int getNumberOfStoredTests() {
		return getNumberofJavaTests(testStore);
	}
	
	public int getNumberofJavaTests(Path from) {
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        TestCounter tc = new TestCounter(from);
		
		try {
	        Files.walkFileTree(from, opts, Integer.MAX_VALUE, tc);		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tc.getNumTests();		
	}

	private void setTestPaths() {
		originalTestsPath = Paths.get(props.getProperty(ODFTOOLKIT_PROJECT), "src/test/java/org/odftoolkit/odfdom");		
		testStore = Paths.get(System.getProperty(USER_DIR), "../testStore");
		originalTestsPath = originalTestsPath.toAbsolutePath();
		testStore = testStore.normalize();
	}

	public void restoreTests() {
        moveTests(testStore, originalTestsPath);	
	}

	private void moveTests(Path from, Path to) {
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        TestCopier tc = new TestCopier(from, to);
		
		try {
	        Files.walkFileTree(from, opts, Integer.MAX_VALUE, tc);		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void runTests() {
		//Change this to a tree walking thing
		Path testsRoot = Paths.get(runDir + "/../tests");
		testsRoot = testsRoot.normalize();
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        ProdCompTest pct = new ProdCompTest(testsRoot);
        pct.setODFBase(originalTestsPath);
        pct.setTestStore(testStore);		
		try {
	        Files.walkFileTree(testsRoot, opts, Integer.MAX_VALUE, pct);		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int numTests() {
		return tests.size();
	}
		
	public void addtestsFrom(File node){
		if(node.isDirectory()) {
			String[] subNote = node.list();
			for(String filename : subNote) {
				addtestsFrom(new File(node, filename));
			}
		} else {
			tests.add(node);			
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
		return false;
	}
}
