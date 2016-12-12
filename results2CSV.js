// Convert the results files to CSV so we can use Excel to see what is going on.
var async = require('async');
var shelljs = require('shelljs');
var fs = require('fs-extra');

var csvInfo = {};
csvInfo.lineName = "LineCoverage";
csvInfo.branchName = "BranchCoverage";
csvInfo.prodName = "ProductionCoverage";
csvInfo.lineCSV = ""; //the results CSV string.
csvInfo.branchCSV = ""; //the results CSV string.
csvInfo.prodCSV = "";
csvInfo.titleDone = false;

function generate() {
	console.log("Generate CSV of results");
	async.waterfall([
			getCovResultsFiles,
			getProdResultsFiles,
			openLineCSV,
			openBranchCSV,
			openProdCovCSV,
			getCovCSVResults,
			getProdCSVResults,
			writeLineResults,
			writeBranchResults,
			writeProdResults,
			closeLineCSV,
			closeBranchCSV,
            closeProdCovCSV
		], resultsDone);
}

function resultsDone(err, result) {
	if (err) {
		console.log(err);
	} else {
		console.log("CSV " + result);
	}
}

function getCovResultsFiles(callback) {
	csvInfo.covResultsFiles = shelljs.ls('-R', "results").filter(function (file) {
			return file.match(/covresult/);
		});
	callback(null);
}

function getProdResultsFiles(callback) {
	csvInfo.prodResultsFiles = shelljs.ls('-R', "results").filter(function (file) {
			return file.match(/pcresult/);
		});
	callback(null);
}

function openLineCSV(callback) {
	var numResults = csvInfo.covResultsFiles.length;
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

function openProdCovCSV(callback) {
	console.log("open " + csvInfo.prodName);
	fs.open(csvInfo.prodName + ".csv", "w", function (err, fd) {
		if (err) {
			callback(err);
		} else {
			csvInfo.prodFD = fd;
			callback(null);
		}
	});
}

function getCovCSVResults(callback) {
	async.forEachOfSeries(csvInfo.covResultsFiles, addToCovCSV, allAdded);
    csvInfo.titleDone = false;
	callback(null);
}

function getProdCSVResults(callback) {
	async.forEachOfSeries(csvInfo.prodResultsFiles, addToProdCSV, allAdded);
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

function writeProdResults(callback) {
	fs.write(csvInfo.prodFD, csvInfo.prodCSV, function (err) {
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
function addToCovCSV(resultFile, key, callback) {
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

function addToProdCSV(resultFile, key, callback) {
	console.log("Generate CSV from " + resultFile);
	var results = JSON.parse(fs.readFileSync("results/" + resultFile, 'utf8'));

	var numDataElements = results.data.length;
    console.log("num els " + numDataElements);
	if (csvInfo.titleDone === false) {
		csvInfo.prodCSV += "Test";
		for (var e = 0; e < numDataElements; e++) {
			csvInfo.prodCSV += ",el_" + results.data[e].name;
			csvInfo.prodCSV += ",attr_" + results.data[e].name;
		}
		csvInfo.prodCSV += "\n";
		csvInfo.titleDone = true;
	}
	csvInfo.prodCSV += results.test;
	for (var e = 0; e < numDataElements; e++) {
		csvInfo.prodCSV += "," + results.data[e].elsPct;
		csvInfo.prodCSV += "," + results.data[e].attrPct;
	}
	csvInfo.prodCSV += "\n";
    console.log(csvInfo.prodCSV);
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

function closeProdCovCSV(callback) {
	fs.close(csvInfo.prodFD, function (err) {
		if (err) {
			callback(err);
		} else {
			console.log("Close Production Coverage CSV");
			callback(null);
		}
	});
}

module.exports.generate = generate;