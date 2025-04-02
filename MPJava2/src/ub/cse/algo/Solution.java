package ub.cse.algo;

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

public class Comparor implements Comparator<Client> { //Comparator for the tolerance levels for each client for the PQ
    @Override
    public int compare(Client a, Client b) {
        if (((a.alpha)*info.shortestDelays.get(a.id)) > ((b.alpha) * info.shortestDelays.get(b.id))) {
            if (a.payment > b.payment)
                return 1;
        }
        else if (((a.alpha)*info.shortestDelays.get(a.id) == (b.alpha) * info.shortestDelays.get(b.id))) {
            if (a.payment > b.payment)
                return 1;
            else
                return 0;
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
        /* TODO: Your solution goes here */

        sol.bandwidths = this.bandwidths;

        PriorityQueue<Client> pq = new PriorityQueue<>(new Comparor());
        for (Client c: this.clients)
        {
            pq.add(c);
        }
        int i = 0;
        ArrayList<Client> cList = new ArrayList<>();
        HashMap<Integer, Float> tolerances = new HashMap<>();
        while (!pq.isEmpty()) //sets client priorities based only on tolerance level @alpha
        {
            Client c = pq.poll();
            c.priority = i;
            sol.priorities.put(c.id,i); //populates the solution objects priorities map
            tolerances.put(c.id, c.alpha*info.shortestDelays.get(c.id));
            i++; //increments the priority by 1
            //cList.add(c); //Debugging
        }

        //System.out.println(cList); //Debugging
        //System.out.println(clients); //Debugging

        sol.paths = Traversals.bfsPaths(graph,clients); //instead of bfs maybe we should use dijkstras based off of bandwidths?

        //System.out.println("Bandwidths: " + info.bandwidths); //IMPORTANT
        //System.out.println("Shortest Delays: " + info.shortestDelays); //THE SHORTEST POSSIBLE DELAY IS MULTIPLIED BY THE CLIENTS TOLERANCE THRESHOLD
        //System.out.println("Clients: " + info.clients); //priority and alpha is important for this
        //System.out.println("Tolerances: " + tolerances);
        for (Client client : clients)
        {
            System.out.println("Client " + client.id + " Payment: " + client.payment);
        }
        //System.out.println(sol.paths);

        return sol;
    }
}
