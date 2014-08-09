import java.util.*;

public final class NetworkGraph<T> {
	
	//Connection class
	
	static class Connection {
		
		public final char from;
		public final char to;
		public final int dprop;
		public final int cap;
		
		public Connection(char from, char to, int dprop, int cap) {
			this.from = from;
			this.to = to;
			this.dprop = dprop;
			this.cap = cap;
		}
		
		public boolean equals(Object obj) {
			Connection c = (Connection)obj;
			return c.from == from && c.to ==to
					&& c.cap == cap && c.dprop == dprop;
		}
	}
	
	// Network Graph
	
    private final HashMap<Character, HashSet<Connection>> graph;
 
    public NetworkGraph() {
    	graph = new HashMap<Character, HashSet<Connection>>();
    }
    
    public boolean addHost(char host){
    	if (graph.containsKey(host))
    		return false;
    	graph.put(host, new HashSet<Connection>());
    	return true;
    }

    public void addConnection(char start, char dest, int dprop, int cap) throws Exception {
        
    	if (!graph.containsKey(start))  addHost(start);
    	if (!graph.containsKey(dest))  addHost(dest);
        
        Connection c = new Connection(start, dest, dprop, cap);
        Connection d = new Connection(dest, start, dprop, cap);
        
        graph.get(start).add(c);
        graph.get(dest).add(d);
       }
 
    public boolean isAdjacent(char start, char end) throws Exception {
     if (!graph.containsKey(start) || !graph.containsKey(end))
      throw new Exception("No such nodes in the graph.");
 
     Set<Connection> connections = this.getConnections(start);
     for (NetworkGraph.Connection c : connections) {
			if (c.to == end) return true;
		}
     return false;
    }
 
    public Set<Connection> getConnections(char node) throws Exception {
     Set<Connection> neighbors = graph.get(node);
     if (neighbors == null)
      throw new Exception("No such node in the graph.");
 
     return Collections.unmodifiableSet(neighbors);
    }
    
    public Set<Character> keySet() {
		return graph.keySet();
    }
 
    public int size() {
     return graph.size();
    }
    public boolean isEmpty() {
     return graph.isEmpty();
    }
}