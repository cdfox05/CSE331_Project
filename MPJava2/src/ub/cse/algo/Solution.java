package ub.cse.algo;

import java.util.*;

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

    private HashMap<NetworkNode, ArrayList<NetworkNode>> convertGraph() {
        HashMap<NetworkNode, ArrayList<NetworkNode>> graph = new HashMap<>();
        HashMap<Integer, NetworkNode> hasNode = new HashMap<>();

        for (int n : this.graph.keySet()) {
            if (hasNode.containsKey(n)) {
                graph.put(hasNode.get(n), new ArrayList<>());
            } else {
                NetworkNode node = createNode(n);
                hasNode.put(n, node);
                graph.put(node, new ArrayList<>());
            }

            for (int a : this.graph.get(n)) {
                if (hasNode.containsKey(a)) {
                    graph.get(hasNode.get(n)).add(hasNode.get(a));
                } else {
                    NetworkNode node = createNode(a);
                    hasNode.put(a, node);
                    graph.get(hasNode.get(n)).add(hasNode.get(a));
                }
            }
        }

        return graph;
    }

    private NetworkNode createNode(int id) {
        int bandwidth = this.bandwidths.get(id);

        Client client = null;
        for (Client c : this.clients) {
            if (c.id == id) {
                client = c;
                break;
            }
        }

        NetworkNode node = null;
        if (client == null) {
            client = new Client(id, 0, bandwidth, 0, false, false);

            if (id == this.graph.contentProvider) {
                node = new NetworkNode(client, bandwidth, false, true);
            } else {
                node = new NetworkNode(client, bandwidth, false, false);
            }
        } else {
            node = new NetworkNode(client, bandwidth, true, false);
        }

        return node;
    }

    private NetworkNode getProvider(HashMap<NetworkNode, ArrayList<NetworkNode>> graph) {
        for (NetworkNode node : graph.keySet()) {
            if (node.isProvider()) {
                return node;
            }
        }

        return null;
    }

    /**
     * This method performs the algorithm as
     * designed by Christian Fox, Harper Scott, and Sam Carrillo.
     * It returns an ArrayList of the highest paying clients out
     * of the list of reachable clients, and should yield an optimal result
     */

    private ArrayList<Integer> getPathToClient(HashMap<NetworkNode, ArrayList<NetworkNode>> graph, NetworkNode contentProvider, NetworkNode target) {
        HashMap<NetworkNode, NetworkNode> edgeTo = new HashMap<>();
        Queue<NetworkNode> queue = new LinkedList<>();

        queue.add(contentProvider);
        while (!edgeTo.containsKey(target) && !queue.isEmpty()) {
            NetworkNode curr = queue.poll();
            for (NetworkNode adj : graph.get(curr)) {
                if (!edgeTo.containsKey(adj) && adj.getBandwidth() > 0) {
                    edgeTo.put(adj, curr);
                    queue.add(adj);
                }
            }
        }

        /// path construction
        ArrayList<NetworkNode> path = new ArrayList<>();
        NetworkNode node = target;

        ArrayList<Integer> pathAsInteger = new ArrayList<>();

        pathAsInteger.add(0, node.getClient().id);
        path.add(0, node);
        while(!node.equals(contentProvider)) {
            if (!edgeTo.containsKey(node)) {
                return new ArrayList<>();
            }
            NetworkNode from = edgeTo.get(node);
            path.add(0, from);
            pathAsInteger.add(0, from.getClient().id);
            node = from;
        }

        // pathAsInteger.add(0, contentProvider.getClient().id);
        // path.add(0,contentProvider);

        /// decrement bandwidths

        for (NetworkNode n : path) {
            n.decrementBandwidth();
            // pathAsInteger.add(n.getClient().id);
        }

        // turn path into a list of integers and return
        return pathAsInteger;
    }


    private PriorityQueue<NetworkNode> getClientRanking(HashMap<NetworkNode, ArrayList<NetworkNode>> edgeList){
        PriorityQueue<NetworkNode> pq = new PriorityQueue<>();

        for(Map.Entry<NetworkNode, ArrayList<NetworkNode>> entry : edgeList.entrySet()){
            NetworkNode node = entry.getKey();
            if(node.isClient()){
                pq.add(node);
            }
        }

        return pq;
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

        HashMap<NetworkNode, ArrayList<NetworkNode>> edgeList = convertGraph();
        PriorityQueue<NetworkNode> ranking = getClientRanking(edgeList);

        while(!ranking.isEmpty()){
            NetworkNode client = ranking.poll();
            ArrayList<Integer> path = getPathToClient(edgeList, getProvider(edgeList), client);
            sol.paths.put(client.getClient().id, path);
            sol.priorities.put(client.getClient().id, client.getClient().payment);
        }

        return sol;
    }
}

class NetworkNode implements Comparable<NetworkNode> {
    private Client client;
    private Boolean isClient;
    private Boolean isProvider;
    private int bandwidth;


    public NetworkNode(Client client, int bandwidth, Boolean isClient, Boolean isProvider) {
        this.client = client;
        this.isClient = isClient;
        this.isProvider = isProvider;
        this.bandwidth = bandwidth;
    }

    public Client getClient() {
        return this.client;
    }

    public Boolean isClient() {
        return this.isClient;
    }

    public Boolean isProvider() {
        return this.isProvider;
    }

    public Integer getBandwidth() {
        return this.bandwidth;
    }

    public void decrementBandwidth(){
        this.bandwidth--;
    }

    @Override
    public int compareTo(NetworkNode b) {
        return Integer.compare(this.getClient().payment, b.getClient().payment);
    }
}

///Class to test our functions to make sure things are processing correctly
class TestingFunctions{}
