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
        HashMap<Integer, Integer> dists = Traversals.bfs(this.graph, this.clients);

        for (Integer id : paths.keySet()) {
            sol.paths.put(id, paths.get(id));
            Client curr = null;
            for (Client client : clients) {
                if (client.id == id) {
                    curr = client;
                    break;
                }
            }

            sol.priorities.put(id, curr.payment / (int)(curr.alpha * dists.get(id)));
        }

        sol.bandwidths = this.bandwidths;

        return sol;
    }
}
