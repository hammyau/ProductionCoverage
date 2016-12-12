// ODFE results gatherer
var jsonfile = require('jsonfile');

function generateJSON(odfdir, aggname, test, key, callback) {
	console.log("Figure out the production coverage results");
	//Where are the results?
	var resultsDir = odfdir + "public/app/records/Aggregations/" + aggname + "/";
	//get the odferuns file
	var odferuns = jsonfile.readFileSync(resultsDir + "odferuns.json");
	var lastrun = odferuns.runs[odferuns.runs.length - 1];
	console.log("Last run " + lastrun.extract);

	var gauges = jsonfile.readFileSync(resultsDir + "/" + lastrun.extract + "/odfegauges.json");
	console.log("Summary ")

	// So what do we do with these results?
	// Iterate througn the summary and get the percentages of each ns
	// for elements and attributes
	var nsResults = {};
	nsResults.test = test;
	nsResults.data = [];
	var numNamespaces = gauges.summary.length;
	for (var n = 0; n < numNamespaces; n++) {
		var ns = gauges.summary[n];
		var nsResult = {};
		nsResult.name = ns.ns;
		var elhits = ns.elementsHit[ns.elementsHit.length - 1];
		nsResult.elsPct = (elhits / ns.elements) * 100;
        if(ns.attributes > 0) 
        {
            var ahits = ns.attrsHit[ns.attrsHit.length - 1];
            nsResult.attrPct = (ahits / ns.attributes) * 100;
        } else {
            nsResult.attrPct = 0;    
        }
		nsResults.data.push(nsResult);
	}
	jsonfile.writeFile("results/" + "pcresults_" + key + ".json", nsResults, function (err) {
		callback();
	});
}

module.exports.generateJSON = generateJSON;
