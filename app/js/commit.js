/**
 * This is a commit object. It stores the edges and nodes to light up
 * and also the "current state" of the wheel
 **/
Commit = function(options) {
    this.options = $.extend({}, this.options, options);
    var self = this;
    this.init();
}

Commit.prototype = {
    added: [], /** Array of strings that are the class names of the added files **/
    removed: [], /** Array of strings that are the class names of the removed files **/
    renamed: [], /** Array of objects that are the class names of the renamed files e.g. {oldname: "a", newname: "b"} **/
    edges: [], /** Array of edges that light up in this commit. also stores # of times accessed e.g. 
                    { source: "a", target: "b", type: "coupling1", accessed: 2 } **/

    currentState: {
        nodes: [],
        edges: []
    },
    
    init: function() {
        // initialize this commit
    },
}