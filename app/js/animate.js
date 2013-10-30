Animate = function() {
    this.init();
};

$.extend(true, Animate.prototype, {
    currentNodes: [],
    currentDependencies: [],

    // will be refreshed every commit for animations
    addedJavaFiles: [],
    modifiedJavaFiles: [],
    removedJavaFiles: [],
    renamedJavaFiles: [],
    dependenciesAffected: [],

    // add logic here for initializing object and stuff
    init: function () {
    },

    start: function() {
        var self = this;
        self.clearWheel();
        $.getJSON("commitTest1.json", function(resp) {
          self.commits = resp.reverse();
          self.animateCommits();
        });
    },

    clearWheel: function() {
        d3.selectAll("circle").remove();
        d3.selectAll("path.edge").remove();
    },

    getDependencies: function(className){
        var dependencies = [];
        this.currentDependencies.forEach(function (d){
           if(d[0] === className)
               dependencies.push(d[1]);
        });
        return dependencies;
    },

    createTree:  function(currentNodes) {
        var root = {name: "root", children: []};
        currentNodes.forEach(function(n){
            var child = {name: n, imports: this.getDependencies(n)};
            root.children.push(child);
        });
        return root;
    },

    // redraws the dependency graph
    redraw: function () {
        this.clearWheel();
        var tree = this.createTree(this.currentNodes);
        globals.dependencyWheel.draw(tree);
    },

    // checks if the dependency is already added
    dependenciesAlreadyAdded: function (source, destination) {
        var i = 0;
        for (i; i < this.currentDependencies.length; i++) {
            if (this.currentDependencies[i][0] === source) {
                if (this.currentDependencies[i][1] === destination) {
                    return true;
                }
            }
            return false;
        }
    },

    // updates current node and connections 
    // reads a commit and adds new nodes, removes nodes, 
    update: function (commit) {
        is.currentNodes = this.currentNodes.concat(commit.addedJavaFiles);
        var i = 0;
        for (i; i < commit.dependenciesAffected.length; i++) {
            if (!this.dependenciesAlreadyAdded(commit.dependenciesAffected[i][0],commit.dependenciesAffected[i][1])) {
                this.currentDependencies.push(commit.dependenciesAffected[i]);
            }
        }
        
        commit.removedJavaFiles.forEach(function(deleted){
            this.currentNodes = this.currentNodes.filter(function(elem){
                return !(elem === deleted)
            });
        });
        
        // remove the dependencies of the removed classes
        commit.removedJavaFiles.forEach(function(deleted){
            this.currentDependencies = this.currentDependencies.filter(function(elem){
                return !((elem[0] === deleted) || (elem[1] === deleted))
            });
        });
        
        // flush out the old
        this.addedJavaFiles = [];
        this.modifiedJavaFiles = [];
        this.removedJavaFiles = [];
        this.renamedJavaFiles = [];
        // add the current data for current commit to be animated
        
        this.addedJavaFiles = this.addedJavaFiles.concat(commit.addedJavaFiles);
        this.modifiedJavaFiles = this.modifiedJavaFiles.concat(commit.modifiedJavaFiles);
        this.removedJavaFiles = this.removedJavaFiles.concat(commit.removedJavaFiles);
        this.renamedJavaFiles = this.renamedJavaFiles.concat(commit.renamedJavaFiles);
    },

    animateCommits: function () {
        
        var commit = this.commits.pop();
        this.update(commit);
        this.redraw();
        
        if(this.commits.length > 0) {
           setTimeout(this.animateCommits, 1000);   
        }
    },

});

