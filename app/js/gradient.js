function gradient(url, rotate, start, stop) {
 
  if (!arguments.length) {
    url = "gradient";
  }
 
  function my() {
 
   var defs = this.append("defs");
      
   var gradient = defs.append("linearGradient")
    .attr("id", url)
    .attr("gradientTransform", "rotate(" + rotate + ")")
    .attr("spreadMethod", "pad");


    gradient.append("stop")
        .attr("offset", "0%")
        .attr("stop-color", start)
        .attr("stop-opacity", 1);
         
    gradient.append("stop")
    .attr("offset", "100%")
    .attr("stop-color", stop)
    .attr("stop-opacity", 1);
      
  }
 
 
  return my;
}