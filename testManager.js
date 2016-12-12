// Test Manager 
//
// This beast knows how to manipulate the tests in both
// The ODFToolkit and the ODFE Explorer
//
// Tell it where the ODFToolKit is and where the ODFE is
// and it will orchestrate the tests
//
var fs = require('fs-extra');
var async = require('async');
var shelljs = require('shelljs');

var testsToMove = [];

var testsDir = "";
var storeDir = "";

var srcDir = "";
var trgDir = "";

function setStoreDir(dir) {
    storeDir = dir;
}

function setTestsDir(dir) {
    testsDir = dir;
}

function getStoreDir() {
    return storeDir;
}

function getTestsDir() {
    return testsDir;
}

// Get the list of original tests
function origTests(callback) {
	srcDir = testsDir;
	console.log("org from " + srcDir);
	testsToMove = shelljs.ls('-R', srcDir).filter(function (file) {
			return file.match(/\Test.java$/);
		});
	var onumTests = testsToMove.length;
	console.log("Original number of tests: " + onumTests);
	callback(null);
}

// Get the list of stored tests
function storedTests(callback) {
	srcDir = storeDir;
	console.log("stored in " + srcDir);
	testsToMove = shelljs.ls('-R', srcDir).filter(function (file) {
			return file.match(/\Test.java$/);
		});
	var numStoredTests = testsToMove.length;
	console.log("Number of stored tests: " + numStoredTests);
	callback(null);
}

function areTestsToMove() {
//	console.log("testsToMove.length " + testsToMove.length);
	return testsToMove.length > 0;
}

function moveOrigTests(callback) {
	trgDir = storeDir;
	console.log("Move tests from " + srcDir + " to " + trgDir);
	async.whilst(areTestsToMove, moveTest, function (err) {
		if (err) {
			callback(err);
		} else {
			allMoved(err);
			callback(null);
		}
	});
}

function restoreTests(rcallback) {
	trgDir = testsDir;
	srcDir = storeDir;
	console.log("Restore tests from " + srcDir + " to " + trgDir);
	async.whilst(areTestsToMove, moveTest, function (err) {
		if (err) {
			rcallback(err);
		} else {
			allRestored(err);
			rcallback(null);
		}
	});
}

function allRestored(err) {
	if (err) {
		console.log("Restore Move :" + err);
	} else {
		console.log("All original tests restored");
		var tests = shelljs.ls('-R', trgDir).filter(function (file) {
				return file.match(/\Test.java$/);
			});
		var numTests = tests.length;
		console.log("Num Tests: " + tests.length);
	}
}

function moveTest(callback) {
	var test = testsToMove[0];
	fs.move(srcDir + test, trgDir + test, function (err) {
		if (err) {
			callback(err);
		} else {
//			console.log("Moved " + srcDir + test + " to " + trgDir + test);
			testsToMove.shift();
			callback();
		}
	});
}

function allMoved(err) {
	if (err) {
		console.log("Orig Move :" + err);
	} else {
		console.log("All original tests moved");
		var tests = shelljs.ls('-R', trgDir).filter(function (file) {
				return file.match(/\Test.java$/);
			});
		var numTests = tests.length;
		console.log("Test Store Num Tests: " + tests.length + "\n\n");
	}
}


module.exports.restoreTests = restoreTests;
module.exports.moveOrigTests = moveOrigTests;
module.exports.origTests = origTests;
module.exports.storedTests = storedTests;
module.exports.setTestsDir = setTestsDir;
module.exports.setStoreDir = setStoreDir;
module.exports.getTestsDir = getTestsDir;
module.exports.getStoreDir = getStoreDir;