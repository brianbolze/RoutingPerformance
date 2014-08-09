// COMP3331 Computer Networks
// Programming Assignment
// "routing_performance"
//
// Author: Brian Bolze

import java.io.*;
import java.util.*;

public class RoutingPerformance {

	static int numRequests;
	static int numSuccessful;
	static int numBlocked;
	static float percSuccessful;
	static float percBlocked;
	static float avgHops;
	static float avgDelay;
	static String routingScheme;
	static String topologyFile;
	static String workloadFile;
	static NetworkGraph network;
	static PriorityQueue<Request> requestPQ;

	public static void main(String[] args) throws Exception {

		routingScheme = args[0];
		topologyFile = args[1];
		workloadFile = args[2];

		if (!routingScheme.equals("SHP") && !routingScheme.equals("SDP")
				&& !routingScheme.equals("LLP")) {
			System.out.println("Invalid Routing Scheme");
			return;
		}

		// Read in Topology_File
		// Build network data structure
		buildNetwork();

		// Read in Workload_File
		// Put into Request Priority Queue
		buildRequestPQ();

		// Test Priority Queue
//		Request rq;
//		while (requestPQ.peek() != null) {
//			rq = requestPQ.poll();
//			rq.print();
//		}

		// Initialize variables

		numSuccessful = 0;
		numBlocked = 0;

		int numHops = 0;
		int cumPropDelay = 0;
		int currDelay = 0;
		char source;
		char dest;
		float currentTime = 0;
		float pause = 0;

		Request currRequest;
		LinkedList<Character> path;
		DijkstraAlgorithm dijkstra;
		HashMap<Float, LinkedList<Character>> VCs = new HashMap<Float, LinkedList<Character>>();

		// Execute Requests (i.e. do the actual work!)

		while (requestPQ.peek() != null) {

			currRequest = requestPQ.poll();
			source = currRequest.A;
			dest = currRequest.B;
			currDelay = 0;

			pause = currRequest.exTime - currentTime;

			// Pause program until this request comes
			// try {
			// Thread.sleep((long) (pause));
			// } catch (InterruptedException e) {
			// }

			currentTime += pause;

			// Make VC connection (if possible)

			if (currRequest.connect) {

				// Build forwarding table
				dijkstra = new DijkstraAlgorithm(network);
				dijkstra.execute(source);
				path = dijkstra.getPath(dest);
				// System.out.println(path.toString());

				// Try to add VC

				// Keep track of connections in the case that it is blocked
				LinkedList<Character> popped = new LinkedList<Character>();

				// Store VC path for disconnection later
				LinkedList<Character> pathCopy = new LinkedList<Character>(path);

				// Create connection for every connection in VC
				boolean success = true;
				while (path.size() > 1) {
					char pop = path.pop();
					popped.add(pop);
					currDelay += network.getDelay(pop, path.peek());
					if (!network.addVC(pop, path.peek())) {
						success = false;
						while (popped.size() > 1) {
							network.removeVC(popped.pop(), popped.peek());
						}
						break;
					}
				}

				// Keep track of successful/blocked connections
				if (success) {
					numSuccessful++;
					numHops += pathCopy.size() - 1;
					VCs.put(currRequest.setupTime, pathCopy);
					cumPropDelay += currDelay;
					// System.out.print("SUCCESS: ");
					// currRequest.print();
					// System.out.println(pathCopy.toString());
					// System.out.println(" SUCCESS");

				} else {
					numBlocked++;
					// System.out.print("BLOCKED: ");
					// currRequest.print();
					// System.out.println(pathCopy.toString());
					// System.out.println(" BLOCKED");
				}

			} else { // Remove the Virtual Circuit

				// currRequest.print();
				path = VCs.get(currRequest.setupTime);
				if (path == null) {
					// Must have been blocked
					// System.out.println("No path stored!!");
				} else {
					while (path.size() > 1) {
						char pop = path.pop();
						network.removeVC(pop, path.peek());
					}
				}
			}
		}

		// Print Results

		percSuccessful = (float) 100 * numSuccessful / numRequests;
		percBlocked = (float) 100 * numBlocked / numRequests;
		avgHops = (float) numHops / numSuccessful;
		avgDelay = (float) cumPropDelay / numSuccessful;

		printResults();
		
		return;

	}

