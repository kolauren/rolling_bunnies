/**
 * This class controls the animation.
 * It is responsible for storing an array of Commits, starting and stopping the animation, 
 * initializing the dependency wheel, etc
**/
Animation = function(options) {
    this.options = $.extend({}, this.options, options);
    var self = this;
    this.init();
}

Animation.prototype = {
    utils: Utils.getInstance(),

    options: {
        selector: "",
        json: "",
        startButton: "",
        slider: ""
    },
    commits: [],
    interval: 1500,
    timer: null,
    dependencyWheel: null,
    frame: 0,

    init: function() {
        this.dependencyWheel = new DependencyWheel({ selector: this.options.selector});
        var self = this;
        
        this.utils.processCommitData(function(commits){
            self.commits = commits;
            var currentEdges = self.commits[self.commits.length - 1].currentEdges;
            var currentNodes = self.commits[self.commits.length - 1].currentNodes;
            self.dependencyWheel.draw(currentNodes, currentEdges);
        });

        $(this.options.startButton).click(function(){
            self.startAnimation();
            $(self.options.startButton).attr("disabled", true);
        });
        
        d3.select(this.options.slider).call(d3.slider().on("slide", function(evt, value) {
            // do something
        
        }));
    },

    startAnimation: function() {
        var self = this;
        self.frame = 0;
        self.timer = setTimeout(function(){
            self.animationCallback();
        }, self.interval);
    },

    // animate commits in different intervals
    animationCallback: function() {
        var self = this;
        self.animateCommits();
        self.frame++;
        clearTimeout(self.timer);
        if(self.frame < self.commits.length) 
            self.timer = setTimeout(function(){
                self.animationCallback();
            }, self.interval);
    },

    pauseAnimation: function() {

    },

    // Jumps to a specific frame in the animation
    jumpTo: function(frame) {
    },

    // clears the canvas of nodes and edges
    clearWheel: function() {
        d3.selectAll("circle").remove();
        d3.selectAll("path.edge").remove();
    },

    // redraws the dependency graph
    redraw: function () {
        //this.dependencyWheel.draw(tree);
    },

    animateCommits: function () {
        var commit = this.commits[this.frame];
        console.log(commit);
        this.dependencyWheel.lightUp(commit.addedJavaFiles.concat(commit.modifiedJavaFiles), commit.removedJavaFiles, commit.dependencies);
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

};


