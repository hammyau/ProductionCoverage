// This beast knows how to run cobertura
// and then parse the results

module.exports.runCoverage = runCoverage;
module.exports.setTestsToRun = setTestsToRun;
module.exports.setSrcDir = setSrcDir;
module.exports.setTrgDir = setTrgDir;
module.exports.setProjectDir = setProjectDir;
module.exports.getSrcDir = getSrcDir;
module.exports.getTrgDir = getTrgDir;
module.exports.getProjectDir = getProjectDir;
module.exports.setTestClassesDirectory = setTestClassesDirectory;
module.exports.setTestProject = setTestProject;

var runCobertura = require('./runCobertura');
var fs = require('fs-extra');
var coberParser = require('./CoberHtmlParser');
var async = require('async');
var odfeRunner = require('./odfeRunner');

var currentTest = "";
var currentTestKey = 0;
var currentIncrement = {};

var testsToRun = [];

var srcDir = "";
var trgDir = "";
var projectDir = "";
var testClassesDirectory = "";
var testProject = "";

function setTestsToRun(tests) {
    testsToRun = tests;
}

function setSrcDir(dir) {
    srcDir = dir;
}

function setTrgDir(dir) {
    trgDir = dir;
}

function setProjectDir(dir) {
    projectDir = dir;
}

function getSrcDir() {
    return srcDir;
}

function getTrgDir() {
    return trgDir;
}

function getProjectDir() {
    return projectDir;
}

function setTestClassesDirectory(dir) {
    testClassesDirectory = dir;
}

function setTestProject(project) {
    testProject = project;
}

// Move the test back to the source
// Run cobertura
// Get the resulting coverage stats
function runTest(callback) {
    currentIncrement = testsToRun[0];
    odfeRunner.setTestIncrement(currentIncrement);
    odfeRunner.setDocsBaseDir(testClassesDirectory);
	currentTest = currentIncrement.test;
    console.log("\nRun test " + currentTest);
	fs.copy(srcDir + currentTest, trgDir + currentTest, function (err) {
		if (err) {
			callback(err);
		} else {
			console.log("To Run Moved " + srcDir + currentTest + " to " + trgDir + currentTest);
			testsToRun.shift();
            async.series([
                    cover,
                    parseCoverage,
                    odfe,
                    deleteTrgTest,
                    deleteTrgTestFromTarget
                ], function (err) {
                console.log("Run test done for " + currentTest);
                callback();
            });
		}
	});
}

function odfe(callback) {
    odfeRunner.run(currentTest, currentTestKey, callback);
    //callback();
}

function parseCoverage(callback) {
	console.log("Parse the results after " + currentTest);
	coberParser.parseResultsToJSON(projectDir, currentTest, currentTestKey, callback);
    //callback();
}

function deleteTrgTest(callback) {
    console.log("Delete " + trgDir + currentTest);
	fs.unlink(trgDir + currentTest, function (err) {
        if(err) {
            callback(err);
        } else {
            callback();
        }
    });   
}

function deleteTrgTestFromTarget(callback) {
    var testClass = testClassesDirectory + testProject + currentTest.replace(".java", ".class");
    console.log("Delete " + testClass);
	fs.unlink(testClass, function (err) {
        if(err) {
            callback(err);
        } else {
            callback();
        }
    });  
}

function areTestsToRun() {
//	console.log("testsToMove.length " + testsToMove.length);
	currentTestKey++;
	return testsToRun.length > 0;
}

function cover(callback) {
	console.log("Cover " + currentTest);
	runCobertura.testThis(projectDir, currentTest, currentTestKey, callback);
    //callback();
}

// Iterate through the testsToRun
function runCoverage(callback) {
	console.log("Run Coverage:");
	var numTests = testsToRun.length;
	console.log("Number of tests to run: " + numTests);
	//for each test move it back and run cobertura
	async.whilst(areTestsToRun, runTest, function (err) {
		if (err) {
			console.log("Coverage not completed " + err);
			callback(err);
		} else {
			console.log("\nAll coverage completed\n\n");
			callback(null);
		}
	});
}

