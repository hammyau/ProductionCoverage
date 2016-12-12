// Comparison Managager
//
// Can make CoverageIncrements
//
// Take a list of CoverageIncrements and get the corresponding
// code coverage and production coverage statics.
//
// Want to aggregate a set of CoverageIncrements so we can graph

var CONFIG_FILE = "config.json";
var TESTS_FILE = "covIncrements.json";

var TEST_SOURCE_DIR = "src/test/java/";
var TEST_CLASSES_DIR = "target/test-classes/";
var TEST_STORE = "testStore/";
var TEST_INCREMENTS_DIR = "tests";

var origTestDir = "";

var argv = require('yargs')
	.usage('Usage: $0 -c configFile -t file -r ')
	.demand('r')
	.choices('r', ['full', 'cov', 'odfe', 'list', 'restore', 'csv', 'split', 'testsDir'])
	.describe('r', 'run options')
	.describe('c', 'config file (default config.json)')
	.describe('t', 'test increments file (default covIncrements.json)')
	.epilog("ODF Testing for all")
	.argv;
var jsonfile = require('jsonfile');
var async = require('async');
var shelljs = require('shelljs');
var fs = require('fs-extra');
var testMover = require('./testMover');
var testRunner = require('./testRunner');
var odfeRunner = require('./odfeRunner');
var results2CSV = require('./results2CSV');

coverageInc = require('./coverageIncrement');

var configFile = CONFIG_FILE;
var testsFile = TESTS_FILE;
var testIncrementsDirectory = TEST_INCREMENTS_DIR;
var config = {};
var coverageTests = {};

//where from
var srcDir = "";
//to local directory
var trgDir = TEST_STORE;

//Given a config file?
//If not assume it is predefined -c for config
//
//Lets use a yargs to provide some command line options
// List tests
// supply test details
// List results

if (argv.c) {
	configFile = argv.c;
}

// Get the list of test increments
// That is a list of tests and their associated input and output documents
if (argv.t) {
	testsFile = argv.t;
}

if (argv.d) {
    testIncrementsDirectory = argv.d;
}
// Control a run
// Run the maven part
// Run the ODFE part
// Full run

console.log("Production Coverage Comparison Manager");
console.log("--------------------------------------\n");

if (argv.r) {
	readConfig(configFile);
	readTests(testsFile);
    origTestDir = config.odftoolkitProject + TEST_SOURCE_DIR + coverageTests.project;
    setupTestMover();
    setupTestRunner();
	switch (argv.r) {
	case "full":
		fullTests();
		break;

	case "cov":
		console.log("Run Coverage Tests Only");
		break;

	case "odfe":
		console.log("Run ODFE Tests Only");
		break;

	case "restore":
		moveThemBack();
		break;

	case "list":
		listTests();
		break;

	case "csv":
		generateCSVs();
		break;

    case "split":
		splitTests();
		break;
        
    case "testsDir":
        runTestsFromDir();
	}
}

// Expect the configuration data in "config.json"
function readConfig(cfgFile) {
	config = jsonfile.readFileSync(cfgFile);
	console.log("Config Parameters Read from " + cfgFile);
	console.log("ODF Toolkit: " + config.odftoolkitProject);
	console.log("ODFE: " + config.odfeBaseDirectory);
	console.log("\n");
}

function readTests(testsFile) {
	console.log("Test increments from " + testsFile);
	coverageTests = jsonfile.readFileSync(testsFile);
}

function setupTestRunner() {
    testRunner.setTestsToRun(coverageTests.increments);
	testRunner.setTrgDir(origTestDir);
	testRunner.setSrcDir(TEST_STORE);
    testRunner.setProjectDir(config.odftoolkitProject);
    testRunner.setTestClassesDirectory(config.odftoolkitProject + TEST_CLASSES_DIR);
    testRunner.setTestProject(coverageTests.project);
}

function setupTestMover() {
    testMover.setTestsDir(origTestDir);
    testMover.setStoreDir(TEST_STORE);    
}

function listTests() {
	console.log("Tests name: " + coverageTests.name);
	console.log("Project Directory: " + coverageTests.project);
	var numTests = coverageTests.increments.length;
	console.log("Number of tests: " + numTests);
	for (var t = 0; t < numTests; t++) {
		console.log(coverageTests.increments[t].test);
	}
}


// Get the original tests
// store the tests
//
// for the tests in the tests file
//      run coverage
//      run odfe
// restore the tests - not the ones in tests file?
//  they will have been already moves anyway?
//
// First cut just simulate the coverage and odfe
//
// should this be a queue of tasks to be run
//
function fullTests() {
	console.log("Run Full Tests");
    //should clear out old cobertura set file
    //and old Aggregations records
	async.series([
			testMover.getOrigTests,
			testMover.moveOrigTests,
			testRunner.runCoverage,
			testMover.getStoredTests
//			testMover.restoreTests
		], fullTestsDone);
}

function moveThemBack() {
	console.log("Move Tests back");
	async.series([
			testMover.getStoredTests,
			testMover.restoreTests
		], moveBackDone);
}

function fullTestsDone(err) {
	if (err) {
		console.log(err);
	} else {
		console.log("Full Test Run Completed");
	}
}

function moveBackDone(err) {
	if (err) {
		console.log(err);
	} else {
		console.log("Move back Completed");
	}
}

function generateCSVs() {
	console.log("\nGenerate Results CSV files");
	results2CSV.generate();
}

function splitTests() {
	console.log("Split Tests:");
	var numTests = coverageTests.increments.length;
	console.log("Number of tests: " + numTests);
	for (var t = 0; t < numTests; t++) {
        writeSingleTestToJSON(coverageTests.increments[t]);
	}
}

function writeSingleTestToJSON(entry) {
    var singleTest = {};
    singleTest.name = entry.test;
    singleTest.project = coverageTests.project;
    singleTest.increments = [];
    singleTest.increments.push(entry);
    jsonfile.writeFileSync("tests/" + entry.test + ".json", singleTest, {spaces: 2});
}

function runTestsFromDir() {
    console.log("\Run test increments from " + testIncrementsDirectory);
    var testIncrementFiles = shelljs.ls('-R', testIncrementsDirectory).filter(function (file) {
			return file.match(/.json$/);
		});
	var numTestIncrements = testIncrementFiles.length;
	console.log("Number of test increments: " + numTestIncrements);
    for(var t=0; t<numTestIncrements; t++) {
        console.log(t+1 + " " + testIncrementFiles[t]);
    }

}