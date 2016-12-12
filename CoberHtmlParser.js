// Parse Cobertura Results
var htmlparser = require('htmlparser2');
var jsonfile = require('jsonfile');
var fs = require('fs-extra');

exports.parseResultsToJSON = parseResultsToJSON;

// parse the test results file and generate a JSON
// file of the coverage results

// This should be given the file not be expected to go find it...
function parseResultsToJSON(projectDirectory, test, key, callback) {
	console.log("Cober Parser for " + test);
	var results = {};
	var row = {};

	//states
	// waiting
	// table
	// row
	// name
	// innerTable
	// percentage

	var state = "waiting";
	var jsonWritten = false;

	//bits we really care about
	results.test = test;
	results.data = [];
	row.name = "";
	row.values = [];

	var parser = new htmlparser.Parser({
			//Structure is
			//table
			// row
			//   td
			//    a tag or b has title
			//    table
			//        tr
			//          first td Percentage values
			//    table
			//
			onopentag : function (name, attribs) {
				switch (state) {
				case "waiting":
					if (name === "tbody") {
						state = "table";
//						console.log("waiting -> table");
					}
					break;

				case "table":
					if (name === "tr") {
						state = "row";
						row.name = "";
						row.values = [];
//						console.log("table -> row");
					}
					break;

				case "row":
					if (name === "a" || name === "b") {
						state = "name";
//						console.log("row -> name");
						if (row.name.length > 0) {
							results.data.push(row);
						}
						row = {};
						row.name = "";
						row.values = [];
					} else if (name === "table") {
						state = "innerTable";
//						console.log("row -> innerTable");
					}

					break;

				case "named":
					if (name === "table") {
						state = "innerTable";
//						console.log("name -> innerTable");
					}
					break;

				case "innerTable":
					if (name === "td") {
						state = "percentage1";
//						console.log("innerTable -> percentage");
					}
					break;

				case "percentage1":
					if (name === "td") {
						state = "percentage";
//						console.log("innerTable -> percentage");
					}
					break;
				}
			},
			ontext : function (text) {
				if (state === "name") {
					row.name = text;
				} else if (state === "percentage1" || state === "percentage2") {
					row.values.push(text);
				}
			},
			onclosetag : function (tagname) {
				switch (state) {
				case "row":
					if (tagname === "tbody") {
						state = "waiting";
//						console.log("row -> waitin");
					}
					break;

				case "name":
					if (tagname === "a" || tagname === "b") {
						state = "named";
//						console.log("name -> named");
					}
					break;
				case "percentage1":
					if (tagname === "td") {
						state = "percentage2";
//						console.log("percentage1 -> percentage2");
					}
					break;
				case "percentage2":
					if (tagname === "td") {
						state = "row";
//						console.log("percentage2 -> row");
					}
					break;
				}
			},
			onend : function () {
				console.log("End of file");
				jsonfile.writeFile("results/" + "covresults_" + key + ".json", results, function (err) {
                    console.log("JSON callback");
					if (err) {
						console.log(err);
						callback(err);
					} else {
						console.log("results JSON written");
						if (jsonWritten === false) {
							jsonWritten = true;
							callback(null);
						}
					}
				});
			}
		}, {
			decodeEntities : true
		});

	console.log("Parse " + projectDirectory + '/target/site/cobertura/frame-summary.html');
	fs.createReadStream(projectDirectory + '/target/site/cobertura/frame-summary.html').pipe(parser).on('end', function () {
		console.log("File read");
	});
	console.log("parser return");
	parser.end();
	console.log("parser return");
}
