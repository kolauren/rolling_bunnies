AnimatedWheel = function(options) {
    this.options = $.extend({}, this.options, options);
    var self = this;
    $(this.options.startButton).click(function(){
        self.startAnimation();
    });
    this.init();
}

$.extend(AnimatedWheel.prototype, DependencyWheel.prototype, {
    currentNodes: [],
    currentDependencies: [],

    // will be refreshed every commit for animations
    addedJavaFiles: [],
    modifiedJavaFiles: [],
    removedJavaFiles: [],
    renamedJavaFiles: [],
    dependenciesAffected: [],
    commits: [],
    interval: 1500,
    timer: null,

    init: function() {
        // invoke super class
        DependencyWheel.prototype.init.call(this);
        var self = this;
        $(this.options.startButton).click(function(){
            self.startAnimation();
        });
    },

    startAnimation: function() {
        var self = this;
        self.clearWheel();
        $.getJSON("commitTest1.json", function(resp) {
            self.commits = resp.reverse();
            self.timer = setTimeout(function(){
                self.animationCallback();
            }, self.interval);
        });
    },

    // animate commits in different intervals
    animationCallback: function() {
        var self = this;
        console.log("callback " + this.commits.length);
        self.animateCommits();
        clearTimeout(self.timer);
        if(this.commits.length > 0) 
            self.timer = setTimeout(function(){
                self.animationCallback();
            }, self.interval);
    },

    // clears the canvas of nodes and edges
    clearWheel: function() {
        d3.selectAll("circle").remove();
        d3.selectAll("path.edge").remove();
    },

    // returns a list of dependencies of given class
    getDependencies: function(className){
        var dependencies = [];
        this.currentDependencies.forEach(function (d){
           if(d[0] === className)
               dependencies.push(d[1]);
        });
        return dependencies;
    },

    // creates the tree for parsing using the d3 cluster method
    createTree:  function(currentNodes) {
        var root = {name: "root", children: []};
        var self = this;
        currentNodes.forEach(function(n){
            var child = {name: n, imports: self.getDependencies(n)};
            root.children.push(child);
        });
        return root;
    },

    // redraws the dependency graph
    redraw: function () {
        this.clearWheel();
        var tree = this.createTree(this.currentNodes);
        this.draw(tree);
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
        var self = this;
        this.currentNodes = this.currentNodes.concat(commit.addedJavaFiles);
        var i = 0;
        for (i; i < commit.dependenciesAffected.length; i++) {
            if (!this.dependenciesAlreadyAdded(commit.dependenciesAffected[i][0],commit.dependenciesAffected[i][1])) {
                this.currentDependencies.push(commit.dependenciesAffected[i]);
            }
        }
        
        // remove the node from current node list
        commit.removedJavaFiles.forEach(function(deleted){
            self.currentNodes = self.currentNodes.filter(function(elem){
                return !(elem === deleted)
            });
        });
        
        // remove the dependencies of the removed classes
        commit.removedJavaFiles.forEach(function(deleted){
            self.currentDependencies = self.currentDependencies.filter(function(elem){
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
    }

});


