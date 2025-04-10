package ub.cse.algo;

import java.util.*;

public class Solution {
    private Info info;
    private Graph graph;
    private ArrayList<Client> clients;
    private ArrayList<Integer> bandwidths;

    /**
     * Basic Constructor
     *
     * @param info: data parsed from input file
     */
    public Solution(Info info) {
        this.info = info;
        this.graph = info.graph;
        this.clients = info.clients;
        this.bandwidths = info.bandwidths;
    }

    /**
     * Method that returns the calculated
     * SolutionObject as found by your algorithm
     *
     * @return SolutionObject containing the paths, priorities and bandwidths
     */
    public SolutionObject outputPaths() {
        SolutionObject sol = new SolutionObject();

        HashMap<Integer, ArrayList<Integer>> paths = Traversals.bfsPaths(this.graph, this.clients); // get all shortest paths
        HashMap<Integer, HashMap<Integer, Integer>> bandUsage = new HashMap<>();                    // hashmap to track the used bandwidth of each node

        for (Client client : this.clients) {                                                        // iterate over every client
            incBandwidth(paths.get(client.id), bandUsage);                                          // call helper function to track used node bandwidth
            sol.paths.put(client.id, paths.get(client.id));                                         // add the shortest path to the solution
        }

        sol.bandwidths = this.bandwidths;

        return sol;
    }

    private void incBandwidth(ArrayList<Integer> path, HashMap<Integer, HashMap<Integer, Integer>> bandUsage) {
        for (int i=1; i<path.size(); i++) {                                                         // iterate over every node in the path besides the provider
            if (!bandUsage.containsKey(i)) {                                                        // add depth and node if the depth has not been tracked yet
                bandUsage.put(i, new HashMap<>());
                bandUsage.get(i).put(path.get(i), 1);
            } else if (!bandUsage.get(i).containsKey(path.get(i))) {                                // add node to a depth if not already present
                bandUsage.get(i).put(path.get(i), 1);
            } else if (bandUsage.get(i).get(path.get(i)) < this.bandwidths.get(path.get(i))) {      // increment used bandwidth for nodes below their capacity
                bandUsage.get(i).put(path.get(i), bandUsage.get(i).get(path.get(i)) + 1);
            } else if (bandUsage.get(i).get(path.get(i)) >= this.bandwidths.get(path.get(i))) {     // increase the bandwidth for nodes at or above their capacity
                bandUsage.get(i).put(path.get(i), bandUsage.get(i).get(path.get(i)) + 1);
                this.bandwidths.set(path.get(i), bandUsage.get(i).get(path.get(i)));
            }
        }
    }
}
