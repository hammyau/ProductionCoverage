package runner;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class ReadConfigTest {

	@Test
	public void storeTests() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.moveTestsToStore();
		//pc.run();
		assert(pc.getNumberOfOriginalTests() == 0 && pc.getNumberOfStoredTests() > 0);
	}

	@Test
	public void restoreTests() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.restoreTests();
		//pc.run();
		assert(pc.getNumberOfOriginalTests() > 0 && pc.getNumberOfStoredTests() == 0);
	}

	@Test
	public void deleteSiteTests() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.deleteSite();
		assert(pc.getNumberOfOriginalTests() > 0 && pc.getNumberOfStoredTests() == 0);
	}

	@Test
	public void cleanMaven() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.cleanMaven();
		assert(pc.getNumberOfOriginalTests() > 0 && pc.getNumberOfStoredTests() == 0);
	}

	@Test
	public void runTests() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.run();
		assert(pc.getNumberOfOriginalTests() > 0 && pc.getNumberOfStoredTests() == 0);
	}

	@Test
	public void runNamedTest() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.runSingleTest("../tests/simple/common/GetTextTest.java.json");
		assert(pc.getNumberOfOriginalTests() > 0 && pc.getNumberOfStoredTests() == 0);
	}

	@Test
	public void correlateTests() throws FileNotFoundException, IOException {
		// We have the tests in the test directory
		//	a test has the java test name
		//  and zero or more associated documents
		// The results directory has the code coverage results after each test
		// and the production coverage details after each document was processed.
		
		// Build an incremental picture of coverage as the tests are performed
		// this can be done for each namespace or as a summary
		//	need a mapping of packages to namespaces
		
		// for each test
		//	find the test result file
		//		get the all packages data
		//  determine the last document.
		//  iterate through the production coverage date to get the correct data
		//		maintain the iterator. And cross check to the test docs?
		//	write the test increment data
		
		
		
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.getODFEResults();
		pc.correlateTheResults();
		assert(pc.getNumberOfOriginalTests() > 0 );
	}

	//create a test to verify that all of the documents in the test-classes
	//are used in a test.
	
	@Test
	public void listTests() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		
		pc.listTests();
		pc.getNumberOfStoredTests();

	}

}
