import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    private static class SearchNode implements Comparable {
        long id;
        double gscore;
        double hscore;
        double fscore;
        SearchNode parent;

        SearchNode(long i, double g, double h) {
            id = i;
            gscore = g;
            hscore = h;
            fscore = g + h;
            parent = null;
        }

        SearchNode(long i, double g, double h, SearchNode p) {
            id = i;
            gscore = g;
            hscore = h;
            fscore = g + h;
            parent = p;
        }

        @Override
        public int compareTo(Object o) {
            if (fscore > ((SearchNode) o).fscore) {
                return 1;
            } else if (fscore > ((SearchNode) o).fscore) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon,
                                                double stlat, double destlon, double destlat) {
        HashMap<Long, Double> closed = new HashMap<>();
        HashMap<Long, Double> open = new HashMap<>();
        PriorityQueue<SearchNode> pq = new PriorityQueue<>();
        long start = g.closest(stlon, stlat);
        long dest = g.closest(destlon, destlat);
        SearchNode destNode = new SearchNode(dest, Double.MAX_VALUE, 0);
        pq.add(new SearchNode(start, 0, 0));
        open.put(start, 0.0);
        while (!open.isEmpty()) {
            SearchNode curr = pq.remove();
            open.remove(curr.id);
            for (Long a : g.adjacent(curr.id)) {
                SearchNode adj = new SearchNode(a, curr.gscore
                        + g.distance(curr.id, a), g.distance(a, dest), curr);
                if (a == dest && adj.gscore < destNode.gscore) {
                    destNode = adj;
                    break;
                }
                if (adj.gscore >= destNode.gscore) {
                    break;
                }
                if (!(open.containsKey(a) && open.get(a) < adj.fscore)
                        && !(closed.containsKey(a) && closed.get(a) < adj.fscore)) {
                    pq.add(adj);
                    open.put(a, adj.fscore);
                }
            }
            closed.put(curr.id, curr.fscore);
        }
        return getPath(destNode);
    }

    public static LinkedList<Long> getPath(SearchNode node) {
        LinkedList<Long> path = new LinkedList<>();
        path.add(node.id);
        while (node.parent != null) {
            path.addFirst(node.parent.id);
            node = node.parent;
        }
        return path;
    }
}
