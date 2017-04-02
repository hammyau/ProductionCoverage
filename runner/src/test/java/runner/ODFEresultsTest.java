package runner;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ODFEresultsTest {

	@Test
/*	public void test() {
		String odfedir = "/Users/Ian/git/ODFExplorer/";
		ODFEresults results = new ODFEresults();
		Path runsPath = Paths.get(odfedir + "public/app/records/Aggregations/prodcov");
		results.setRunsPath(runsPath);
		results.setResultsOUtputPath(Paths.get("/Users/Ian/git/ProductionComparison/results/testPCCov.json"));
		results.read();
		results.getStatsOfLastRun();
		results.processSummary();
		results.write();
		assertTrue(results.getNumRuns() > 0);
	}*/

	public void getAllStats() {
		String odfedir = "/Users/Ian/git/ODFExplorer/";
		ODFEResults results = new ODFEResults();
		Path runsPath = Paths.get(odfedir + "public/app/records/Aggregations/prodcov");
		results.setRunsPath(runsPath);
		results.setResultsOUtputPath(Paths.get("/Users/Ian/git/ProductionComparison/results/testPCCov.json"));
		results.getStats();
		results.write();
		assertTrue(results.getNumRuns() > 0);
	}

}
