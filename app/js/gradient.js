function gradient(url) {
 
  if (!arguments.length) {
    url = "gradient";
  }
 
  function my() {
 
   var defs = this.append("defs");
      
   var gradient = defs.append("linearGradient")
    .attr("id", url)
    .attr("x1", "50%")
    .attr("y1", "50%")
    .attr("x2", "100%")
    .attr("y2", "50%")
    .attr("spreadMethod", "pad");


    gradient.append("stop")
        .attr("offset", "0%")
        .attr("stop-color", "white")
        .attr("stop-opacity", 1);
         
    gradient.append("stop")
    .attr("offset", "100%")
    .attr("stop-color", "yellow")
    .attr("stop-opacity", 1);
  }
 
 
  return my;
}