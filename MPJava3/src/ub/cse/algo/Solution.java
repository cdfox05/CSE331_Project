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

        HashMap<Integer, ArrayList<Integer>> paths = Traversals.bfsPaths(this.graph, this.clients);


        for (int n : this.graph.keySet()) {

            if (hasNode.containsKey(n)) {

                graph.put(hasNode.get(n), new ArrayList<>());

            } else {

                int maxPathLength = 0;

                int clientsOnPath = 0;

                if (paths.containsKey(n)) {

                    maxPathLength = paths.get(n).size();


                    for (int a : paths.get(n)) {

                        if (paths.containsKey(a)) {

                            clientsOnPath++;

                        }

                    }

                }


                NetworkNode node = createNode(n, maxPathLength, clientsOnPath);

                hasNode.put(n, node);

                graph.put(node, new ArrayList<>());

            }


            for (int a : this.graph.get(n)) {

                if (hasNode.containsKey(a)) {

                    graph.get(hasNode.get(n)).add(hasNode.get(a));

                } else {

                    int maxPathLength = 0;

                    int clientsOnPath = 0;

                    if (paths.containsKey(a)) {

                        maxPathLength = paths.get(a).size();


                        for (int ap : paths.get(a)) {

                            if (paths.containsKey(ap)) {

                                clientsOnPath++;

                            }

                        }

                    }


                    NetworkNode node = createNode(a, maxPathLength, clientsOnPath);

                    hasNode.put(a, node);

                    graph.get(hasNode.get(n)).add(hasNode.get(a));

                }

            }

        }


        return graph;

    }


    private NetworkNode createNode(int id, int maxPathLength, int clientsOnPath) {

        Client client = null;

        for (Client c : this.clients) {

            if (c.id == id) {

                client = c;

                break;

            }

        }


        int bandwidth = this.bandwidths.get(id);

        NetworkNode node = null;

        if (client == null) {

            client = new Client(id, 0, bandwidth, 0, false, false);


            if (id == this.graph.contentProvider) {

                node = new NetworkNode(client, bandwidth, false, true, maxPathLength, clientsOnPath);

            } else {

                node = new NetworkNode(client, bandwidth, false, false, maxPathLength, clientsOnPath);

            }

        } else {

            node = new NetworkNode(client, bandwidth, true, false, maxPathLength, clientsOnPath);

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


    private PriorityQueue<NetworkNode> getClientRanking(HashMap<NetworkNode, ArrayList<NetworkNode>> graph) {

        PriorityQueue<NetworkNode> pq = new PriorityQueue<>();


        for (NetworkNode node : graph.keySet()) {

            if (node.isClient()) {

                pq.add(node);

            }

        }


        return pq;

    }


    private ArrayList<Integer> getTargetPath(HashMap<NetworkNode, ArrayList<NetworkNode>> graph, NetworkNode provider, NetworkNode target, HashMap<Integer, HashMap<NetworkNode, Integer>> consumedBandwidth) {

        HashMap<NetworkNode, NetworkNode> edgeTo = new HashMap<>();

        Deque<NetworkNode> queue = new LinkedList<>();

        queue.add(provider);


        int depth = 0;

        NetworkNode last = provider;

        ArrayList<NetworkNode> incremented = new ArrayList<>();

        while (!edgeTo.containsKey(target) && !queue.isEmpty()) {

            NetworkNode curr = queue.poll();


            for (NetworkNode adj : graph.get(curr)) {

                if (!edgeTo.containsKey(adj)) {

//                    if (adj.getConsumedBandwidth(depth) >= adj.getBandwidth() && adj == target) {

//                        int len = 0;

//                        int throttled = 0;

//                        NetworkNode prelim = curr;

//                        while (!prelim.isProvider()) {

//                            if (prelim.getConsumedBandwidth(depth) >= prelim.getBandwidth()) {

//                                throttled += prelim.getConsumedBandwidth(depth) - prelim.getBandwidth();

//                            }

//

//                            len++;

//                            prelim = edgeTo.get(prelim);

//                        }

//

//                        if (Double.compare((len + throttled) * adj.getClient().alpha, adj.getTolerance()) <= 0) {

//                            edgeTo.put(adj, curr);

//                            queue.add(adj);

//                            adj.incConsumedBandwidth(depth);

//                            incremented.add(adj);

//                        }

//                    } else {

                    edgeTo.put(adj, curr);

                    queue.add(adj);

                    adj.incConsumedBandwidth(depth);

                    incremented.add(adj);

//                    }

                }

            }


            if (curr == last && !queue.isEmpty()) {

                depth++;

                last = queue.peekLast();

            }

        }


        NetworkNode node = target;

        ArrayList<NetworkNode> nodePath = new ArrayList<>();

        nodePath.add(node);

        ArrayList<Integer> idPath = new ArrayList<>();

        idPath.add(0, node.getClient().id);


        if (!edgeTo.containsKey(target)) {

            return new ArrayList<>();

        } else {

            while (!node.isProvider()) {

                NetworkNode from = edgeTo.get(node);

                nodePath.add(0, from);

                idPath.add(0, from.getClient().id);

                node = from;

            }


            depth = 0;

            for (NetworkNode v : incremented) {

                if (depth == 0) {

                    depth++;

                } else {

                    if (!nodePath.contains(v)) {

                        v.decConsumedBandwidth(depth);

                    }

                }

            }

        }


        return idPath;

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


        HashMap<NetworkNode, ArrayList<NetworkNode>> graph = convertGraph();

        PriorityQueue<NetworkNode> ranking = getClientRanking(graph);

        HashMap<Integer, HashMap<NetworkNode, Integer>> consumedBandwidth = new HashMap<>();


        while (!ranking.isEmpty()) {

            NetworkNode client = ranking.poll();

            ArrayList<Integer> path = getTargetPath(graph, getProvider(graph), client, consumedBandwidth);

//            sol.paths.put(client.getClient().id, path);

            sol.priorities.put(client.getClient().id, client.getClient().payment / (int)client.getTolerance() - client.getClientsOnPath());

        }


        HashMap<Integer, ArrayList<Integer>> paths = Traversals.bfsPaths(this.graph, this.clients);

        for (Integer id : paths.keySet()) {

            sol.paths.put(id, paths.get(id));

        }


        return sol;

    }

}


class NetworkNode implements Comparable<NetworkNode> {

    private final Client client;

    private final Boolean isClient;

    private final Boolean isProvider;

    private final int bandwidth;

    private final int maxLength;

    private final int clientsOnPath;

    private final double tolerance;

    private HashMap<Integer, Integer> consumedBandwidth;


    public NetworkNode(Client client, int bandwidth, Boolean isClient, Boolean isProvider, int maxLength, int clientsOnPath) {

        this.client = client;

        this.isClient = isClient;

        this.isProvider = isProvider;

        this.bandwidth = bandwidth;

        this.maxLength = maxLength;

        this.clientsOnPath = clientsOnPath;

        this.tolerance = client.alpha * maxLength;

        this.consumedBandwidth = new HashMap<>();

    }


    public Boolean isProvider() {

        return this.isProvider;

    }


    public Boolean isClient() {

        return this.isClient;

    }


    public Client getClient() {

        return this.client;

    }


    public int getBandwidth() {

        return this.bandwidth;

    }


    public int getMaxLength() {

        return this.maxLength;

    }


    public int getClientsOnPath() {

        return this.clientsOnPath;

    }


    public double getTolerance() {

        return this.tolerance;

    }


    public void incConsumedBandwidth(int depth) {

        this.consumedBandwidth.put(depth, this.consumedBandwidth.getOrDefault(depth, 0) + 1);

    }


    public void decConsumedBandwidth(int depth) {

        this.consumedBandwidth.put(depth, this.consumedBandwidth.getOrDefault(depth, 0) - 1);

    }


    public int getConsumedBandwidth(int depth) {

        return this.consumedBandwidth.getOrDefault(depth, 0);

    }


    @Override

    public int compareTo(NetworkNode b) {

        if (Double.compare(this.tolerance, b.getTolerance()) == 0) {

            return Integer.compare(b.getClient().payment, this.getClient().payment);

        } else {

            return Double.compare(this.getTolerance(), b.getTolerance());

        }

    }

}