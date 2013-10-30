var b = b || {};
b.currentNodes = [];
b.currentDependencies = [];

// will be refreshed every commit for animations
b.addedJavaFiles = [];
b.modifiedJavaFiles = [];
b.removedJavaFiles = [];
b.renamedJavaFiles = [];
b.dependenciesAffected = [];


b.clearWheel = function() {
    d3.selectAll("circle").remove();
    d3.selectAll("path.edge").remove();
};

b.getDependencies = function(className){
    var dependencies = [];
    b.currentDependencies.forEach(function (d){
       if(d[0] === className)
           dependencies.push(d[1]);
    });
    return dependencies;
}


b.createTree = function(currentNodes) {
    var root = {name: "root", children: []};
    currentNodes.forEach(function(n){
        var child = {name: n, imports: b.getDependencies(n)};
        root.children.push(child);
    });
    return root;
};

// redraws the dependency graph
b.redraw = function () {
    b.clearWheel();
    var tree = b.createTree(b.currentNodes);
    g.dependencyWheel.actualDraw(tree);
}

// checks if the dependency is already added
b.dependenciesAlreadyAdded = function (source, destination) {
    var i = 0;
    for (i; i < b.currentDependencies.length; i++) {
        if (b.currentDependencies[i][0] === source) {
            if (b.currentDependencies[i][1] === destination) {
                return true;
            }
        }
        return false;
    }
};

// updates current node and connections 
// reads a commit and adds new nodes, removes nodes, 
b.update = function (commit) {
    b.currentNodes = b.currentNodes.concat(commit.addedJavaFiles);
    var i = 0;
    for (i; i < commit.dependenciesAffected.length; i++) {
        if (!b.dependenciesAlreadyAdded(commit.dependenciesAffected[i][0],commit.dependenciesAffected[i][1])) {
            b.currentDependencies.push(commit.dependenciesAffected[i]);
        }
    }
    
    commit.removedJavaFiles.forEach(function(deleted){
        b.currentNodes = b.currentNodes.filter(function(elem){
            return !(elem === deleted)
        });
    });
    
    // remove the dependencies of the removed classes
    commit.removedJavaFiles.forEach(function(deleted){
        b.currentDependencies = b.currentDependencies.filter(function(elem){
            return !((elem[0] === deleted) || (elem[1] === deleted))
        });
    });
    
    
    
    // flush out the old
    b.addedJavaFiles = [];
    b.modifiedJavaFiles = [];
    b.removedJavaFiles = [];
    b.renamedJavaFiles = [];
    // add the current data for current commit to be animated
    
    b.addedJavaFiles = b.addedJavaFiles.concat(commit.addedJavaFiles);
    b.modifiedJavaFiles = b.modifiedJavaFiles.concat(commit.modifiedJavaFiles);
    b.removedJavaFiles = b.removedJavaFiles.concat(commit.removedJavaFiles);
    b.renamedJavaFiles = b.renamedJavaFiles.concat(commit.renamedJavaFiles);
};

b.animateCommits = function () {
    
    var commit = b.commits.pop();
    b.update(commit);
    b.redraw();
    
    if(b.commits.length > 0) {
       setTimeout(b.animateCommits, 1000);   
    }
};



// draws the background circle thing
b.init = function () {
    b.clearWheel();
    $.getJSON("commitTest1.json", function(resp) {
      b.commits = resp.reverse();
      b.animateCommits();
    });
};

