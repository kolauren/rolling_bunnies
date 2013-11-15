/**
 * This class controls the animation.
 * It is responsible for storing an array of Commits, starting and stopping the animation, 
 * initializing the dependency wheel, etc
**/
Animation = function(options) {
    this.options = $.extend({}, this.options, options);
    this.init();
}

Animation.prototype = {
    utils: Utils.getInstance(),

    options: {
        selector: "",
        json: "",
        startButton: "",
        pauseButton: "",
        slider: ""
    },
    commits: [],
    interval: 1500,
    timer: null,
    dependencyWheel: null,
    frame: 0,
    sliderPosition: 0,
    paused: false,

    init: function() {
        this.dependencyWheel = new DependencyWheel({ selector: this.options.selector});
        var self = this;
        $(self.options.pauseButton).attr("disabled", true);
        
        this.utils.processCommitData(self.options.json, function(commits, final_state, matrix_order){
            console.log(commits);
            self.commits = commits;
            self.dependencyWheel.draw(final_state);
        });

        // click event for start button
        $(this.options.startButton).click(function(){
            // clear wheel (assume start at beginning)
            if(self.paused) {
                self.paused = false;
            } else {
                self.frame = self.sliderPosition;
            } 
            $(self.options.startButton).attr("disabled", true);
            $(self.options.pauseButton).attr("disabled", false);
            self.startAnimation();
            
        });
        
        // click event for pause button
        $(this.options.pauseButton).click(function(){
            // set paused flag true
            console.log("paused");
            self.paused = true;
            $(self.options.startButton).attr("disabled", false);
            $(self.options.pauseButton).attr("disabled", true);
        });
        
        // event for slider
        d3.select(this.options.slider).call(d3.slider().on("slide", function(evt, value) {
            self.clearWheel();
            var commitToStartAt = value * 0.01 * (self.commits.length - 1);
            commitToStartAt = Math.round(commitToStartAt);
            var commit = self.commits[commitToStartAt];
            self.frame = commitToStartAt;
            self.sliderPosition = commitToStartAt;
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
        if(self.paused === true)
            return;
        self.animateCommits();
        self.frame++;
        self.updateSliderPosition(self.frame);
        clearTimeout(self.timer);
        if(self.frame < self.commits.length) 
            self.timer = setTimeout(function(){
                self.animationCallback();
            }, self.interval);
        else {
          $(self.options.startButton).attr("disabled", false);
          $(self.options.pauseButton).attr("disabled", true);
        }
    },

    
    // updates the position of the slider as the animation progresses
    updateSliderPosition: function(frame) {
      var self = this;
      var newSliderPosition = frame/(0.01*self.commits.length);
      d3.select("#slider").select(".d3-slider-handle")
        .style("left","" + newSliderPosition + "%");  
    },
    
    // set opacity of all nodes and edges to 0
    clearWheel: function() {
        d3.selectAll("circle")
            .style('opacity', 0.2); 
        d3.selectAll(".edge")
            .style('opacity', 0.2); 
        d3.selectAll(".arrow")
            .style('opacity', 0.2); 
    },

    animateCommits: function () {
        this.clearWheel();
        var commit = this.commits[this.frame];
        console.log(commit);
        this.dependencyWheel.lightUp(commit);
        $(".info").animate({opacity:0}, 400, function() {
            $(this).text("Commit #: " + commit.commit_SHA).animate({opacity: 1});
        });
    }

};


