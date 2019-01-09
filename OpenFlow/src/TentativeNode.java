import java.util.ArrayList;

public class TentativeNode { //class for use as part of calculating Dijkstra's shortest path algorithm
    ArrayList<Router> pathSoFar;
    int cost;
    Router node;
    public TentativeNode(Router node, ArrayList<Router> pathSoFar, int cost) {
        this.pathSoFar = pathSoFar;
        this.cost = cost;
        this.node = node;
    }
}
