import java.util.ArrayList;

public class PermanentNode { //class for use as part of calculating Dijkstra's shortest path algorithm
    ArrayList<Router> path;
    int cost;
    Router node;
    public PermanentNode(Router node, ArrayList<Router> path, int cost) {
        this.path = path;
        this.cost = cost;
        this.node = node;
    }
}

