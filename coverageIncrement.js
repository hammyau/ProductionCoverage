// Coverage Increment
//
// Can't think of a better name for this thing just yet
// As part of the process of comparing code coverage and production coverage
// we need an object to track the individual tests and their associated documents.
//
// That is a CoverageIncrement
//  it just has a test and an array of documents
//      we could have some stuff like last run
//
// 

exports.createCI = createCI;

function createCI(test) {
    return new CoverageIncrement(test);
}

// CI Object
function CoverageIncrement(test) {
    this.test = test;
    this.docs = [];
    
    this.addDoc = addDoc;
}

function addDoc(doc) {
    this.docs.push(doc);
}



