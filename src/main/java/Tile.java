public class Tile implements Comparable {
    private int image;
    private double ullon;
    private double ullat;
    private double lrlon;
    private double lrlat;
    private double londpp;
    private static String dir = "";
    private String file;

    public Tile(int img, double ulo, double ula, double llo, double lla, double dpp) {
        image = img;
        ullon = ulo;
        ullat = ula;
        lrlon = llo;
        lrlat = lla;
        londpp = dpp;
        file = dir + ((Integer) image).toString() + ".png";
    }

    public Tile(int img, double ulo, double ula, double llo, double lla, double dpp, String d) {
        image = img;
        ullon = ulo;
        ullat = ula;
        lrlon = llo;
        lrlat = lla;
        londpp = dpp;
        dir = d;
        file = dir + ((Integer) image).toString() + ".png";
    }

    public int image() {
        return image;
    }

    public double ullon() {
        return ullon;
    }

    public double ullat() {
        return ullat;
    }

    public double lrlon() {
        return lrlon;
    }

    public double lrlat() {
        return lrlat;
    }

    public double londpp() {
        return londpp;
    }

    public String file() {
        return file;
    }

    @Override
    public int compareTo(Object o) {
        if (ullat > ((Tile)o).ullat()) {
            return -1;
        } else if (ullat < ((Tile)o).ullat()) {
            return 1;
        } else if (ullon < ((Tile)o).ullon()) {
            return -1;
        } else if (ullon > ((Tile)o).ullon()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "(ullon=" + ullon + ", ullat=" + ullat + ")";
    }
}
