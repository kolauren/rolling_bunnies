/**
 * This module creates a dependency wheel based on a range of options
**/
c.DependencyWheel = function(options) {
  this.options = $.extend({}, this.options, options);
  this.init(options);
}

$.extend(true, c.DependencyWheel.prototype, {
  utils: c.Utils.getInstance(),

  svg: null,

  cluster: null,

  options: {
    json: "",
    selector: "", /* class or id of the element containing the dependency wheel */
    radius: 300,
    line: null, /* line function */
    bundle: null
  },

  init: function(options) {

    // create the main svg
    this.svg = d3.select(this.options.selector)
      .append("svg:svg")
      .attr("width", (this.options.radius * 2))
      .attr("height", (this.options.radius * 2))
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

    this.draw();
  },

  actualDraw: function(data) {
      var self = this;
      var nodes = self.getNodes(data);
      var edges = self.getEdges(nodes);
      var splines = self.options.bundle(edges);

      var paths = self.svg.selectAll(self.options.selector)
        .data(edges).enter().append("svg:path")
        .attr("class", "edge")
        .style("stroke", function(d) { 
//          console.log(d); 
          return self.utils.getColour(d.source.hue, 50, 80); })
        .attr("data-source", function(d) { return d.source.name; })
        .attr("data-target", function(d) { return d.target.name; })
        .attr("d", function(d, i) { return self.options.line(splines[i]); });

      self.svg.selectAll("g")
        .data(nodes.filter(function(n) { return !n.children; }))
        .enter().append("circle")
        .attr("r", 10)
        .attr("class", "node")
        .style("fill", function(d) { return self.utils.getColour(d.hue, 50, 60); })
        .attr("data-name", function(d) { return d.name; })
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")"; })
        .append("svg:title")
        .text(function(d) { return d.name; });        
  },
    
  draw: function() {
    var that = this;
      
    d3.json(this.options.json, function(resp) {
        // don't forget that!
        that.actualDraw.call(that, resp);
    });
  },

  /** 
   * @param json data
   * @return returns an array of d3.cluster nodes
   **/
  getNodes: function(data) {
    var self = this;
    var nodes = self.cluster.nodes(data);
    // pick hues for each node that are equidistant apart from one another
    var colours = []
    var increment = Math.ceil(360 / nodes.length);
    for(var i = 0; i < 360 && colours.length <= nodes.length; i += increment) {
      colours.push(i);
    }
    nodes.forEach(function(n){
      n.hue = colours.pop();
    });
    return nodes;
  },

  /** 
   * @param nodes The nodes created from getNodes()
   * @return an array of edges
   **/
  getEdges: function(nodes) {
    var map = {};
    var edges = [];
    nodes.forEach(function(d) {
      map[d.name]  = d;
    });
    nodes.forEach(function(d) {
      if(d.imports) {
        d.imports.forEach(function(a) {
          edges.push({source: map[d.name], target: map[a]});
        });
      }
    });
    return edges;
  }
});