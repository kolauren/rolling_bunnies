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
    sliderPosition: 0,

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
            // clear wheel (assume start at beginning)
            self.frame = self.sliderPosition;
            if(self.frame === 0)
                self.setWheelNodesAndEdgesInvisible();
            
            //$(self.options.startButton).attr("disabled", true);
        });
        
        d3.select(this.options.slider).call(d3.slider().on("slide", function(evt, value) {
            // run animation again from this point?
            self.setWheelNodesAndEdgesInvisible();
            var commitToStartAt = value * 0.01 * (self.commits.length - 1);
            commitToStartAt = Math.round(commitToStartAt);
            var commit = self.commits[commitToStartAt];
            console.log(commitToStartAt);
            self.dependencyWheel.drawExistingNodesAndEdges(commit);
            self.frame = commitToStartAt;
            self.sliderPosition = commitToStartAt;
            //self.startAnimation();
        
        }));
    },

    startAnimation: function() {
        var self = this;
        self.timer = setTimeout(function(){
            self.animationCallback();
        }, self.interval);
    },

    // animate commits in different intervals
    animationCallback: function() {
        var self = this;
        self.animateCommits();
        self.frame++;
        self.updateSliderPosition(self.frame);
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
    
    // updates the position of the slider as the animation progresses
    updateSliderPosition: function(frame) {
      var self = this;
      var newSliderPosition = frame/(0.01*self.commits.length);
      d3.select("#slider").select(".d3-slider-handle")
        .style("left","" + newSliderPosition + "%");  
    },

    // clears the canvas of nodes and edges
    clearWheel: function() {
        d3.selectAll("circle").remove();
        d3.selectAll("path.edge").remove();
    },
    
    // set opacity of all nodes and edges to 0
    setWheelNodesAndEdgesInvisible: function() {
        d3.selectAll("circle")
            .style('opacity', 0); 
        d3.selectAll("path.edge")
            .style('opacity', 0); 
        
    },

    animateCommits: function () {
        var commit = this.commits[this.frame];
        console.log(commit);
        this.dependencyWheel.lightUp2(commit);
        //this.dependencyWheel.lightUp(commit.addedJavaFiles.concat(commit.modifiedJavaFiles), commit.removedJavaFiles, commit.dependencies);
    }

};


