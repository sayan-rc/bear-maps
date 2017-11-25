import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    private static class Node {
        String name;
        long id;
        double lat;
        double lon;
        ArrayList<Long> adj;

        Node(long i, double la, double lo) {
            id = i;
            lat = la;
            lon = lo;
            adj = new ArrayList<>();
            name = "";
        }

        void setName(String n) {
            name = n;
        }
    }

    private HashMap<Long, Node> nodes = new HashMap<>();
    private HashMap<String, ArrayList<Long>> ways = new HashMap<>();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        HashMap<Long, Node> copy = new HashMap<>();
        for (Long n : nodes.keySet()) {
            copy.put(n, nodes.get(n));
        }
        for (Long n : nodes.keySet()) {
            if (nodes.get(n).adj.size() == 0) {
                copy.remove(n);
            }
        }
        nodes = copy;
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        return nodes.keySet();
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        ArrayList<Long> adj = new ArrayList<>();
        for (Long a : nodes.get(v).adj) {
            adj.add(a);
        }
        return adj;
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        return dist(lon(v), lon(w), lat(v), lat(w));
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        double minDistance = Double.MAX_VALUE;
        long closest = 0;
        for (Long n : nodes.keySet()) {
            if (dist(lon(n), lon, lat(n), lat) < minDistance) {
                minDistance = dist(lon(n), lon, lat(n), lat);
                closest = n;
            }
        }
        return closest;
    }

    double dist(double lon1, double lon2, double lat1, double lat2) {
        return Math.sqrt(Math.pow(lon1 - lon2, 2) + Math.pow(lat1 - lat2, 2));
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return nodes.get(v).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return nodes.get(v).lat;
    }

    void addNode(long id, double lat, double lon) {
        nodes.put(id, new Node(id, lat, lon));
    }

    void setNodeName(long v, String name) {
        nodes.get(v).setName(name);
    }

    String nodeName(long v) {
        return nodes.get(v).name;
    }

    void addEdge(long v, long w) {
        nodes.get(v).adj.add(w);
        nodes.get(w).adj.add(v);
    }

    void addWay(String name, ArrayList<Long> wayNodes) {
        ways.put(name, wayNodes);
    }
}
