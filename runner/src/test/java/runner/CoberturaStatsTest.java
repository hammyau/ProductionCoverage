package runner;

import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.Test;

public class CoberturaStatsTest {

	@Test
	public void test() {
		CoberturaStats cs = new CoberturaStats();
		cs.setSite(Paths.get("C:\\Users\\Ian\\odf\\odfdom\\target\\site\\cobertura\\frame-summary.html"));
		assert(cs.getResults().size() > 0);
		System.out.println(cs.getResults());
	}

}
