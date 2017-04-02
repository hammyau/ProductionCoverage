package runner;

import static org.junit.Assert.assertTrue;

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
	
	private static final String ODFE_RUNS_PATH = "public/app/records/Aggregations/prodcov";
	private static final String ODFTOOLKIT_BASE = "odftoolkitBase";
	private static final String ODF_URI = "odfURI";
	private static final String ODFPROJECT = "odfProject";
	private static final String MAVEN_TESTBASE = "src/test/java/";
	private static final String MAVEN_TESTCLASSES = "target/test-classes/";
	private static final String MAVEN_COVERAGE_SITE = "target/site/cobertura/";
	private static final String ODFE_BASE = "odfeBase";
	private static final String USER_DIR = "user.dir";
	private Properties props;
	private final static   Logger LOGGER = Logger.getLogger(ProductionComparison.class.getName());
	
	private List<File> tests = new ArrayList<File>();
	private String runDir;
	private Path originalTestsPath;
	private Path odfProjectBase;
	private Path testStore;
	private Path originalTestClasses;
	private Path coveragePath;
	private Path resultsPath;
	private Path odfExplorerBase;
	private Path odfExplorerDocuments;
	private ODFEResults odferesults;
	
	ProductionComparison() throws FileNotFoundException, IOException {
		readProperties("prodComp.properties");
		setTestPaths();				
	}

	
	public void run() {
		System.out.println("Production Comparison");
		System.out.println("ODF Toolkit @ " + props.getProperty(ODFTOOLKIT_BASE));
		System.out.println("ODFE @ " + props.getProperty(ODFE_BASE));
		
		moveTestsToStore();	
		runTests();
		getODFEResults();
		correlateTheResults();
	}
	
	public void correlateTheResults() {
		// walk the test tree again to build an incremental picture of coverage
		// for both the code and production
		System.out.println("Production Comparison Test Correlation");
		System.out.println("ODF Toolkit @ " + props.getProperty(ODFTOOLKIT_BASE));
		System.out.println("ODFE @ " + props.getProperty(ODFE_BASE));
		
		
		Path testsRoot = Paths.get(runDir + "/../tests");
		testsRoot = testsRoot.normalize();
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        ProdCompTestCorrelator pctc = new ProdCompTestCorrelator(testsRoot);
        pctc.setResultsPath(resultsPath);
        pctc.setODFEResults(odferesults);
		try {
	        Files.walkFileTree(testsRoot, opts, Integer.MAX_VALUE, pctc);		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public void runSingleTest(String name) {
		System.out.println("Production Comparison");
		System.out.println("ODF Toolkit @ " + props.getProperty(ODFTOOLKIT_BASE));
		System.out.println("ODFE @ " + props.getProperty(ODFE_BASE));
		
		moveTestsToStore();	
		//Change this to a tree walking thing
		Path testsRoot = Paths.get(runDir + "/../tests");
		testsRoot = testsRoot.normalize();
		
		Path p = Paths.get(name);
		ProdCompTest pct = new ProdCompTest(p);
		pct.intFromJSON(p);
        pct.setODFProjectBase(odfProjectBase);
        pct.setODFTestsBase(originalTestsPath);
        pct.setTestStore(testStore);		
        pct.setTestClassesBase(originalTestClasses);
        pct.setCoverageSite(coveragePath);
        pct.setResultsPath(resultsPath);
        
        //need to clean out the old aggregations first
        pct.setODFExplorerBase(odfExplorerBase);
        pct.setODFExplorerDocuments(odfExplorerDocuments);
        
        pct.run();
        
        
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
		originalTestsPath = Paths.get(props.getProperty(ODFTOOLKIT_BASE), props.getProperty(ODFPROJECT), MAVEN_TESTBASE, props.getProperty(ODF_URI), props.getProperty(ODFPROJECT));		
		originalTestClasses = Paths.get(props.getProperty(ODFTOOLKIT_BASE), props.getProperty(ODFPROJECT), MAVEN_TESTCLASSES, props.getProperty(ODF_URI), props.getProperty(ODFPROJECT));
		
		coveragePath = Paths.get(props.getProperty(ODFTOOLKIT_BASE), props.getProperty(ODFPROJECT), MAVEN_COVERAGE_SITE);

		testStore = Paths.get(System.getProperty(USER_DIR), "../testStore");
		resultsPath = Paths.get(System.getProperty(USER_DIR), "../results").normalize();
		originalTestsPath = originalTestsPath.toAbsolutePath();
		testStore = testStore.normalize();
		odfProjectBase = Paths.get(props.getProperty(ODFTOOLKIT_BASE), props.getProperty(ODFPROJECT));

		odfExplorerBase = Paths.get(props.getProperty(ODFE_BASE));
		odfExplorerDocuments = Paths.get(props.getProperty(ODFTOOLKIT_BASE), props.getProperty(ODFPROJECT), MAVEN_TESTCLASSES);
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
		Path testsRoot = Paths.get(runDir + "/../tests");
		testsRoot = testsRoot.normalize();
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        ProdCompTest pct = new ProdCompTest(testsRoot);
        pct.setODFProjectBase(odfProjectBase);
        pct.setODFTestsBase(originalTestsPath);
        pct.setTestStore(testStore);		
        pct.setTestClassesBase(originalTestClasses);
        pct.setCoverageSite(coveragePath);
        pct.setResultsPath(resultsPath);
        
        //need to clean out the old aggregations first
        pct.setODFExplorerBase(odfExplorerBase);
        pct.setODFExplorerDocuments(odfExplorerDocuments);
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
		return props.getProperty(ODFTOOLKIT_BASE) != null;
	}
	
	public boolean isODFEDirSet() {
		return props.getProperty(ODFE_BASE) != null;
	}

	public boolean haveResults() {
		return false;
	}

	public void getODFEResults() {
		String odfedir = props.getProperty(ODFE_BASE);
		odferesults = new ODFEResults();
		Path runsPath = Paths.get(odfedir + ODFE_RUNS_PATH);
		odferesults.setRunsPath(runsPath);
		odferesults.setResultsOUtputPath(resultsPath.resolve("testPCCov.json"));
		odferesults.getStats();
		odferesults.write();
	}}

