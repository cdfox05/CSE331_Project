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

        HashMap<Integer, ArrayList<Integer>> paths = Traversals.bfsPaths(this.graph, this.clients);
        HashMap<Integer, HashMap<Integer, Integer>> bandUsage = new HashMap<>();

        for (Client client : this.clients) {
            incBandwidth(paths.get(client.id), bandUsage);
            sol.paths.put(client.id, paths.get(client.id));
        }

        sol.bandwidths = this.bandwidths;

        return sol;
    }

    private void incBandwidth(ArrayList<Integer> path, HashMap<Integer, HashMap<Integer, Integer>> bandUsage) {
        for (int i=1; i<path.size(); i++) {
            if (!bandUsage.containsKey(i)) {
                bandUsage.put(i, new HashMap<>());
                bandUsage.get(i).put(path.get(i), 1);
            } else if (!bandUsage.get(i).containsKey(path.get(i))) {
                bandUsage.get(i).put(path.get(i), 1);
            } else if (bandUsage.get(i).get(path.get(i)) < this.bandwidths.get(path.get(i))) {
                bandUsage.get(i).put(path.get(i), bandUsage.get(i).get(path.get(i)) + 1);
            } else if (bandUsage.get(i).get(path.get(i)) >= this.bandwidths.get(path.get(i))) {
                bandUsage.get(i).put(path.get(i), bandUsage.get(i).get(path.get(i)) + 1);
                this.bandwidths.set(path.get(i), bandUsage.get(i).get(path.get(i)));
            }
        }
    }
}
