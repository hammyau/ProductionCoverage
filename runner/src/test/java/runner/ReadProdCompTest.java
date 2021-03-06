package runner;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ReadProdCompTest {

	@Test
	public void test() {
		Path p = Paths.get("../tests/doc/DocumentCreationTest.java.json");
		ProdCompTest pct = new ProdCompTest(p);
		pct.intFromJSON();
		assert(pct.hasIncrements());
	}

	@Test
	public void test2() {
		Path p = Paths.get("../tests/doc/ChartTemplateTest.java.json");
		ProdCompTest pct = new ProdCompTest(p);
		pct.intFromJSON();
		assert(pct.hasIncrements());
		pct.run();
	}

}
