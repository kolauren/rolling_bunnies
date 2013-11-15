/**
 * This class draws the dependency wheel ie interacts with D3
**/
DependencyWheel = function(options) {
  this.options = $.extend({}, this.options, options);
  this.init(options);
    
}

DependencyWheel.prototype = {
  utils: Utils.getInstance(),

  svg: null,

  cluster: null,

  options: {
    selector: "", /* class or id of the element containing the dependency wheel */
    radius: 250,
    line: null, /* line function */
    bundle: null,
  },
    
    tooltip: null,

  init: function(options) {

    // create the main svg
    this.svg = d3.select(this.options.selector)
      .append("svg:svg")
      .attr("width", (this.options.radius * 2.5))
      .attr("height", (this.options.radius * 2.1))
      .append("svg:g")
      .attr("transform", "translate(" + (this.options.radius * 1.25) + "," + (this.options.radius) + ")");

    // create the wheel
    this.svg.append("svg:path")
      .attr("class", "wheel")
      .attr("d", d3.svg.arc().outerRadius(this.options.radius)
        .innerRadius(0).startAngle(0).endAngle(2 * Math.PI));

    this.cluster = d3.layout.cluster()
      .size([360, (this.options.radius)])
      .sort(function(a, b) { return d3.ascending(a.name, b.name); });

    this.options.bundle = d3.layout.bundle();

    this.options.line = d3.svg.line.radial()
      .radius(function(d){ return d.y; })
      .angle(function(d){ return d.x / 180 * Math.PI })
      .interpolate("bundle");

    this.tooltip = d3.select("body").append("div")   
    .attr("class", "tooltip")               
    .style("opacity", 0);

  },
    
  classOver: function(t) {
        d3.selectAll("g").selectAll("." + t.class)
            .style('opacity', 1);
        
      // // Show tool tip with class name
      //   this.tooltip.transition().duration(200).style("opacity", .9);      
      //   this.tooltip.html(t.name)  
      //     .style("left", (d3.event.pageX) + "px")     
      //     .style("top", (d3.event.pageY - 50) + "px");
      
      // Search for dependencies connected to this node and thicken dependencies
      d3.select("g").selectAll(".edge").filter(".source-" + t.method_id)
        .style('opacity', 1);
      
  },
    
  classOut: function(t) {
      // de-stroke node
      d3.selectAll("g").selectAll("." + t.class)
        .style('opacity', 0.2);
      //this.tooltip.transition().duration(500).style("opacity", 0); 
      
      // de-thicken dependencies
      d3.select("g").selectAll(".edge").filter(".source-" + t.method_id)
        .style('opacity', 0.2);
      
  },

  // Draws all the nodes and edges based on input data
  draw: function(state) {
      var self = this;  
      var d3data = self.parseDataToD3(state);
      var splines = self.options.bundle(d3data.edges);

      var paths = self.svg.selectAll(self.options.selector)
        .data(d3data.edges).enter().append("svg:path")
        .attr("class", function(d) { return d.class; })
        .style("stroke", function(d) { 
          return self.utils.getColour(d.source.hue, 70, 60); })
        .style("opacity", 0.2)
        .attr("d", function(d, i) { return self.options.line(splines[i]); });

      var node = self.svg.selectAll("g.node")
        .data(d3data.nodes.filter(function(n) { return !n.children; }))
        .enter().append("svg:g")
        .attr("class", "node")
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")"; });

      node.append("svg:circle")
        .attr("class", function(d) { return d.class })
        .attr("r", 10)
        .style("fill", function(d) { return self.utils.getColour(d.hue, 70, 60); })
        .style("opacity", 0.2);
      node.append("svg:text")
          .attr("dx", function(d) { return d.x < 180 ? 15 : -15; })
          .attr("dy", "0.4em")
          .attr("class", "node-label")
          .attr("font-size", "15")
          .attr("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
          .attr("transform", function(d) { return d.x < 180 ? null : "rotate(180)"; })
          .text(function(d) { return d.method_name; });
      
      //event handlers for nodes on mouse over
      d3.selectAll("svg").selectAll("circle")
        .on("mouseover", function(t) { self.classOver(t) })
        .on("mouseout", function(t) { self.classOut(t) });
      
      // d3.selectAll("svg").selectAll("circle").append("div")
      //   .attr("class", "tooltip")
      //   .style("opacity", 0);
      
  },

  glow: function(selector) {
    this.svg.select(selector)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1250)
        .style('opacity', 1);
  },

  lightUp: function(commit) {
    var self = this;
    commit.nodes.forEach(function(n) {
      self.glow(".node-" + n.method_id);
    });

    commit.edges.forEach(function(e) {
      self.glow(".edge.source-" + e.source + ".target-" + e.target);
    });
  },
    
  // clean up later
  //   drawExistingNodesAndEdges: function(commit) {
  //     var self = this;
        
  //     // existing current nodes that should be present for this frame
  //     var existingNodes = commit.currentNodes;
  //     existingNodes.forEach(function(n) {
  //        d3.selectAll("g").selectAll("[data-name=" + n.name + "]")
  //           .style('opacity', 0.2);     
  //     });
      
  //     // existing current edges that should be present for this frame
  //     var existingEdges = commit.currentEdges;
  //     existingEdges.forEach(function(n) {
  //       self.svg.select(".edge.source-" + n.source + ".target-" + n.target)
  //           .style('opacity', 0.2);     
  //     });
        
  //   },
    
    
  // lightUp: function(commit) {
  //     var self = this;
      
  //     self.drawExistingNodesAndEdges(commit);
      
  //     //make added files opacity 1
  //     var addedFiles = commit.addedJavaFiles;
  //     addedFiles.forEach(function(n){
  //     var selector = ".node." + n;
  //     self.svg.select(selector)
  //       .style('opacity', 1);
  //     });
      
  //     // make dependencies opacity 1
  //     var dependencies = commit.dependencies;
  //     dependencies.forEach(function(n) {
  //     self.svg.select(".edge.source-" + n.source + ".target-" + n.target)
  //       .transition()
  //       .delay(function(d,i) { return i * 10; })
  //       .duration(1250)
  //       .style('opacity', 1);
  //     });
      
  //     // make deleted files and dependencies grey
  //     var removedFiles = commit.removedJavaFiles; 
  //     removedFiles.forEach(function(n) {
  //     d3.selectAll("g").selectAll("[data-name=" + n + "]")
  //       .style("fill", "grey")
  //       .style('opacity', 1);
          
  //     d3.select("g").selectAll(".edge").filter(".source-" + n)
  //       .style("stroke", "grey")  
  //       .transition()
  //       .delay(function(d,i) { return i * 10; })
  //       .duration(1250)
  //       .style('opacity', 1);
          
  //     d3.select("g").selectAll(".edge").filter(".target-" + n)
  //       .style("stroke", "grey")  
  //       .transition()
  //       .delay(function(d,i) { return i * 10; })
  //       .duration(1250)
  //       .style('opacity', 1); 
  //     });
      
  //     // make modified dependencies opacity 1
  //     var modified = commit.modifiedJavaFiles;
  //     modified.forEach(function(n){
  //     var selector = ".node." + n;
  //     self.svg.select(selector)
  //       .style('opacity', 1);
  //     });
      
  //   },

  // parses the data so it will be appropriate to pass to D3
  parseDataToD3:  function(state) {
      var self = this;
      var colours = [];
      var edges = [];
      var cluster_map = {};

      // creating equidistant color math
      var increment = Math.ceil(360 / state.values().length);
      for(var i = 0; i < 360 && colours.length <= state.values().length; i += increment) {
        colours.push(i);
      }

      // adding a colour item to each node
      state.forEach(function(k, n) {
          n.class = "node-" + n.method_id;
          n.hue = colours.pop();
      });

      // create the nodes from cluster
      var nodes = self.cluster.nodes({name: "root", children: state.values()});
      // create a map of each node for easy reference
      nodes.forEach(function(d) {
        cluster_map[d.method_id]  = d;
      });

      // create edges from edge data
      nodes.forEach(function(n) {
        if(n.adjacent) {
          n.adjacent.forEach(function(a){
            var css_class = "edge source-" + n.method_id + " target-" + a;
            edges.push({ source: cluster_map[n.method_id], target: cluster_map[a], "class": css_class });
          });
        }
      });

      return {
        nodes: nodes,
        edges: edges
      };
  },

  // returns a list of dependencies of given class
  // helper function for parsing data
  getDependencies: function(className, currentEdges){
      var dependencies = [];
      currentEdges.forEach(function (d){
         if(d.target === className)
             dependencies.push(d.source);
      });
      return dependencies;
  }
};