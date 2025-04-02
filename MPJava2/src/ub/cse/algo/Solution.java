package ub.cse.algo;

import sun.nio.ch.Net;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Solution{
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

    public class Comp implements Comparator<Client> { //Comparator for the tolerance levels for each client for the PQ
        @Override
        public int compare(Client a, Client b) {
            if (((a.alpha)*info.shortestDelays.get(a.id)) > ((b.alpha) * info.shortestDelays.get(b.id))) {
                if (a.payment > b.payment) {
                    return 1;
                }
            } else if (((a.alpha)*info.shortestDelays.get(a.id) == (b.alpha) * info.shortestDelays.get(b.id))) {
                if (a.payment > b.payment) {
                    return 1;
                } else {
                    return 0;
                }
            }

            return -1;
        }
    }

    /**
     * Method that returns the calculated 
     * SolutionObject as found by your algorithm
     *
     * @return SolutionObject containing the paths, priorities and bandwidths
     */
    public SolutionObject outputPaths() {
        SolutionObject sol = new SolutionObject();
        sol.bandwidths = this.bandwidths;

        PriorityQueue<Client> pq = new PriorityQueue<>(new Comp());
        for (Client c: this.clients) {
            pq.add(c);
        }

        int i = 0;
        ArrayList<Client> cList = new ArrayList<>();
        HashMap<Integer, Float> tolerances = new HashMap<>();
        while (!pq.isEmpty()) { // sets client priorities based only on tolerance level @alpha
            Client c = pq.poll();
            c.priority = i;
            sol.priorities.put(c.id,i); // populates the solution objects priorities map
            tolerances.put(c.id, c.alpha*info.shortestDelays.get(c.id));
            i++; // increments the priority by 1
        }

        return sol;
    }
}



class NetworkTree {
    private NetworkNode root;
    private int count;

    public NetworkTree(NetworkNode root) {
        this.root = root;
        this.count = 1;
    }

    public NetworkNode getRoot() {
        return this.root;
    }

    public int getCount() {
        return this.count;
    }
}

class NetworkNode implements Comparator<NetworkNode> {
    private Client client;
    private NetworkNode parent;
    private Boolean isRouter;
    private PriorityQueue<NetworkNode> children;

    public NetworkNode(Client client, NetworkNode parent, Boolean isRouter) {
        this.client = client;
        this.parent = parent;
        this.isRouter = isRouter;
        this.children = new PriorityQueue<>();
    }

    public Client getClient() {
        return this.client;
    }

    public Boolean getIsRouter() {
        return this.isRouter;
    }

    public void addChild(NetworkNode child) {
        this.children.add(child);
    }

    public NetworkNode getBestChild() {
        return this.children.poll();
    }

    public NetworkNode checkBestChild() {
        return this.children.peek();
    }

    public NetworkNode getParent() {
        return this.parent;
    }

    public Boolean isLeafNode() {
        return this.children.isEmpty();
    }

    @Override
    public int compare(NetworkNode a, NetworkNode b) {
        return a.getClient().payment - b.getClient().payment;
    }
}
