package runner;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class ReadConfigTest {

	@Test
	public void test() throws FileNotFoundException, IOException {
		ProductionComparison pc = new ProductionComparison();
		pc.readProperties("prodComp.properties");
		assert(pc.isODFEDirSet());
		assert(pc.isODFToolkitDirSet());
		pc.run();
		assert(pc.numTests() > 0);
	}

}
