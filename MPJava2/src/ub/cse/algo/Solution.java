package ub.cse.algo;

import java.util.*;

public class Solution{
    private Info info;
    private Graph graph;
    private ArrayList<Client> clients;
    private ArrayList<Integer> bandwidths;
    private int maxPathLength;

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
        int maxPathLength = getMaxPathLength();

        for (int n : this.graph.keySet()) {
            if (hasNode.containsKey(n)) {
                graph.put(hasNode.get(n), new ArrayList<>());
            } else {
                NetworkNode node = createNode(n, maxPathLength);
                hasNode.put(n, node);
                graph.put(node, new ArrayList<>());
            }

            for (int a : this.graph.get(n)) {
                if (hasNode.containsKey(a)) {
                    graph.get(hasNode.get(n)).add(hasNode.get(a));
                } else {
                    NetworkNode node = createNode(a, maxPathLength);
                    hasNode.put(a, node);
                    graph.get(hasNode.get(n)).add(hasNode.get(a));
                }
            }
        }

        int numClients = 0;
        for(NetworkNode n : graph.keySet()){
            if (n.isClient()){
                numClients++;
            }
        }

        return graph;
    }

    private NetworkNode createNode(int id, int maxPathLength) {
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
                node = new NetworkNode(client, bandwidth, false, true, maxPathLength, info);
            } else {
                node = new NetworkNode(client, bandwidth, false, false, maxPathLength, info);
            }
        } else {
            node = new NetworkNode(client, bandwidth, true, false, maxPathLength, info);
        }

        return node;
    }

    private int getMaxPathLength() {
        HashMap<Integer, ArrayList<Integer>> paths = Traversals.bfsPaths(this.graph, this.clients);

        double maxLen = 0;
        for (Client c : this.clients) {
            if(c.alpha * paths.get(c.id).size() > maxLen){
                maxLen = c.alpha * paths.get(c.id).size();
            }
        }

        return (int)maxLen;
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

    private ArrayList<Integer> getPathToClient(HashMap<NetworkNode, ArrayList<NetworkNode>> graph, NetworkNode contentProvider, NetworkNode target, HashMap<Integer, ArrayList<Integer>> bfsPaths) {
        HashMap<NetworkNode, PathItem> edgeTo = new HashMap<>();
        Queue<PathItem> queue = new LinkedList<>();

        queue.add(new PathItem(1, contentProvider)); //why the fuck does pathlength 1 work better
        while (!edgeTo.containsKey(target) && !queue.isEmpty()) {
            PathItem item = queue.poll();
            NetworkNode curr = item.node;
            int currPathLength = item.pathLength;
            for (NetworkNode adj : graph.get(curr)) {
                if (!edgeTo.containsKey(adj) && adj.getBandwidth(currPathLength) > 0) {
                    edgeTo.put(adj, new PathItem(currPathLength + 1, curr));
                    queue.add(new PathItem(currPathLength + 1, adj));
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
            NetworkNode from = edgeTo.get(node).node;
            path.add(0, from);
            pathAsInteger.add(0, from.getClient().id);
            node = from;
        }

        int pathSize = pathAsInteger.size();
        double maxSize = bfsPaths.get(target.getClient().id).size() * target.getClient().alpha;
        if(pathSize > maxSize ){
            return null;
        }

        /// decrement bandwidths
        for(int i = 0; i < path.size(); i++){
            NetworkNode n = path.get(i);
            n.decrementBandwidth(i);
        }

        // turn path into a list of integers and return
        return pathAsInteger;
    }


    private PriorityQueue<NetworkNode> getClientRanking(HashMap<NetworkNode, ArrayList<NetworkNode>> edgeList){
        PriorityQueue<NetworkNode> pq = new PriorityQueue<>();

        double revenue = 0.0;
        for(Map.Entry<NetworkNode, ArrayList<NetworkNode>> entry : edgeList.entrySet()){
            NetworkNode node = entry.getKey();
            if(node.isClient()){
                pq.add(node);
                revenue+=node.getClient().payment;
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
        HashMap<Integer, ArrayList<Integer>> bfsPaths = Traversals.bfsPaths(graph, clients);

        while(!ranking.isEmpty()){
            NetworkNode client = ranking.poll();
            ArrayList<Integer> path = getPathToClient(edgeList, getProvider(edgeList), client, bfsPaths);

            if(path != null) {
                sol.paths.put(client.getClient().id, path);
                sol.priorities.put(client.getClient().id, client.getClient().priority);
            }
        }

        int numNodes = 0;
        HashSet<NetworkNode> counted = new HashSet<>();
        for(NetworkNode n : edgeList.keySet()){
            if(!counted.contains(n)){
                counted.add(n);
                numNodes++;
            }
        }

        return sol;
    }
}

class NetworkNode implements Comparable<NetworkNode> {
    private Client client;
    private Boolean isClient;
    private Boolean isProvider;
    private int bandwidth;
    int[] bandwidthTicks;
    Info info;

    public NetworkNode(Client client, int bandwidth, Boolean isClient, Boolean isProvider, int maxPathLength, Info info) {
        this.client = client;
        this.isClient = isClient;
        this.isProvider = isProvider;
        this.bandwidth = bandwidth;
        bandwidthTicks = new int[maxPathLength];
        Arrays.fill(bandwidthTicks, bandwidth);
        this.info = info;
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

    public Integer getBandwidth(int tick) {
        return this.bandwidthTicks[tick];
    }

    public void decrementBandwidth(int tick){
        this.bandwidthTicks[tick]--;
    }

    @Override
    public int compareTo(NetworkNode b) {
        return (int) ((this.getClient().alpha*info.shortestDelays.get(this.getClient().id))-(b.getClient().alpha*info.shortestDelays.get(b.getClient().id)));
        //return this.getClient().payment-b.getClient().payment;
    }
}

class PathItem{
    int pathLength;
    NetworkNode node;
    public PathItem(int pathLength, NetworkNode node){
        this.pathLength = pathLength;
        this.node = node;
    }

}

///Class to test our functions to make sure things are processing correctly
class TestingFunctions{}
