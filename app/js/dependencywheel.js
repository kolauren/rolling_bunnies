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
  impact_mode: "multiline", // either "thickness" or "multiline"
  d3data: null,
  nodeGlow: null,

  init: function(options) {

      this.nodeGlow = glow("nodeGlow").rgb("#7f7f7f").stdDeviation(3);
      
    // create the main svg
    this.svg = d3.select(this.options.selector)
      .append("svg:svg")
      .attr("width", (this.options.radius * 2.5))
      .attr("height", (this.options.radius * 2.1))
      .append("svg:g")
      .attr("transform", "translate(" + (this.options.radius * 1.25) + "," + (this.options.radius) + ")")
      .call(this.nodeGlow);

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
            .style('opacity', 1)
            .style('stroke', 'white')
            .style('stroke-width', 2);
      
      // highlight node labels
        $("." + t.class).siblings()
            .css("opacity", 1);
        
      // // Show tool tip with class name
      //   this.tooltip.transition().duration(200).style("opacity", .9);      
      //   this.tooltip.html(t.name)  
      //     .style("left", (d3.event.pageX) + "px")     
      //     .style("top", (d3.event.pageY - 50) + "px");
      
      // Search for dependencies connected to this node and thicken dependencies
      d3.select("g").selectAll("path")
        .filter(".edge.source-" + t.method_id)
        .style('opacity', 1);
      
      d3.select("g").selectAll("path")
        .filter(".edge.target-" + t.method_id)
        .style('opacity', 1);
      
  },
    
  classOut: function(t) {
      var self = this;
      // de-stroke node
      d3.selectAll("g").selectAll("." + t.class)
        .style('opacity', self.opacity)
        .style('stroke-width', 0);
      //this.tooltip.transition().duration(500).style("opacity", 0); 
      
      // de-highlight dependencies
      d3.select("g").selectAll("path")
        .filter(".edge.source-" + t.method_id)
        .style('opacity', self.opacity);
      
      d3.select("g").selectAll("path")
        .filter(".edge.target-" + t.method_id)
        .style('opacity', 0.5);
      
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
          return self.utils.getColour(d.source.hue, 70, 70); })
        .style("opacity", self.opacity)
        .style("stroke-dasharray", ("3, 3"))
        .attr("d", function(d, i) { return self.options.line(splines[i]); });

      // Add a second layer of paths for animation purposes
      self.svg.selectAll(self.options.selector)
        .data(d3data.edges).enter().append("svg:path")
        .attr("class", function(d) { return "animate " + d.class; })
        .style("stroke", function(d) { 
          return self.utils.getColour(d.source.hue, 70, 70); })
        .style('opacity', 0)
        .attr("d", function(d, i) { return self.options.line(splines[i]); });

      if(self.impact_mode === "thickness"){
        var impact_paths = self.svg.selectAll(self.options.selector)
          .data(d3data.impact_edges).enter().append("svg:path")
          .attr("class", function(d) { return "impact_edge " + d.class; })
          .style("stroke", "#DDDDDD")
          .style("opacity", 0)
          .attr("d", function(d, i) { return self.options.impact_line(impact_splines[i]); });
      }

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
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")"; })
        .style("filter", "url(#nodeGlow)");

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
    if(self.impact_mode === "thickness") {
      commit.impact_edges.forEach(function(e, i) {
          self.svg.selectAll(".impact_edge.source-" + e.source + ".target-" + e.target)
            .transition()
            .delay(function(d,i) { return i * 10; })
            .duration(1200)
            .style('stroke-width', function(d) { return Math.pow(3, e.count); })
            .style('opacity', 0.5);
      });
    } else {
        var line = d3.svg.line.radial()
          .radius(function(d){ return d.y - 10; })
          .angle(function(d){ return d.x / 180 * Math.PI })
          .interpolate("bundle");
        commit.impact_edges.forEach(function(e, i) {
          var current_paths = $(".impact_edge.source-" + e.source + ".target-" + e.target).length;
          if(current_paths < e.count) {
            var num_paths_to_add = e.count - current_paths;
            for(var j = 1; j <= num_paths_to_add + 4; j++) {
              var data = [{ source: self.d3data.cluster_map[e.source], target: self.d3data.cluster_map[e.target] }];
              var splines = self.options.bundle(data);
              self.svg.selectAll(self.options.selector)
                .data(data).enter().append("svg:path")
                .attr("class", function(d) { return "impact_edge source-" + e.source + " target-" + e.target; })
                .style("stroke", "#DDDDDD")
                .style("filter", "url(#nodeGlow)")
                .style("opacity", 0.9)
                .attr("d", function(d, i) { 
                  var prev_spline = {};
                  var difference = 0;
                  var tension_const = 0.1;
                  for(var h = 0; h < splines[i].length; h++) {
                    if(splines[i][h].name !== "root") {
                      if(!$.isEmptyObject(prev_spline)) {
                        difference = Math.abs(prev_spline.x - splines[i][h].x);
                      }
                      prev_spline = splines[i][h];
                    }
                  }
                  if(difference === 180) tension_const = 0.8;
                    return line.tension(-j * tension_const)(splines[i]);
                });
            }
          }
        });
    }
  },

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

      self.d3data = {
        nodes: nodes,
        edges: edges,
        impact_edges: d3_impact_edges,
        cluster_map: cluster_map
      };

      return self.d3data;
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