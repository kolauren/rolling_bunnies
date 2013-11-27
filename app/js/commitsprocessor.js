CommitsProcessor = function() {
    this.init();
}

CommitsProcessor.prototype = {
    init: function() {
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

    // Recursive function omg. Returns true if the child is actually a child of the parent
    isImpactEdge: function(state, parent, child) {
        if(state.has(parent)) {
            if(state.get(parent).adjacent.indexOf(child) > -1)
                return true;
            else {
                var adjacent = state.get(parent).adjacent;
                for(var i = 0; i < adjacent.length; i++) {
                    return this.isImpactEdge(state, adjacent[i], child);
                }
            }
        }
        return false;
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
    **/
    processCommitData: function(json, callback) {
        var commits = [];
        var final_state = d3.map({});
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

            var impact_edges_map = d3.map({});
            data.forEach(function(d) {
                var commit = {
                    commit_SHA: "",
                    nodes: [],
                    edges: [],
                    impact_edges: []
                };

                commit.commit_SHA = d.commit_SHA;
                d.dependency_graphs.forEach(function(g) {
                    commit.nodes.push(final_state.get(g.method_id));
                    var source_id = g.method_id;
                    // Iterate through every adjacency list to add edges
                    g.adjacency_list.forEach(function(a) {
                        var parent_id = self.affectedParent(g.adjacency_list, a);
                        if(parent_id !== false) {
                            commit.edges.push({ source: parent_id, target: a.method_id });
                        }

                        // if this changed node is a child of the source_id, then add it to impacted edges
                        if(a.status === "changed" && self.isImpactEdge(final_state, source_id, a.method_id)) {
                            var key = source_id + "-" + a.method_id;
                            if(impact_edges_map.has(key)) {
                                impact_edges_map.get(key).count += 1;
                            } else {
                                impact_edges_map.set(key, {
                                    source: source_id,
                                    target: a.method_id,
                                    count: 1
                                });
                            }
                        }
                    });
                });
                impact_edges_map.forEach(function(k, v) {
                    commit.impact_edges.push($.extend(true, {}, v));
                });
                commits.push(commit);
            });
            if(callback) callback(commits, final_state, impact_edges_map.values());
        });
    }
}