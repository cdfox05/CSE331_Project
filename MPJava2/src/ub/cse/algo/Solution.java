package ub.cse.algo;

import com.sun.xml.internal.bind.v2.TODO;
import sun.nio.ch.Net;

import java.lang.reflect.Array;
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


    /**
     * Method that generates the BFS tree for the input graph
     *
     * @return NetworkTree containing the root node and count
     */
    private NetworkTree buildTree() {
        NetworkNode provider = new NetworkNode(new Client(this.graph.contentProvider, 0, 0, 0, false, false), null, true, bandwidths);
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
                        node = new NetworkNode(new Client(adj, 0, bandwidth, 0, false, false), curr, true, bandwidths);
                        nTree.incRounterCount();
                    } else {
                        node = new NetworkNode(client, curr, false, bandwidths);
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
     * This method performs the algorithm as
     * designed by Christian Fox, Harper Scott, and Sam Carrillo.
     * It returns an ArrayList of the highest paying clients out
     * of the list of reachable clients, and should yield an optimal result
     */
    private ArrayList<NetworkNode> doAlgorithm(Queue<NetworkNode> queue, HashSet<NetworkNode> visited, NetworkNode provider) {
        NetworkNode node = queue.poll();
        //every node that is not the provider sends their top b children to their parent node
        while (!queue.isEmpty()) {
            node = queue.poll();
            if(!node.isProvider()) {
                node.sendTopBClients();
            }
            if (!visited.contains(node.getParent())) {
                queue.add(node.getParent());
                visited.add(node.getParent());
            }

        }
        if (!node.equals(provider))
        {
            System.out.println("!!!ERROR!!! final node is not the provider. \nCurrent Node: " + node.toString());
            return null;
        }
        PriorityQueue<NetworkNode> pq = node.getPQueue(); //the last queue item remaining should be the provider
        ArrayList<NetworkNode> clientList = new ArrayList<>();
        while(!pq.isEmpty()){
            clientList.add(pq.poll());
        }

        return clientList; //return an arraylist of the provider's clients
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
        //System.out.println(nTree.toString());

        // these print the entire tree with its node count followed
        // by the size of the bandwidth array for debugging purposes
        // System.out.println(nTree.toString());
        // System.out.println(this.bandwidths.toArray().length);

        // add all leaf nodes in nTree to a queue
        Queue<NetworkNode> queue = new LinkedList<>();
        HashSet<NetworkNode> visited = new HashSet<>();
        for (NetworkNode n : nTree.getNodes().values()) {
            if (n.isLeafNode() && !n.isRouter()) {
                queue.add(n);
                visited.add(n);
            }
        }
        System.out.println("Leaf Nodes: " + queue.size());

        ArrayList<NetworkNode> bestNodes = doAlgorithm(queue, visited, nTree.getRoot());
        if (bestNodes == null)
        {
            return sol;
        }

        System.out.println(bestNodes.size());
        //System.out.println(nTree.toString());

        //turn the bestNodes list into a solution object and return it
        HashMap<Integer, ArrayList<Integer>> bfsPaths =  Traversals.bfsPaths(graph, clients);

        for (NetworkNode n : bestNodes) {
            Integer client = n.getClient().id;
            sol.paths.put(client, bfsPaths.remove(client));
            sol.priorities.put(client, n.getClient().payment);
        }

        for (Client c : clients)
        {
            if (bfsPaths.containsKey(c.id))
            {
                sol.paths.put(c.id, bfsPaths.remove(c.id));
            }
        }

        TestingFunctions test = new TestingFunctions(clients, sol.bandwidths, sol.priorities, nTree, info, sol.paths);

        test.testBandwidths();
        test.testCounts();
        test.testRoutersAndClients();


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
            out += "Node: " + curr.getClient().id + " Payment: " + curr.getClient().payment;
            if (curr.getParent() != null) {
                out += " || Parent: " + curr.getParent().getClient().id + " Bandwidth: " + curr.getParent().getBandwidth() + "\n";
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
    private int bandwidth;
    private ArrayList<NetworkNode> path;

    public NetworkNode(Client client, NetworkNode parent, Boolean isRouter, ArrayList<Integer> bandwidths) {
        this.client = client;
        this.parent = parent;
        this.isRouter = isRouter;
        this.children = new PriorityQueue<>(NetworkNode.this);
        this.bandwidth = bandwidths.get(client.id);
        this.path = new ArrayList<>();
        this.path.add(this);
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

    public Integer getBandwidth() {
        return this.bandwidth;
    }

    public boolean isProvider(){
        return this.parent == null;
    }

    public void pathAdd(NetworkNode node) {
        this.path.add(node);
    }

    public ArrayList<NetworkNode> getPath() {
        return this.path;
    }

    public void pathReversal() {
        Stack<NetworkNode> stack = new Stack<>();
        stack.addAll(this.path);

        this.path.clear();

        while (!stack.isEmpty())
        {
            this.path.add(stack.pop());
        }
    }

    public void sendTopBClients(){
        for (int i = bandwidth; i > 0 && !children.isEmpty(); i--) { //runs for i = bandwidth iterations
            NetworkNode n = getBestChild();
            parent.addChild(n);
            if( !(this.isRouter()) ){
                parent.addChild(this); //add clients own value if they aren't a router
            }
        }
    }


    public String toString()
    {
        return this.getClient().toString();
    }

    @Override
    public int compare(NetworkNode a, NetworkNode b) {
        return a.getClient().payment - b.getClient().payment;
    }
}

///Class to test our functions to make sure things are processing correctly
class TestingFunctions{
    private ArrayList<Client> clientList;
    private ArrayList<Integer> bandwidthList;
    private HashMap<Integer, Integer> priorityMap;
    private HashMap<Integer, ArrayList<Integer>> pathsMap;
    private NetworkTree tree;
    private Info info;



    public TestingFunctions(ArrayList<Client> clients, ArrayList<Integer> bandwidths,
                            HashMap<Integer,Integer> priorities, NetworkTree tree, Info info, HashMap<Integer, ArrayList<Integer>> path)
    {
        this.clientList = clients;
        this.bandwidthList = bandwidths;
        this.priorityMap = priorities;
        this.tree = tree;
        this.info = info;
        this.pathsMap = path;
    }

    public void testBandwidths()
    {
        int errorCount = 0;
        for (int i = 0; i < tree.getNodes().size(); i++)
        {
            int currBandwidth = tree.getNodes().get(i).getBandwidth();
            int actBandwidth = info.bandwidths.get(i);
            if (currBandwidth != actBandwidth){
                System.out.println("!!!ERROR!!! Client No. " + (i+1) + " has incorrect bandwidth.\nCurrent Bandwidth: "
                        + currBandwidth + " Actual Bandwidth: " + actBandwidth);
                errorCount++;
            }

        }

        if (errorCount != 0)
        {
            System.out.println("Number of errors with bandwidth: " + errorCount);
            return;
        }

        System.out.println("!!!All client bandwidths are correct!!!");
    }

    public void testCounts()
    {
        if (tree.getCount() != 10876) {
            System.out.println("!!!ERROR!!! Node Count is incorrect. \nCurrent Count: " + tree.getCount() + " Actual Count: " + 10876);
            return;
        }
        else if (tree.getRouterCount() != 8669) {
            System.out.println("!!!ERROR!!! Router Count is incorrect. \nCurrent Count: " + tree.getRouterCount() + " Actual Count: " + 8669);
            return;
        }
        else if (tree.getClientCount() != 2207)
        {
            System.out.println("!!!ERROR!!! Client Count is incorrect. \nCurrent Count: " + tree.getClientCount() + " Actual Count: " + 2207);
            return;
        }

        System.out.println("!!!Counts all returned successfully!!!");
    }

    public void testRoutersAndClients()
    {
        int errorCount = 0;
        for (NetworkNode node : tree.getNodes().values())
        {
            if (node.isRouter() && clientList.contains(node.getClient()))
            {
                System.out.println("!!!ERROR!!! Network Node No. " + node.getClient().id + " is incorrectly set as a Router");
                errorCount++;
            }
            else if (!node.isRouter() && !clientList.contains(node.getClient()))
            {
                System.out.println("!!!ERROR!!! Network Node No. " + node.getClient().id + " is incorrectly set as a Client");
                errorCount++;
            }
        }
        if (errorCount != 0)
        {
            System.out.println("Number of errors: " + errorCount);
            return;
        }

        System.out.println("!!!All routers and clients are set correctly!!!");
    }

    public void testDoAlgo()
    {

    }


    public void testRoutersInPaths() {
        int routerCount = 0;
        for (Integer key : pathsMap.keySet()) {
            if (tree.getNodes().get(key).isRouter())
            {
                routerCount++;
            }
        }

        if (routerCount != 0)
        {
            System.out.println("!!!ERROR!!! Routers within our solution objects path as a client");
            System.out.println("number of routers found incorrectly placed: " + routerCount);
            return;
        }

}
}
