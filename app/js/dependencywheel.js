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
    radius: 300,
    line: null, /* line function */
    bundle: null,
  },
    
    tooltip: null,

  init: function(options) {

    // create the main svg
    this.svg = d3.select(this.options.selector)
      .append("svg:svg")
      .attr("width", (this.options.radius * 2))
      .attr("height", (this.options.radius * 2.1))
      .append("svg:g")
      .attr("transform", "translate(" + (this.options.radius) + "," + (this.options.radius) + ")");

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
        d3.selectAll("g").selectAll("[data-name=" + t.name + "]")
            .style('opacity', 0.7)
            .attr("r", 15); 
        
      // Show tool tip with class name
        this.tooltip.transition().duration(200).style("opacity", .9);      
        this.tooltip.html(t.name)  
          .style("left", (d3.event.pageX) + "px")     
          .style("top", (d3.event.pageY - 50) + "px");
      
      // Search for dependencies connected to this node and thicken dependencies
      d3.select("g").selectAll(".edge").filter(".source-" + t.name)
        .style('opacity', 0.7)
        .style("stroke-width", 10);   
      
  },
    
  classOut: function(t) {
      // de-stroke node
      d3.selectAll("g").selectAll("[data-name=" + t.name + "]")
        .style('opacity', 0.2)
        .attr("r", 10);
      this.tooltip.transition().duration(500).style("opacity", 0); 
      
      // de-thicken dependencies
      d3.select("g").selectAll(".edge").filter(".source-" + t.name)
        .style('opacity', 0.2)
        .style("stroke-width", 4);   
      
  },

  // Draws all the nodes and edges based on input data
  draw: function(currentNodes, currentEdges) {
      var self = this;  
      var d3data = self.parseDataToD3(currentNodes, currentEdges);
      var splines = self.options.bundle(d3data.edges);

      var paths = self.svg.selectAll(self.options.selector)
        .data(d3data.edges).enter().append("svg:path")
        .attr("class", function(d) { return d.class; })
        .style("stroke", function(d) { 
          return self.utils.getColour(d.source.hue, 70, 60); })
        .style("opacity", 0.2)
        .attr("d", function(d, i) { return self.options.line(splines[i]); });

      self.svg.selectAll("g")
        .data(d3data.nodes.filter(function(n) { return !n.children; }))
        .enter().append("circle")
        .attr("r", 10)
        .attr("class", function(d) { return "node " + d.id; })
        .style("fill", function(d) { return self.utils.getColour(d.hue, 70, 60); })
        .style("opacity", 0.2)
        .attr("data-name", function(d) { return d.name; })
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")"; })
        .append("svg:title")
        .text(function(d) { return d.name; });
      
      // event handlers for nodes on mouse over
      d3.selectAll("svg").selectAll("circle")
        .on("mouseover", function(t) { self.classOver(t) })
        .on("mouseout", function(t) { self.classOut(t) });
      
      d3.selectAll("svg").selectAll("circle").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);
      
  },
    
    drawExistingNodesAndEdges: function(commit) {
      var self = this;
        
      // existing current nodes that should be present for this frame
      var existingNodes = commit.currentNodes;
      existingNodes.forEach(function(n) {
         d3.selectAll("g").selectAll("[data-name=" + n.name + "]")
            .style('opacity', 0.2);     
      });
      
      // existing current edges that should be present for this frame
      var existingEdges = commit.currentEdges;
      existingEdges.forEach(function(n) {
        self.svg.select(".edge.source-" + n.source + ".target-" + n.target)
            .style('opacity', 0.2);     
      });
        
    },
    
    
  lightUp2: function(commit) {
      var self = this;
      
      self.drawExistingNodesAndEdges(commit);
      
      //make added files opacity 1
      var addedFiles = commit.addedJavaFiles;
      addedFiles.forEach(function(n){
      var selector = ".node." + n;
      console.log(selector);
      self.svg.select(selector)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1250)
        .style('opacity', 1);
      });
      
      // make dependencies opacity 1
      var dependencies = commit.dependencies;
      dependencies.forEach(function(n) {
      self.svg.select(".edge.source-" + n.source + ".target-" + n.target)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1250)
        .style('opacity', 1);
      });
      
      // make deleted files and dependencies grey
      var removedFiles = commit.removedJavaFiles; 
      removedFiles.forEach(function(n) {
      d3.selectAll("g").selectAll("[data-name=" + n + "]")
        .style("fill", "grey")
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1250)
        .style('opacity', 1);
          
      d3.select("g").selectAll(".edge").filter(".source-" + n)
        .style("stroke", "grey");   
          
      d3.select("g").selectAll(".edge").filter(".target-" + n)
        .style("stroke", "grey"); 
      });
      
      // make modified dependencies opacity 1
      var modified = commit.modifiedJavaFiles;
      modified.forEach(function(n){
      var selector = ".node." + n;
      self.svg.select(selector)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1250)
        .style('opacity', 1);
      });
      
    },

  lightUp: function(modified, removed, dependencies) {
    var self = this;
    modified.forEach(function(n){
      var selector = ".node." + n;
      console.log(selector);
      self.svg.select(selector)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1250)
        .style('opacity', 1);
    });

    dependencies.forEach(function(n) {
      console.log(".edge.source-" + n.source + ".target-" + n.target);
      self.svg.select(".edge.source-" + n.source + ".target-" + n.target)
        .transition()
        .delay(function(d,i) { return i * 10; })
        .duration(1250)
        .style('opacity', 1);
    });

    d3.selectAll("path.edge").style("opacity", 0.2);
    d3.selectAll("circle.node").style("opacity", 0.2);
  },

  // parses the data so it will be appropriate to pass to D3
  parseDataToD3:  function(currentNodes, currentEdges) {
      var self = this;
      var colours = [];
      var edges = [];
      var cluster_map = {};

      // creating equidistant color math
      var increment = Math.ceil(360 / currentNodes.length);
      for(var i = 0; i < 360 && colours.length <= currentNodes.length; i += increment) {
        colours.push(i);
      }

      // adding a colour item to each node
      currentNodes.forEach(function(n) {
          if(n.oldName)
            n.id = n.oldName + " " + n.name;
          else n.id = n.name;
          n.hue = colours.pop();
      });

      // create the nodes from cluster
      var nodes = self.cluster.nodes({name: "root", children: currentNodes});
      // create a map of each node for easy reference
      nodes.forEach(function(d) {
        cluster_map[d.name]  = d;
      });

      // create edges from edge data
      currentEdges.forEach(function(n) {$
        if(cluster_map[n.source] && cluster_map[n.target]) {
          var css_class = "edge source-" + cluster_map[n.source].name + " target-" + cluster_map[n.target].name;
          if(cluster_map[n.source].oldName) css_class += " source-" + cluster_map[n.source].oldName;
          if(cluster_map[n.target].oldName) css_class += " target-" + cluster_map[n.target].oldName;
          edges.push({ source: cluster_map[n.source], target: cluster_map[n.target], "class": css_class });
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