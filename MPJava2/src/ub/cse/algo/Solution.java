package ub.cse.algo;

import java.util.*;

public class Solution{
    private Info info;
    private Graph graph;
    private ArrayList<Client> clients;
    private ArrayList<Integer> bandwidths;
    private int maxPathLength;
    /// Necessary Information
    public HashMap<Integer, int[]> bandwidthLists;
    public HashMap<Integer, ArrayList<Integer>> bfsPaths;
    public HashSet<Integer> clientList;
    public HashMap<Integer, Integer> priorityList;
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
        this.bandwidthLists = new HashMap<>();
        for (int i = 0; i < bandwidths.size(); i++) {
            bandwidthLists.put(i, new int[150]);
            Arrays.fill(bandwidthLists.get(i), bandwidths.get(i));
        }
        System.out.println(bandwidthLists.size());
        this.bfsPaths = Traversals.bfsPaths(info.graph, info.clients);
        clientList = new HashSet<>();
        priorityList = new HashMap<>();
        for (Client client : this.clients) {
            if(client!=null) {
                clientList.add(client.id);
                priorityList.put(client.id, (int) (client.alpha * bfsPaths.get(client.id).size()));
            }
        }
    }

    /**
     * This method performs the algorithm as
     * designed by Christian Fox, Harper Scott, and Sam Carrillo.
     * It returns an ArrayList of the highest paying clients out
     * of the list of reachable clients, and should yield an optimal result
     */

    private QueueItem getPathToClient(Integer contentProvider, Integer target) {
        //Djikstra's based on the bandwidth of a node at a given tick of time
        //Goal is to find the path with the least delay

        PriorityQueue<QueueItem> pq = new PriorityQueue<>();
        HashSet<Integer> visited = new HashSet<>();

        ArrayList<Integer> startPath = new ArrayList<>();
        QueueItem item = new QueueItem(1, contentProvider, startPath);
        pq.add(item);
        while (!pq.isEmpty()) {
            item = pq.poll();
            Integer node = item.node;
            visited.add(node);
            for (Integer n : graph.get(node)) { //10 ms
                QueueItem newItem = new QueueItem(item.currDelay, n, item.path);
                if(n.equals(target)){
                    return newItem;
                }
                else if (!visited.contains(n) && item.pathIsPossible(newItem.currDelay)) {
                    pq.add(newItem);
                }
            }
        }

        return null;
    }

    private ArrayList<Integer> getPath(QueueItem item) {
        ArrayList<Integer> path = new ArrayList<>();

        int tick = 0;
        for (int node : item.path) {
            decrementBandwidth(tick, node);
            path.add(node);
            tick+= getDelay(tick, node);
            if (tick > maxPathLength) {
                break;
            }
        }
        return path;
    }


    private PriorityQueue<Integer> getClientRanking(){
        PriorityQueue<Integer> pq = new PriorityQueue<>((Integer a,Integer b) -> priorityList.get(a) - priorityList.get(b) );

        for(Integer node : graph.keySet()) {
            if(clientList.contains(node)) {
                pq.add(node);
            }
        }

        return pq;
    }

    public int getDelay(int tick, int node)
    {
        int delay = 0;
        while(bandwidthLists.get(node)[tick] <= 0) { ////////// 5,739 ms
            delay++;
            tick++;
        }
        return delay;
    }

    public void decrementBandwidth(int tick, int node){
        bandwidthLists.get(node)[tick]--;
    }

    class QueueItem implements Comparable<QueueItem> {
        int currDelay;
        ArrayList<Integer> path;
        Integer node;
        QueueItem(int prevDelay, int node, ArrayList<Integer> path) {
            this.currDelay = prevDelay + getDelay(prevDelay, node);
            this.node = node;
            this.path = new ArrayList<>(path); // 390ms
            this.path.add(node);
        }

        public Boolean pathIsPossible(int currPathLength) {
            if (this.path.size() - this.currDelay >= this.path.size() - currPathLength) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int compareTo(QueueItem o) {
            return o.currDelay - this.currDelay;
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

        PriorityQueue<Integer> ranking = getClientRanking();

        while(!ranking.isEmpty()){
            Integer client = ranking.poll();
            QueueItem pathItem = getPathToClient(graph.contentProvider, client); //492 ms

            ArrayList<Integer> path;
            if(pathItem != null) {
                path = getPath(pathItem);
                sol.paths.put(client, path);
                sol.priorities.put(client, priorityList.get(client));
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
    private int[] bandwidthTicks;
    private int shortestDelay;
    private int tolerance;

    public NetworkNode(Client client, int bandwidth, Boolean isClient, Boolean isProvider, int maxPathLength, int shortestDelay) {
        this.client = client;
        this.isClient = isClient;
        this.isProvider = isProvider;
        this.bandwidth = bandwidth;
        bandwidthTicks = new int[maxPathLength];
        Arrays.fill(bandwidthTicks, bandwidth);
        this.shortestDelay = shortestDelay;
        if (isClient) {
            this.tolerance = (int) this.getClient().alpha * this.shortestDelay;
        }
        else {
            this.tolerance = Integer.MAX_VALUE;
        }
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

    public int getTolerance() {
        return this.tolerance;
    }

    public int getShortestDelay() {
        return this.shortestDelay;
    }

    public int getDelay(int tick)
    {
        int delay = 0;
        while(this.bandwidthTicks[tick] <= 0){ ////////// 5,739 ms
            delay++;
            tick++;
        }
        return delay;
    }

    @Override
    public int compareTo(NetworkNode b) {
        return (int) (this.getClient().alpha * this.shortestDelay - b.getClient().alpha * b.getShortestDelay());
    }
}

