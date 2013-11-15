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

            // returns parent if the parent is an affected or changed nodes
            // else returns false
            affectedParent: function(adjacency_list, child) {
                var id = false;
                adjacency_list.forEach(function(a) {
                    if(a.adjacent.indexOf(child.method_id) > -1 && a.status !== "unaffected" 
                        && child.status !== "unaffected") {
                        id = a.method_id;
                    }
                });
                return id;
            },

            createMatrix: function(length) {
                var matrix = [];
                for(var i = 0; i < length; i++) {
                    var row = [];
                    for(var j = 0; j < length; j++) {
                        row.push(0);
                    }
                    matrix.push(row);
                }
                return matrix;
            },

            /** 
            final_state d3.map() object
            {
                "a": { 
                    method_id: "a",
                    method_name: "a",
                    class_name: "A",
                    adjacent: ["b", "d", "e"]
                },
                "b": { 
                    method_id: "b",
                    method_name: "b",
                    class_name: "B",
                    adjacent: []
                }
            }

            commits array
            [
                {
                    commit_SHA: "123",
                    nodes: ["a", "b"],
                    edges: [
                        {
                            source: "a",
                            target: "b"
                        }
                    ]
                }
            ] 

            matrix_order: the order of the matrix labels ie
            [a,b,c,d,e]

            **/
            processCommitData: function(json, callback) {
                var commits = [];
                var final_state = d3.map({});
                var matrix_order = [];
                var self = this;
                $.getJSON(json, function(data){
                    data.forEach(function(d) {
                        d.dependency_graphs.forEach(function(g) {
                            // Iterate through every adjacency list to add all the nodes to the final state
                            g.adjacency_list.forEach(function(a) {
                                // check if this node is in the final state yet.
                                if(!(a.method_id in final_state)) {
                                    final_state.set(a.method_id, {
                                        method_id: a.method_id,
                                        method_name: a.method_name,
                                        class_name: a.class_name,
                                        adjacent: a.adjacent
                                    });
                                }
                            })
                        });
                    });

                    matrix_order = final_state.values().map(function(m){ return m.method_id });
                    data.forEach(function(d) {
                        var commit = {
                            commit_SHA: "",
                            nodes: [],
                            edges: []
                        };

                        var matrix = self.createMatrix(matrix_order.length);

                        commit.commit_SHA = d.commit_SHA;
                        d.dependency_graphs.forEach(function(g) {
                            commit.nodes.push(final_state.get(g.method_id));
                            // Iterate through every adjacency list to add edges
                            g.adjacency_list.forEach(function(a) {
                                var parent_id = self.affectedParent(g.adjacency_list, a);
                                if(parent_id !== false) {
                                    var row = matrix_order.indexOf(parent_id);
                                    var col = matrix_order.indexOf(a.method_id);
                                    if(row > -1 && col > -1) {
                                        matrix[row][col] += 1;
                                    }
                                    commit.edges.push({ source: parent_id, target: a.method_id });
                                }
                            })
                        });
                        commit.matrix = matrix;
                        commits.push(commit);
                    });
                    if(callback) callback(commits, final_state);
                });
            }

            // dependencyExists: function(dependencyList, dependency) {
            //     exists = false;
            //     dependencyList.forEach(function(d) {
            //         if(d.source === dependency.source && d.target === dependency.target)
            //             exists = true;
            //     });
            //     return exists;
            // },

            // // processes the commit data
            // processCommitData: function(callback) {
            //     // new fields for pushing current states for each commit
            //     var nodes = [];
            //     var edges = [];
            //     var commits = [];
            //     var self = this;
                
            //     $.getJSON("commitsTest2.json", function(resp) {
            //         commits = resp;
            //         commits.forEach(function(c){
            //             // loop through added files
            //             // add added files to nodes array
            //             c.addedJavaFiles.forEach(function(a) {
            //               nodes = nodes.concat({"name": a, "deleted": false});  
                            
            //             });
                        
            //               // loop through dependencies
            //             // add dependency if it doesnt exist already in edges array
            //             c.dependencies.forEach(function (d){
            //                 if(!self.dependencyExists(edges, d))
            //                     edges = edges.concat({"source": d.source, "target": d.target, "type": d.type });
            //             });
                        
                        
            //             // loop through renamed files, rename files in nodes array
            //             c.renamedJavaFiles.forEach(function(r){
            //                 // search the current nodes list and replace the old with new name
            //                 var i = 0;
            //                 for(i; i<nodes.length; i++) {
            //                  if(nodes[i].name === r.old) {
            //                      nodes[i].name = r.new;
            //                      nodes[i].oldName = r.old;
            //                      break;
            //                  }
            //                 }
                            
            //                 // rename the edge if it contains renamed node??
            //                 edges.forEach(function(e) {
            //                     // if the edge contains the node, rename it??
            //                     if(e.target === r.old)
            //                         e.target = r.new;
            //                     else if(e.source === r.old)
            //                         e.source = r.new;
            //                 });
            //             });
                        
            //             // loop through removed files, set deleted flag in nodes array to true
            //             c.removedJavaFiles.forEach(function(d) {
            //                 var j = 0;
            //                 for(j; j<nodes.length; j++) {
            //                  if(nodes[j].name === d) {
            //                      nodes[j].deleted = true;
            //                      continue;
            //                  }
            //                 }
            //             // set deleted flag to true for all associated edges
            //                 edges.forEach(function(e) {
            //                     // if the edge contains the node, set deleted flag = true
            //                     if(e.target === d || e.source === d)
            //                         e.deleted = true;
            //                 });

            //             });
                        
            //             // set commit object's currentNode and currentEdges fields
            //             c.currentNodes = nodes;
            //             c.currentEdges = edges;
            //         });
            //         if(callback) callback(commits);
            //     });
            // },
        };
    }

    return {
        getInstance: function() {
            if(!instance) instance = init();
            return instance;
        }
    }
})();