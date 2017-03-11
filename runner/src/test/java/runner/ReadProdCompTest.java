package runner;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class ReadProdCompTest {

	@Test
	public void test() {
		ProdCompTest pct = new ProdCompTest();
		pct.intFromJSON(new File("../tests/doc/DocumentCreationTest.java.json"));
		assert(pct.hasIncrements());
	}

}
