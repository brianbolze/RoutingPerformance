RoutingPerformance
--Brian Bolze
--October 25 2013
==================

Summary
==================
Java program that simulates and compares the performance of three different permutations of Dijkstra's algorithm for routing packets of data on the internet.


Description
=================
In order to store the topology file in a convenient and useful format, I chose to build my own class for this specific scenario. The class, NetworkGraph, is made up of a hashmap that maps characters (representing the nodes) to Connections. Connections is a subclass within NetworkGraph that consists of two characters for the two nodes, and three integers for the connection propagation delay, capacity, and the current number of Virtual Circuits active on that connection. The Connection class has a few methods, including a print method (for debugging), and add and remove VC methods.

The NetworkGraph class has a few more methods. The addConnection method takes in the two characters and two integers from the topology file, creates a connection, and maps each node to the connection in the hashmap. Both nodes are mapped so that the direction is arbitrary. The isAdjacent method determines if two supplied nodes are neighbors. This is extremely helpful when performing the Dijkstra algorithm. After this there are a few basic get methods, and then an addVC method and removeVC method. The addVC method in this case only adds the virtual circuit if the path has sufficient capacity to support the connection.


Notes
================
Originally written as an assignment for a class taught at the University of New South Wales.
-- Computer Science 3331: Computer Networks and Applications
-- Professor Sanjay Jha
