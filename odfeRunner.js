var spawn = require('child_process').spawn;
var odfeResults = require('./odfeResults');
var fs = require('fs-extra');

var ODFEXPLORER_DIR = "../ODFExplorer/";
var PRODCOV = "prodcov";
var testIncrement = {};

var docsBase = "";

function setDocsBaseDir(dir) {
	docsBase = dir;
}

function setTestIncrement(inc) {
	testIncrement = inc;
}

function run(test, key, callback) {
	var cmdArgs = ['-jar', 'odfe.jar'];
	cmdArgs.push('-a');
	cmdArgs.push('-o');
	cmdArgs.push(PRODCOV); //make this a set by the comparisonrun
	cmdArgs.push('-f');

    var docFullName = "";

	console.log("Run ODFE for " + testIncrement.test);
	var docsin = testIncrement.docsin;
	console.log("Number of Docs in " + docsin.length);
	for (var i = 0; i < docsin.length; i++) {
        docFullName = docsBase + docsin[i];
		console.log(docFullName);
		//this is going to need a relative path...
		//to the ODFEXPLORER_DIR?
        
        //check existance of the document
        if(exists(docFullName)) {
            cmdArgs.push(docFullName);
        }
	}
	var docsout = testIncrement.docsout;
	if (docsout != null) {
		console.log("Number of Docs out " + docsout.length);
		for (var i = 0; i < docsout.length; i++) {
            docFullName = docsBase + docsout[i];
			console.log(docFullName);
            if(exists(docFullName)) {
                cmdArgs.push(docFullName);
            }
		}
	}

	runodfe(test, key, cmdArgs, callback);
}

// We need to build up the cmdArgs to aggregate into a given
// directory (or will it default)
// also need to make working directory something that of the odfe tool
// hard code the aggregation file name to prodcov?
// or make settable.... latter better... can then have odf and simple tests separate

var runodfe = function (test, key, cmdArgs, callback) {
	console.log('process ' + cmdArgs.toString());
	ls = spawn('java', cmdArgs, {
			cwd : ODFEXPLORER_DIR
		});

	ls.stdout.on('data', function (data) {
		console.log('stdout: ' + data);
	});

	ls.stderr.on('data', function (data) {
		console.log('stderr: ' + data);
	});

	ls.on('close', function (code) {
		console.log('child process exited with code ' + code);
		//examine code  > 0 we have an error
		if (code > 0) {
			callback(code);
		} else {
			odfeResults.generateJSON(ODFEXPLORER_DIR, PRODCOV, test, key, callback);
			//			callback();
		}
	});
}

function exists(file) {
    return fs.existsSync(file);
}

module.exports.run = run;
module.exports.setTestIncrement = setTestIncrement;
module.exports.setDocsBaseDir = setDocsBaseDir;
