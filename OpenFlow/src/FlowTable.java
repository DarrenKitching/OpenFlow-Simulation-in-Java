import java.util.ArrayList;

public class FlowTable {
    private Endnode destination;
    private ArrayList<Router> route;

    public FlowTable(Endnode destination, ArrayList<Router> route) {
        this.destination = destination;
        this.route = route;
    }

    public Endnode getDestination() {
        return destination;
    }

    public ArrayList<Router> getRoute() {
        return route;
    }

    public void addNodeToRoute(Router nextHop) {
        route.add(nextHop);
    }
}
