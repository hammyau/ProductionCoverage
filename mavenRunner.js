/*
 * Node JS Project to run Maven Cobertura iteratively on a series of tests
 * In the ODF Toolkit.
 * Aim is to accumulate progressive coverage results
 *
 * We will then include the associated documents and run Production Coverage
 * via ODFE and get a comparison of Production versus Code Coverage
 */

var spawn = require('child_process').spawn;
var shelljs = require('shelljs');
var async = require('async');
var htmlparser = require('htmlparser2');
var fs = require('fs-extra');
var jsonfile = require('jsonfile');
var json2csv = require('json2csv');

var coberParser = require('./CoberHtmlParser');

var ODFDOM = "/Users/Ian/odf/odfdom";
var ODF = "/Users/Ian/odf/";
var TESTBASE = "/Users/Ian/odf/testBase/odfdom/";
var TESTSRC = "/Users/Ian/odf/odfdom/src/";

var ORIG_ODF_TEST_DIR = TESTSRC + "test/java/org/odftoolkit/odfdom/";
var TEMP_ODF_TEST_DIR = TESTBASE + "test/java/org/odftoolkit/odfdom/";

exports.runCobertura = runCobertura;

/*var csvInfo = {};
csvInfo.lineName = "LineCoverage";
csvInfo.branchName = "BranchCoverage";
csvInfo.lineCSV = ""; //the results CSV string.
csvInfo.branchCSV = ""; //the results CSV string.
csvInfo.titleDone = false;

console.log("maven runner");*/

//sequenceTests();
//resultsToCSV();

function doMaven(test, key, callback) {
	console.log("Do the stuff for " + test);
	shelljs.cp('-Rf', TESTBASE + test, TESTSRC + test);
	runCobertura(ODFDOM, test, callback);
	console.log("Done");
}

function allDone(err) {
	console.log("All Done and I'm knackered");
	if (err) {
		console.log(err);
	} else {
		console.log("Results:" + results);
	}
}

/*for(var i=0; i<2; i++) {//i<numTests; i++) {
console.log(tests[i]);
//Add each test to the src test directory
shelljs.cp('-Rf', TESTBASE + tests[i], TESTSRC + tests[i]);
//Then run Cobertura
// log the test and corresponding coverage
// at the conclusion of the cobertura run

//need to make this synchronous...
//Use promises?
runCobertura(ODFDOM, tests[i], firstRun);
firstRun = false;
}*/

function runCobertura(testDirectory, test, key, callback) {
	console.log('maven coverage');
	var mvn = require('maven').create({
			cwd : testDirectory
		});
	//could run it and generate XML instead of the html - or both?
	//XML does not seem to be forthcoming... just live with html
	var mvnCommands = [];
	if (key === 0) {
		//mvnCommands.push('clean');
		mvnCommands.push('cobertura:cobertura');
	} else {
		mvnCommands.push('cobertura:cobertura');
	}

	mvn.execute(mvnCommands, {
		format : 'html'
	}).then(function (result) {
		console.log("Tests run\n");
		console.log("Now get the coverage statistics");
		coberParser.parseResultsToJSON(testDirectory, test, key);
		console.log("Parsing done");
		callback();
	}, function (err) {
		callback(err);
	});
}

/* Want to  start with a given directory of tests
 ** Copy it to a temporary location
 ** Iterate the tests by some increment
 **  copy the tests back
 **  run the tests and generate the coverage results
 **
 ** Somehow need to learn or also have a list of associated documents
 ** to be used in Production coverage
 */

// start from odfdom/src/test/java/org/odftoolkit/odfdom
function sequenceTests() {
	//start with - move the tests. use fs-extra
	console.log("Move tests from " + ORIG_ODF_TEST_DIR + " to " + TEMP_ODF_TEST_DIR);
	//Only want to move the tests....
	//If we move all of the dir then we lose the utility classes
	//Maybe we should have a filter on none tests and move them back
	//Can we do that?
	var otests = shelljs.ls('-R', ORIG_ODF_TEST_DIR).filter(function (file) {
			return file.match(/\Test.java$/);
		});
	var onumTests = otests.length;
	console.log("Original number of tests: " + onumTests);

	async.forEachOfSeries(otests, moveOrig, allMoved);
}

function moveOrig(test, key, callback) {
	fs.move(ORIG_ODF_TEST_DIR + test, TEMP_ODF_TEST_DIR + test, function (err) {
		if (err) {
			callback(err);
		} else {
			console.log("Moved " + ORIG_ODF_TEST_DIR + test + " to " + TEMP_ODF_TEST_DIR + test);
			callback()
		}
	});
}

