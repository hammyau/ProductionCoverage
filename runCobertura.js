/*
 *  Call maven to run cobertura
 */

exports.testThis = testThis;

function testThis(testDirectory, test, key, callback) {
	console.log('maven coverage');
	var mvn = require('maven').create({
			cwd : testDirectory
		});
	//could run it and generate XML instead of the html - or both?
	//XML does not seem to be forthcoming... just live with html
	var mvnCommands = [];
	if (key === 0) {
		mvnCommands.push('clean');
		mvnCommands.push('cobertura:cobertura');
	} else {
		mvnCommands.push('cobertura:cobertura');
	}

	mvn.execute(mvnCommands, {
		format : 'html'
	}).then(function (result) {
		console.log("Tests run\n");
		callback();
	}, function (err) {
		callback(err);
	});
}

