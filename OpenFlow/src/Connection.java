//This is a class of hardcoded connections between routers to implement Dijkstra's shortest path algorithm

public class Connection {
    Router fromRouter;
    Router toRouter;
    int cost;
    public Connection(Router fromRouter, Router toRouter, int cost) {
        this.fromRouter = fromRouter;
        this.toRouter = toRouter;
        this.cost = cost;
    }
}
