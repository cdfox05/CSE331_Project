package ub.cse.algo;

import sun.nio.ch.Net;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Comparator;

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
     * Method that generates the BFS tree for the input graph
     *
     * @return NetworkTree containing the root node and count
     */
    private NetworkTree buildTree() {
        NetworkNode provider = new NetworkNode(new Client(this.graph.contentProvider, 0, 0, 0, false, false), null, true);
        NetworkTree nTree = new NetworkTree(provider);

        Queue<NetworkNode> queue = new LinkedList<>();
        queue.add(provider);
        HashMap<Integer, NetworkNode> visited = new HashMap<>();
        visited.put(this.graph.contentProvider, provider);
        NetworkNode curr = provider;
        while (curr != null) {
            for (int adj : this.graph.get(curr.getClient().id)) {
                if (!visited.containsKey(adj)) {
                    int bandwidth = this.bandwidths.get(adj);
                    Client client = null;
                    for (Client c : this.clients) {
                        if (c.id == adj) {
                            client = c;
                            break;
                        }
                    }

                    NetworkNode node = null;
                    if (client == null) {
                        node = new NetworkNode(new Client(adj, 0, bandwidth, 0, false, false), curr, true);
                        nTree.incRounterCount();
                    } else {
                        node = new NetworkNode(client, curr, false);
                    }

                    curr.addChild(node);
                    queue.add(node);
                    visited.put(adj, node);
                    nTree.incCount();
                }
            }

            curr = queue.poll();
        }

        nTree.setNodes(visited);

        return nTree;
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

        NetworkTree nTree = this.buildTree();

        // these print the entire tree with its node count followed
        // by the size of the bandwidth array for debugging purposes
        // System.out.println(nTree.toString());
        // System.out.println(this.bandwidths.toArray().length);

        // add all leaf nodes in nTree to a queue
        Queue<NetworkNode> queue = new LinkedList<>();
        for (NetworkNode n : nTree.getNodes().values()) {
            if (n.isLeafNode() && !n.isRouter()) {
                queue.add(n);
            }
        }

        PriorityQueue<Client> pq = new PriorityQueue<>(new Comp());
        pq.addAll(this.clients);

        System.out.println(nTree.toString());

        int i = 0;
        ArrayList<Client> cList = new ArrayList<>();
        HashMap<Integer, Float> tolerances = new HashMap<>();
        while (!pq.isEmpty()) { // sets client priorities based only on tolerance level @alpha
            Client c = pq.poll();
            c.priority = i;
            sol.priorities.put(c.id,i); // populates the solution objects priorities map
            tolerances.put(c.id, c.alpha*info.shortestDelays.get(c.id));
            i++; // increments the priority by 1 (maybe not needed for problem 2?) ((Priorities are needed for the solution object))
        }

        return sol;
    }
}

class NetworkTree {
    private NetworkNode root;
    private HashMap<Integer, NetworkNode> nodes;
    private int count;
    private int routers;

    public NetworkTree(NetworkNode root) {
        this.root = root;
        this.nodes = null;
        this.count = 1;
        if (root.isRouter()) {
            routers = 1;
        } else {
            routers = 0;
        }
    }

    public NetworkNode getRoot() {
        return this.root;
    }

    public HashMap<Integer, NetworkNode> getNodes() {
        return this.nodes;
    }

    public void setNodes(HashMap<Integer, NetworkNode> nodes) {
        this.nodes = nodes;
    }

    public int getCount() {
        return this.count;
    }

    public void incCount() {
        this.count++;
    }

    public int getRouterCount() {
        return this.routers;
    }

    public void incRounterCount() {
        this.routers++;
    }

    public int getClientCount() {
        return this.count - this.routers;
    }

    @Override
    public String toString() {
        String out = new String();
        out += "Depth: 0\n";

        int depth = 0;
        int routers = 0;
        int last = this.root.getClient().id;
        Deque<NetworkNode> queue = new LinkedList<>();
        queue.add(this.root);
        NetworkNode curr = null;
        while (!queue.isEmpty()) {
            curr = queue.poll();
            out += "Node: " + curr.getClient().id;
            if (curr.getParent() != null) {
                out += " || Parent: " + curr.getParent().getClient().id + "\n";
            } else {
                out += " (Root)\n";
            }

            if (curr.isRouter())
                routers++;

            queue.addAll(curr.getPQueue());

            if (curr.getClient().id == last) {
                if (!queue.isEmpty()) {
                    depth++;
                    out += "Depth: " + depth + "\n";
                    last = queue.peekLast().getClient().id;
                }
            }
        }


        out += "# Nodes: " + this.getCount() + ", # Routers: " + this.getRouterCount() + ", # Clients: " + this.getClientCount();

        return out;
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
        this.children = new PriorityQueue<>(NetworkNode.this);
    }

    public Client getClient() {
        return this.client;
    }

    public Boolean isRouter() {
        return this.isRouter;
    }

    public PriorityQueue<NetworkNode> getPQueue() {
        return this.children;
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
