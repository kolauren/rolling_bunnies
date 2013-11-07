/**
 * Singleton class for all utils
 *
 */
Utils = (function() {

    var instance;

    function init() {
        return {
            /**
             * @param hue, saturation, lightness
             * @return a stringified random hsl value
             **/
            getColour: function(h, s, l) {
                return "hsl(" + h + "," + s + "%," + l + "%)";
            },

            dependencyExists: function(dependencyList, dependency) {
                exists = false;
                dependencyList.forEach(function(d) {
                    if(d.source === dependency.source && d.target === dependency.target)
                        exists = true;
                });
                return exists;
            },

            // processes the commit data
            processCommitData: function(callback) {
                // new fields for pushing current states for each commit
                var nodes = [];
                var edges = [];
                var commits = [];
                var self = this;
                
                $.getJSON("commitsTest2.json", function(resp) {
                    commits = resp;
                    commits.forEach(function(c){
                        // loop through added files
                        // add added files to nodes array
                        c.addedJavaFiles.forEach(function(a) {
                          nodes = nodes.concat({"name": a, "deleted": false});  
                            
                        });
                        
                          // loop through dependencies
                        // add dependency if it doesnt exist already in edges array
                        c.dependencies.forEach(function (d){
                            if(!self.dependencyExists(edges, d))
                                edges = edges.concat({"source": d.source, "target": d.target, "type": d.type });
                        });
                        
                        
                        // loop through renamed files, rename files in nodes array
                        c.renamedJavaFiles.forEach(function(r){
                            // search the current nodes list and replace the old with new name
                            var i = 0;
                            for(i; i<nodes.length; i++) {
                             if(nodes[i].name === r.old) {
                                 nodes[i].name = r.new;
                                 nodes[i].oldName = r.old;
                                 break;
                             }
                            }
                            
                            // rename the edge if it contains renamed node??
                            edges.forEach(function(e) {
                                // if the edge contains the node, rename it??
                                if(e.target === r.old)
                                    e.target = r.new;
                                else if(e.source === r.old)
                                    e.source = r.new;
                            });
                        });
                        
                        // loop through removed files, set deleted flag in nodes array to true
                        c.removedJavaFiles.forEach(function(d) {
                            var j = 0;
                            for(j; j<nodes.length; j++) {
                             if(nodes[j].name === d) {
                                 nodes[j].deleted = true;
                                 continue;
                             }
                            }
                        // set deleted flag to true for all associated edges
                            edges.forEach(function(e) {
                                // if the edge contains the node, set deleted flag = true
                                if(e.target === d || e.source === d)
                                    e.deleted = true;
                            });

                        });
                        
                        // set commit object's currentNode and currentEdges fields
                        c.currentNodes = nodes;
                        c.currentEdges = edges;
                    });
                    if(callback) callback(commits);
                });
            },
        };
    }

    return {
        getInstance: function() {
            if(!instance) instance = init();
            return instance;
        }
    }
})();