function allMoved(err) {
	if (err) {
		console.log("Orig Move :" + err);
	} else {
		console.log("All original tests moved");
		var tests = shelljs.ls('-R', TEMP_ODF_TEST_DIR).filter(function (file) {
				return file.match(/\Test.java$/);
			});
		var numTests = tests.length;
		console.log("Num Tests: " + tests.length);

		//so now move them back and test each set
		async.forEachOfSeries(tests, moveBackAndTest, allTested);
	}
}

function moveBackAndTest(test, key, callback) {
	fs.move(TEMP_ODF_TEST_DIR + test, ORIG_ODF_TEST_DIR + test, function (err) {
		if (err) {
			callback(err);
		} else {
			console.log("Returned " + TEMP_ODF_TEST_DIR + test + " to " + ORIG_ODF_TEST_DIR + test);
			var otests = shelljs.ls('-R', ORIG_ODF_TEST_DIR).filter(function (file) {
					return file.match(/\Test.java$/);
				});
			var onumTests = otests.length;
			runCobertura(ODFDOM, test, key, callback);
		}
	});
}

function allTested(err) {
	if (err) {
		console.log("All tested :" + err);
	} else {
		console.log("All tests performed");
	}
}

function resultsToCSV() {
	console.log("Generate CSV of results");
	async.waterfall([
			getResultsFiles,
			openLineCSV,
			openBranchCSV,
			getCSVResults,
			writeLineResults,
			writeBranchResults,
			closeLineCSV,
			closeBranchCSV
		], resultsDone);
}

function resultsDone(err, result) {
	if (err) {
		console.log(err);
	} else {
		console.log("CSV " + result);
	}
}

function getResultsFiles(callback) {
	csvInfo.resultFiles = shelljs.ls('-R', "results").filter(function (file) {
			return file.match(/.json$/);
		});
	callback(null);
}

function openLineCSV(callback) {
	var numResults = csvInfo.resultFiles.length;
	console.log("Num results files: " + numResults);
	console.log("open " + csvInfo.lineName);
	fs.open(csvInfo.lineName + ".csv", "w", function (err, fd) {
		if (err) {
			callback(err);
		} else {
			csvInfo.lineFD = fd;
			callback(null);
		}
	});
}

function openBranchCSV(callback) {
	console.log("open " + csvInfo.branchName);
	fs.open(csvInfo.branchName + ".csv", "w", function (err, fd) {
		if (err) {
			callback(err);
		} else {
			csvInfo.branchFD = fd;
			callback(null);
		}
	});
}

function getCSVResults(callback) {
	async.forEachOfSeries(csvInfo.resultFiles, addToCSV, allAdded);
	callback(null);
}

function writeLineResults(callback) {
	fs.write(csvInfo.lineFD, csvInfo.lineCSV, function (err) {
		if (err) {
			callback(err);
		} else {
			callback(null);
		}
	});
}

function writeBranchResults(callback) {
	fs.write(csvInfo.branchFD, csvInfo.branchCSV, function (err) {
		if (err) {
			callback(err);
		} else {
			callback(null);
		}
	});
}

/**
 * Offer some sort of filtering mechanism here?
 * or leave to presentation stage?
 * Generate separate line and Branch coverage reports?
 * -same funtion but parameter to determine which to generate
 *
 */
function addToCSV(resultFile, key, callback) {
	console.log("Generate CSV from " + resultFile);
	var results = JSON.parse(fs.readFileSync("results/" + resultFile, 'utf8'));

	var numDataElements = results.data.length;
	if (csvInfo.titleDone === false) {
		csvInfo.lineCSV += "Test";
		csvInfo.branchCSV += "Test";
		for (var e = 0; e < numDataElements; e++) {
			csvInfo.lineCSV += "," + results.data[e].name;
			csvInfo.branchCSV += "," + results.data[e].name;
		}
		csvInfo.lineCSV += "\n";
		csvInfo.branchCSV += "\n";
		csvInfo.titleDone = true;
	}
	csvInfo.lineCSV += results.test;
	csvInfo.branchCSV += results.test;
	for (var e = 0; e < numDataElements; e++) {
		var values = results.data[e].values;
		csvInfo.lineCSV += "," + values[0];
		var bc = values[2];
		if (bc === "N/A") {
			bc = 0;
		}
		csvInfo.branchCSV += "," + bc;
	}
	csvInfo.lineCSV += "\n";
	csvInfo.branchCSV += "\n";
	callback();
}

function allAdded(err) {
	console.log("All Added to CSVs");
}

function closeLineCSV(callback) {
	fs.close(csvInfo.lineFD, function (err) {
		if (err) {
			callback(err);
		} else {
			console.log("Close Line CSV");
			callback(null);
		}
	});
}

function closeBranchCSV(callback) {
	fs.close(csvInfo.branchFD, function (err) {
		if (err) {
			callback(err);
		} else {
			console.log("Close Branch CSV");
			callback(null);
		}
	});
}
