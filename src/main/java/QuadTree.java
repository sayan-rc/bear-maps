import java.lang.reflect.Array;

public class QuadTree<T> {
    private T root;
    QuadTree<T>[] children;

    public QuadTree(T r) {
        root = r;
        children = (QuadTree<T>[]) Array.newInstance(QuadTree.class, 4);
        for (int i = 0; i < 4; i++) {
            children[i] = null;
        }
    }

    public QuadTree(T r, QuadTree[] c) {
        root = r;
        children = (QuadTree<T>[]) Array.newInstance(QuadTree.class, 4);
        System.arraycopy(c, 0, children, 0, c.length);
    }

    public T root() {
        return root;
    }

    public QuadTree[] children() {
        return children;
    }

    public void setChildren(QuadTree[] c) {
        System.arraycopy(c, 0, children, 0, c.length);
    }

    public boolean isLeaf() {
        return children[0] == null && children[1] == null && children[2] == null && children[3] == null;
    }
}
