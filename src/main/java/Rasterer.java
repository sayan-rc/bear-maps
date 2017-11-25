import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.
    private QuadTree<Tile> tiles;

    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
        tiles = new QuadTree<>(new Tile(0, MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT,
                MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT,
                (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / MapServer.TILE_SIZE, imgRoot));
        buildTree(tiles);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        if (params.get("ullon") >= params.get("lrlon") || params.get("ullat")
                <= params.get("lrlat") || !intersects(tiles.root(), params.get("ullon"),
                params.get("ullat"), params.get("lrlon"), params.get("lrlat"))) {
            results.put("render_grid", new String[0][0]);
            results.put("raster_ul_lon", 0);
            results.put("raster_ul_lat", 0);
            results.put("raster_lr_lon", 0);
            results.put("raster_lr_lat", 0);
            results.put("depth", 0);
            results.put("query_success", false);
            return results;
        }
        ArrayList<Tile> raster = new ArrayList<>();
        ArrayDeque<QuadTree<Tile>> queue = new ArrayDeque<QuadTree<Tile>>();
        queue.addLast(tiles);
        results.put("depth", raster(queue, raster, params.get("ullon"), params.get("ullat"),
                params.get("lrlon"), params.get("lrlat"), params.get("w")));
        Collections.sort(raster);
        int width = 1;
        while (width < raster.size() && raster.get(width).ullat() == raster.get(0).ullat()) {
            width++;
        }
        int height = raster.size() / width;
        String[][] grid = new String[height][width];
        int k = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = raster.get(k).file();
                k++;
            }
        }
        results.put("render_grid", grid);
        results.put("raster_ul_lon", raster.get(0).ullon());
        results.put("raster_ul_lat", raster.get(0).ullat());
        results.put("raster_lr_lon", raster.get(raster.size() - 1).lrlon());
        results.put("raster_lr_lat", raster.get(raster.size() - 1).lrlat());
        results.put("query_success", true);
        return results;
    }

    private static void buildTree(QuadTree<Tile> tree) {
        Tile t = tree.root();
        if (t.image() / 1000000 >= 1) {
            return;
        }
        QuadTree<Tile>[] subtrees = (QuadTree<Tile>[]) Array.newInstance(QuadTree.class, 4);
        subtrees[0] = new QuadTree<>(new Tile(t.image() * 10 + 1, t.ullon(),
                t.ullat(), ((t.lrlon() - t.ullon()) / 2) + t.ullon(),
                ((t.ullat() - t.lrlat()) / 2) + t.lrlat(), t.londpp() / 2.0));
        subtrees[1] = new QuadTree<>(new Tile(t.image() * 10 + 2,
                ((t.lrlon() - t.ullon()) / 2) + t.ullon(), t.ullat(), t.lrlon(),
                ((t.ullat() - t.lrlat()) / 2) + t.lrlat(), t.londpp() / 2.0));
        subtrees[2] = new QuadTree<>(new Tile(t.image() * 10 + 3, t.ullon(),
                ((t.ullat() - t.lrlat()) / 2) + t.lrlat(),
                ((t.lrlon() - t.ullon()) / 2) + t.ullon(), t.lrlat(), t.londpp() / 2.0));
        subtrees[3] = new QuadTree<>(new Tile(t.image() * 10 + 4,
                ((t.lrlon() - t.ullon()) / 2) + t.ullon(), ((t.ullat() - t.lrlat()) / 2)
                + t.lrlat(), t.lrlon(), t.lrlat(), t.londpp() / 2.0));
        tree.setChildren(subtrees);
        for (QuadTree<Tile> c : tree.children()) {
            buildTree(c);
        }

    }

    private int raster(ArrayDeque<QuadTree<Tile>> queue, ArrayList<Tile> raster, double ullon,
                       double ullat, double lrlon, double lrlat, double width) {
        int depth = Integer.MAX_VALUE;
        while (!queue.isEmpty()) {
            QuadTree<Tile> tree = queue.removeLast();
            if ((((Integer) tree.root().image()).toString().length()) > depth) {
                return depth;
            }
            if (intersects(tree.root(), ullon, ullat, lrlon, lrlat)) {
                if (tree.root().londpp() <= (lrlon - ullon) / width || tree.isLeaf()) {
                    depth = ((Integer) tree.root().image()).toString().length();
                    raster.add(tree.root());
                } else {
                    for (QuadTree<Tile> c : tree.children()) {
                        queue.addFirst(c);
                    }
                }
            }
        }
        return depth;
    }

    private boolean intersects(Tile t, double ullon, double ullat,
                               double lrlon, double lrlat) {
        return !(t.lrlon() < ullon || lrlon < t.ullon())
                && !(t.lrlat() > ullat || lrlat > t.ullat());
    }
}
