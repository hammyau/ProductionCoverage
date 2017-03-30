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
		
		pc.runSingleTest("../tests/doc/ImageTest.java.json");
		assert(pc.getNumberOfOriginalTests() > 0 && pc.getNumberOfStoredTests() == 0);
	}

}