	private static void buildRequestPQ() {
		// Initialize variables

		numRequests = 0;
		char source;
		char dest;
		float setupTime;
		float duration;
		Request currRequest;
		requestPQ = new PriorityQueue<Request>();

		BufferedReader br = null;

		try {
			String wCurrentLine;
			br = new BufferedReader(new FileReader(workloadFile));
			while ((wCurrentLine = br.readLine()) != null) {
				// Process Line
				String[] request = wCurrentLine.split(" ");
				numRequests++;

				source = request[1].charAt(0);
				dest = request[2].charAt(0);
				setupTime = Float.valueOf(request[0]) * 1000; // in msec
				duration = Float.valueOf(request[3]) * 1000; // in msec

				currRequest = new Request(true, setupTime, setupTime, source,
						dest);
				requestPQ.add(currRequest);
				currRequest = new Request(false, setupTime, setupTime
						+ duration, source, dest);
				requestPQ.add(currRequest);

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	private static void buildNetwork() throws Exception {
		network = new NetworkGraph();

		BufferedReader br = null;

		try {

			String sCurrentLine;
			br = new BufferedReader(new FileReader(topologyFile));
			while ((sCurrentLine = br.readLine()) != null) {
				// Process Line
				String[] current = sCurrentLine.split(" ");

				char a = current[0].charAt(0);
				char b = current[1].charAt(0);
				int dprop = Integer.parseInt(current[2]);
				int cap = Integer.parseInt(current[3]);

				network.addConnection(a, b, dprop, cap);

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	private static void printResults() {

		System.out.println("total number of virtual circuit requests: "
				+ numRequests);
		System.out.println("number of successfully routed requests: "
				+ numSuccessful);
		System.out.printf(
				"percentage of successfully routed requests: %.2f \n",
				percSuccessful);
		System.out.println("number of blocked requests: " + numBlocked);
		System.out.printf("percentage of blocked requests: %.2f \n",
				percBlocked);
		System.out.printf("average number of hops per circuit: %.2f \n",
				avgHops);
		System.out.printf(
				"average cumulative propagation delay per circuit: %.2f \n",
				avgDelay);
		return;
	}

	public static class Request implements Comparable<Object> {

		public final char A;
		public final char B;
		public final float setupTime;
		public final float exTime;
		public boolean connect;

		public Request(boolean connect, float setupTime, float executionTime,
				char to, char from) {
			this.A = to;
			this.B = from;
			this.setupTime = setupTime; // msec
			this.exTime = executionTime; // msec
			this.connect = connect;
		}

		@Override
		public int compareTo(Object o) {
			Request r2 = (Request) o;
			return (int) (1000000 * this.exTime - 1000000 * r2.exTime);
		}

		public void print() {
			if (this.connect)
				System.out.print("Connect ");
			if (!this.connect)
				System.out.print("Disconnect ");
			System.out.printf("%C - %C at %f seconds \n", this.A, this.B,
					this.exTime / 1000);
		}
	}

	public static class NetworkGraph {

		// Connection class

		public class Connection {

			public final char from;
			public final char to;
			public final int dprop;
			public final int cap;
			public int numVCs;

			public Connection(char from, char to, int dprop, int cap) {
				this.from = from;
				this.to = to;
				this.dprop = dprop;
				this.cap = cap;
				this.numVCs = 0;
			}

			public boolean addVC() {
				if (numVCs + 1 > cap)
					return false;
				numVCs++;
				return true;
			}

			public void removeVC() {
				numVCs--;
			}

			public String toString() {
				return from + "--" + to;
			}
		}

		// Network Graph

		private final HashMap<Character, HashSet<Connection>> graph;

		public NetworkGraph() {
			graph = new HashMap<Character, HashSet<Connection>>();
		}

		public Set<Character> getNodes() {
			return graph.keySet();
		}

		public boolean addHost(char host) {
			if (graph.keySet().contains(host))
				return false;
			graph.put(host, new HashSet<Connection>());
			return true;
		}

		public void addConnection(char start, char dest, int dprop, int cap)
				throws Exception {

			if (!graph.containsKey(start))
				addHost(start);
			if (!graph.containsKey(dest))
				addHost(dest);

			Connection c = new Connection(start, dest, dprop, cap);
			Connection d = new Connection(dest, start, dprop, cap);

			graph.get(start).add(c);
			graph.get(dest).add(d);
		}

		public boolean isAdjacent(char start, char end) throws Exception {
			if (!graph.containsKey(start) || !graph.containsKey(end))
				throw new Exception("No such nodes in the graph.");

			Set<Connection> connections = this.getConnections(start);
			for (Connection c : connections) {
				if (c.to == end)
					return true;
			}
			return false;
		}

		public Set<Connection> getConnections(char node) throws Exception {
			Set<Connection> neighbors = graph.get(node);
			if (neighbors == null)
				throw new Exception("No such node in the graph.");

			return Collections.unmodifiableSet(neighbors);
		}

		public ArrayList<Character> getNeighbors(char node) throws Exception {
			ArrayList<Character> out = new ArrayList<Character>();
			
			for (char c : graph.keySet()) {
				if (c == node)
					continue;
				if (isAdjacent(c, node))
					out.add(c);
			}
			
			return out;
		}

		public int getDelay(char start, char finish) {
			Set<Connection> neighbors = graph.get(start);
			for (Connection c : neighbors) {
				if (c.to == finish)
					return c.dprop;
			}
			return Integer.MAX_VALUE;
		}

		public int getCap(char start, char finish) {
			Set<Connection> neighbors = graph.get(start);
			for (Connection c : neighbors) {
				if (c.to == finish)
					return c.cap;
			}
			return Integer.MAX_VALUE;
		}

		public int getLoad(char start, char finish) {
			Set<Connection> neighbors = graph.get(start);
			for (Connection c : neighbors) {
				if (c.to == finish)
					return c.numVCs;
			}
			return Integer.MAX_VALUE;
		}

		public boolean addVC(char a, char b) throws Exception {
			Set<Connection> aConnections = this.getConnections(a);
			Set<Connection> bConnections = this.getConnections(b);
			for (Connection c : aConnections) {
				if (c.to == b) {
					c.addVC();
					continue;
				}
			}
			for (Connection d : bConnections) {
				if (d.to == a)
					return d.addVC();
			}
			return false;
		}

		public void removeVC(char a, char b) throws Exception {
			Set<Connection> aConnections = this.getConnections(a);
			Set<Connection> bConnections = this.getConnections(b);
			for (Connection c : aConnections) {
				if (c.to == b)
					c.removeVC();
			}
			for (Connection d : bConnections) {
				if (d.to == a)
					d.removeVC();
			}
			return;
		}

		public int size() {
			return graph.size();
		}

		public boolean isEmpty() {
			return graph.isEmpty();
		}
	}

	public static class DijkstraAlgorithm {

		private final NetworkGraph graph;
		private HashSet<Character> N;
		private HashMap<Character, Character> P;
		private HashMap<Character, Integer> Cost;

		public DijkstraAlgorithm(NetworkGraph graph) {
			this.graph = graph;
		}

		// Generate Forwarding Table
		public void execute(Character source) throws Exception {
			N = new HashSet<Character>();
			P = new HashMap<Character, Character>();
			Cost = new HashMap<Character, Integer>();

			Cost.put(source, 0);
			N.add(source);

			while (!N.isEmpty()) {
				char node = getMin(N);
				N.remove(node);
				findMinCost(node);
			}

		}

		private char getMin(HashSet<Character> N) {
			Character min = null;
			int cCost = 0;
			int minCost = 0;
			ArrayList<Character> Ties = new ArrayList<Character>();
			Random randomGenerator = new Random();

			for (char c : N) {
				if (min == null)
					min = c;
				else {
					cCost = getLowestCost(c);
					minCost = getLowestCost(c);
					if (cCost < minCost) {
						min = c;
						Ties.clear();
						Ties.add(min);
					} else if (cCost == minCost) {
						Ties.add(c);
					}
				}
			}
			
			if (Ties.size()<=1) return min;
			
			int index = randomGenerator.nextInt(Ties.size());

			return Ties.get(index);
		}

		private int getLowestCost(char dest) {
			Integer d = Cost.get(dest);
			if (d == null)
				return Integer.MAX_VALUE;
			return d;
		}

		public void findMinCost(char node) throws Exception {
			ArrayList<Character> neighbors = graph.getNeighbors(node);

			for (Character dest : neighbors) {

				if (routingScheme.equals("SHP")) {
				// Shortest Hop Path
					if (getLowestCost(dest) > getLowestCost(node) + 1) {
						Cost.put(dest, getLowestCost(node) + 1);
						P.put(dest, node);
						N.add(dest);
					}
					
				} else if (routingScheme.equals("SDP")) {
					if (getLowestCost(dest) > getLowestCost(node)
							+ graph.getDelay(node, dest)) {
						Cost.put(
								dest,
								getLowestCost(node)
										+ graph.getDelay(node, dest));
						P.put(dest, node);
						N.add(dest);
					}
				} else if (routingScheme.equals("LLP")) {
					int load = graph.getLoad(node, dest)
							/ graph.getCap(node, dest);
					if (getLowestCost(dest) > getLowestCost(node) + load) {
						Cost.put(dest, getLowestCost(node) + load);
						P.put(dest, node);
						N.add(dest);
					}
				} else break;
			}
			return;

		}

		public LinkedList<Character> getPath(char dest) {

			LinkedList<Character> path = new LinkedList<Character>();
			char hop = dest;

			if (P.get(hop) == null)
				return null; // Path does not exist

			path.add(hop);

			while (P.get(hop) != null) { // Trace back through the forwarding
											// table to the source
				hop = P.get(hop);
				path.add(hop); // Build path
			}

			return path;
		}

	}
}
