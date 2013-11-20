/**
 * This class controls the animation.
 * It is responsible for storing an array of Commits, starting and stopping the animation, 
 * initializing the dependency wheel, etc
**/
Matrix = function(options) {
    this.options = $.extend({}, this.options, options);
    var self = this;
    this.init();
}

Matrix.prototype = {

    options: {
        selector: "", /* Selector for element in the dom */
        methodMatrix: [],
        methodNames: []
    },
    
    init: function() {
        
        var cell_width = 16;
        var cell_gap = 1;
        var maxCount = 10;
        var matrixWidth = 300;
        var opacity = d3.scale.linear()
            .domain([0, maxCount])
            .range([0, 1]);
 
        console.log(this.options.methodMatrix);
        console.log(this.options.methodNames);
        // Create the initial matrix given array of method names
        var grid = d3.select(this.options.selector)
            .append("svg:svg")
            .attr("width", 200 + this.options.methodNames.length*(cell_width + cell_gap))
            .attr("height", 200 + this.options.methodNames.length*(cell_width + cell_gap))
            .append("g")
            .classed("grid", true)
            .attr("transform", "translate(100, 130)");
        
        // Add the rows and fill colour based on count/frequency
        var rows = grid.selectAll("g")
            .data(this.options.methodMatrix)
            .enter()
            .append("g");
    
        rows.selectAll("rect")
            .data(function(d) { return d;})
            .enter()
            .append("rect")
            .attr("x", function(d, i, j) {return j*(cell_width+cell_gap);})
            .attr("y", function(d, i, j) {return i*(cell_width+cell_gap);})
            .attr("width", function(d, i, j) {return cell_width;})
            .attr("height", function(d, i, j) {return cell_width;})
            .style("fill", 'blue')
            .style("opacity", opacity);
        
        // add method labels
        var method_labels_x =  d3.select("#matrix").select("svg")
            .append("g")
            .classed("method labels x", true)
            .attr("transform", "translate(112, 125)");
        
        method_labels_x.selectAll("text")
            .data(this.options.methodNames)
            .enter()
            .append("text")
            .attr("transform", function(d, i) {return "translate("+i*(cell_width+cell_gap)+", 0) rotate(270)";})
            .text(function(d) { return d; });
        
      var method_labels_y = d3.select("#matrix").select("svg")
        .append("g")
        .classed("method labels y", true)
        .attr("transform", "translate(-70, 145)");
        
      method_labels_y.selectAll("text")
          .data(["methodA","methodB","methodC"])
          .enter().append("text")
          .attr("x", 95)
          .attr("y", function(d, i) {return i*(cell_width+cell_gap);})
          .text(function(d) {return d;});
    },
    
    // Given new array of count values, 
    // this function recalculates new colours/saturation of cells and returns new matrix
    recalculate: function(matrix) {
        
        
    }
    
}


