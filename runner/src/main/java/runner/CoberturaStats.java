package runner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class CoberturaStats {
	
	/* Parse the Cobertura results HTML file
	 * to get some statistics on a test run
	 */
	private Path coberturaSite;
	
	private Map<String, String[]> results = new HashMap<String, String[]>();
	
	CoberturaStats() {

	}

	public Map<String, String[]> getResults() {
		Document doc = null;
		try {
			doc = Jsoup.parse(coberturaSite.toFile(), "UTF-8", "http://example.com/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements header = doc.select("b");
		Elements trs = doc.getElementsByTag("tr");
		int num = 0;
		for(Element tr : trs) {
			String name = "";
			if(num == 1) {
				Elements node = tr.select("b");
//				System.out.println(node.text().toString());
				name = node.text().toString();
			} else {
				Elements as = tr.select("a[href]");	
				if(as.size() == 1 || as.size() == 3) {
//					System.out.println(as.get(0).text());
					name = as.get(0).text();
				}
			}
			List<Element> children = tr.select("td.percentgraph");
			if(children.size() > 3) {
//				System.out.println(children.get(0).text());
				String[] vals = new String[2];
				vals[0] = children.get(0).text();
				vals[1] = children.get(2).text();
//				System.out.println(children.get(2).text());
				results.put(name, vals);
			}
			num++;
		}
		return results;
	}
	
	public void setSite(Path site) {
		coberturaSite = site.resolve("frame-summary.html");
	}
}
