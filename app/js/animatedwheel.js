/**
 * This class creates a animated depedency wheel. inherits from DependencyWheel
**/
AnimatedWheel = function(options) {
    this.options = $.extend({}, this.options, options);
    var self = this;
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
            // process the commit data
            self.processCommitData();
            self.startAnimation();
            $(self.options.startButton).attr("disabled", true);
        });
    },
    
    
    dependencyExists: function(dependencyList, dependency) {
        exists = false;
        dependencyList.forEach(function(d) {
            if(d.source === dependency.source && d.target === dependency.target)
                exists = true;
        });
        return exists;
    },
    
    
    // TODO: REFACTOR!!
    processCommitData: function() {
        // new fields for pushing current states for each commit
        var self = this;
        var nodes = [];
        var edges = [];
        
        $.getJSON("commitsTest2.json", function(resp) {
            self.commits = resp;
            self.commits.forEach(function(c){
                // loop through added files
                // add added files to nodes array
                c.addedJavaFiles.forEach(function(a) {
                  nodes = nodes.concat({"name": a, "deleted": false});  
                    
                });
                //nodes = nodes.concat(c.addedJavaFiles);
                
                  // loop through dependencies
                // add dependency if it doesnt exist already in edges array
                c.dependencies.forEach(function (d){
                    if(!self.dependencyExists(edges, d))
                        //edges.push(d);   <====== fffffffffffffffffff youuuu!!!!!!
                        edges = edges.concat(d);
                });
                
                
                // loop through renamed files, rename files in nodes array
                c.renamedJavaFiles.forEach(function(r){
                    // search the current nodes list and replace the old with new name
                    var i = 0;
                    for(i; i<nodes.length; i++) {
                     if(nodes[i] === r.old) {
                         nodes[i] = r.new;
                         break;
                     }
                    }
                    
                    // rename the edge if it contains renamed node??
                    edges.forEach(function(e) {
                        // if the edge contains the node, rename it??
                        if(e.target === r.old)
                            e.target = r.new;
                        else if(e.source === r.old)
                            e.source = r.new;
                    });
                });
                
                // loop through removed files, set deleted flag in nodes array to true
                c.removedJavaFiles.forEach(function(d) {
                    var j = 0;
                    for(j; j<nodes.length; j++) {
                     if(nodes[j].name === d) {
                         nodes[j].deleted = "true";
                         continue;
                     }
                    }
                // set deleted flag to true for all associated edges
                    edges.forEach(function(e) {
                        // if the edge contains the node, set deleted flag = true
                        if(e.target === d || e.source === d)
                            e.deleted = "true";
                    });

                });
                
                // set commit object's currentNode and currentEdges fields
                c.currentNodes = nodes;
                c.currentEdges = edges;
            });
            
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
    },
    
    // TODO: creates a static wheel for the beginning of animation
    // static wheel contains all nodes/.java files that were added through commit history
    createStaticWheel: function() {
    
    },
    
    // TODO: animates static wheel by iterating through commits
    animateStaticWheel: function() {
        // lower opacity of nodes
        // loop through commits
    }

});


