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
    impact_line: null
  },
    
  opacity: 0.4,
  tooltip: null,
  opacity_increment: 0.1,

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
      .radius(function(d){ return d.y - 10; })
      .angle(function(d){ return d.x / 180 * Math.PI })
      .interpolate("bundle");

    this.options.impact_line = d3.svg.line.radial()
      .radius(function(d){ return d.y - 10; })
      .angle(function(d){ return d.x / 180 * Math.PI })
      .tension(0.1)
      .interpolate("bundle");

    this.tooltip = d3.select("body").append("div")   
    .attr("class", "tooltip")               
    .style("opacity", 0);

  },
    
  classOver: function(t) {
      // highlight nodes
        d3.selectAll("g").selectAll("." + t.class)
            .style('opacity', 1);
      
      // highlight node labels
        $("." + t.class).siblings()
            .css("opacity", 1);
        
      // // Show tool tip with class name
      //   this.tooltip.transition().duration(200).style("opacity", .9);      
      //   this.tooltip.html(t.name)  
      //     .style("left", (d3.event.pageX) + "px")     
      //     .style("top", (d3.event.pageY - 50) + "px");
      
      // Search for dependencies connected to this node and thicken dependencies
      d3.select("g").selectAll("path").filter(".edge.source-" + t.method_id)
        .style('opacity', 1);
      
  },
    
  classOut: function(t) {
      var self = this;
      // de-stroke node
      d3.selectAll("g").selectAll("." + t.class)
        .style('opacity', self.opacity);
      //this.tooltip.transition().duration(500).style("opacity", 0); 
      
      // de-highlight dependencies
      d3.select("g").selectAll("path").filter(".edge.source-" + t.method_id)
        .style('opacity', self.opacity);
      
      // de-highlight node labels
        $("." + t.class).siblings()
            .css("opacity", 0.5);
      
  },

  // Draws all the nodes and edges based on input data
  draw: function(state, impact_edges) {
      var self = this;  
      var d3data = self.parseDataToD3(state, impact_edges);

      var splines = self.options.bundle(d3data.edges);
      var impact_splines = self.options.bundle(d3data.impact_edges);

      var paths = self.svg.selectAll(self.options.selector)
        .data(d3data.edges).enter().append("svg:path")
        .attr("class", function(d) { return "edge " + d.class; })
        .style("stroke", function(d) { 
          return self.utils.getColour(d.source.hue, 70, 60); })
        .style("opacity", self.opacity)
        .style("stroke-dasharray", ("3, 3"))
        .attr("d", function(d, i) { return self.options.line(splines[i]); });

      // Add a second layer of paths for animation purposes
      self.svg.selectAll(self.options.selector)
        .data(d3data.edges).enter().append("svg:path")
        .attr("class", function(d) { return "edge animate " + d.class; })
        .style("stroke", function(d) { 
          return self.utils.getColour(d.source.hue, 70, 60); })
        .style('opacity', 0)
        .attr("d", function(d, i) { return self.options.line(splines[i]); });

      var impact_paths = self.svg.selectAll(self.options.selector)
        .data(d3data.impact_edges).enter().append("svg:path")
        .attr("class", function(d) { return "impact_edge " + d.class; })
        .style("stroke", "#DDDDDD")
        .style("opacity", 0)
        .attr("d", function(d, i) { return self.options.impact_line(impact_splines[i]); });

      // this.svg.selectAll(".arrow")
      //   .data(d3data.edges).enter().append("svg:path")
      //   .attr("class", function(d) { return "arrow " + d.class; })
      //   .attr("d", d3.svg.symbol().type("triangle-up").size(128))
      //   .attr("transform", function(d) { return "rotate(" + (d.target.x - 90) + ")translate(" + (d.target.y) + ", -12) rotate(-25) translate(-20)" })
      //   .style("fill", function(d) { return self.utils.getColour(d.source.hue, 70, 60); })
      //   .style("opacity", 0.2);

      var node = self.svg.selectAll("g.node")
        .data(d3data.nodes.filter(function(n) { return !n.children; }))
        .enter().append("svg:g")
        .attr("class", "node")
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")"; });

      node.append("svg:circle")
        .attr("class", function(d) { return d.class })
        .attr("r", 10)
        .style("fill", function(d) { return self.utils.getColour(d.hue, 70, 60); })
        .style("opacity", self.opacity);
      node.append("svg:text")
          .attr("dx", function(d) { return d.x < 180 ? 15 : -15; })
          .attr("dy", "0.4em")
          .attr("class", "node-label")
          .attr("font-size", "15")
          .attr("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
          .attr("transform", function(d) { return d.x < 180 ? null : "rotate(180)"; })
          .text(function(d) { return d.method_name; })
          .attr("fill","white")
          .style("opacity", 0.7);

      
      //event handlers for nodes on mouse over
      d3.selectAll("svg").selectAll("circle")
        .on("mouseover", function(t) { self.classOver(t) })
        .on("mouseout", function(t) { self.classOut(t) });
      
      // d3.selectAll("svg").selectAll("circle").append("div")
      //   .attr("class", "tooltip")
      //   .style("opacity", 0);
      
  },

  animatePath: function(selector) {
    var path = this.svg.select(".animate" + selector);
    var totalLength = path.node().getTotalLength();
    path.attr("stroke-dasharray", totalLength + " " + totalLength)
        .attr("stroke-dashoffset", totalLength)
        .transition()
        .duration(1000)
        .ease("linear")
        .attr("stroke-dashoffset", 0)
        .style("opacity", 1);
  },

  lightUp: function(commit, callback) {
    var self = this;
    commit.nodes.forEach(function(n, i) {
      self.svg.selectAll(".node-" + n.method_id)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1200)
        .style('opacity', 1);
    });

    commit.edges.forEach(function(e, i) {
      self.animatePath(".source-" + e.source + ".target-" + e.target);
    });

    commit.impact_edges.forEach(function(e, i) {
      self.svg.selectAll(".impact_edge.source-" + e.source + ".target-" + e.target)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1200)
        .style('stroke-width', function(d) { return e.count * 2; })
        .style('opacity', 0.5);
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
  parseDataToD3:  function(state, impact_edges) {
      var self = this;
      var colours = [];
      var edges = [];
      var cluster_map = {};
      var d3_impact_edges = [];

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
      // also create impact edges
      nodes.forEach(function(n) {
        if(n.adjacent) {
          n.adjacent.forEach(function(a){
            var css_class = "source-" + n.method_id + " target-" + a;
            edges.push({ source: cluster_map[n.method_id], target: cluster_map[a], "class": css_class });
          });
        }
      });

      for(key in impact_edges) {
        var source = impact_edges[key].source;
        var target = impact_edges[key].target;
        var css_class = "source-" + source + " target-" + target;
        d3_impact_edges.push({ source: cluster_map[source], target: cluster_map[target], "class": css_class });
      }

      return {
        nodes: nodes,
        edges: edges,
        impact_edges: d3_impact_edges
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