import java.util.ArrayList;

import tcdIO.Terminal;

public class Initialize {
    static final String DEFAULT_DST_NODE = "localhost";
    static int endNodeStartPort = 50000;
    static int routerStartPort = 51000;
    static final int NUMBER_OF_ROUTERS = 8; //don't change
    static int numberOfEndnodes = 3;//don't change
    public static final int CONTROLLER_PORT = 52000;
    public static ArrayList<Endnode> endnodes = new ArrayList<>();
    public static ArrayList<Router> routers = new ArrayList<>();
    public static Controller controller;//one controller only
    public static Packetizer packetizer = new Packetizer();

    public static void createAll(int NUMBER_OF_ROUTERS, int numberOfEndnodes) { //create routers and endnodes
        Terminal terminal = new Terminal("Controller");
        controller = new Controller(terminal, DEFAULT_DST_NODE, CONTROLLER_PORT);
        for (int i = 0; i < numberOfEndnodes; i++) {
            terminal = new Terminal("Endnode " + i);
            Endnode endnode = new Endnode(terminal, DEFAULT_DST_NODE, endNodeStartPort++, i);
            endnodes.add(endnode);
        }

        for (int i = 0; i < NUMBER_OF_ROUTERS; i++) {
            terminal = new Terminal("Router " + i);
            Router router = new Router(terminal, DEFAULT_DST_NODE, routerStartPort++, i);
            routers.add(router);
        }
    }

    public static void implementHardCodedConnections() {//hardcoded costs between connections created for Dijkstra's shortest path algorithm.
        try {
            Router current;
            for (int i = 0; i < routers.size(); i++) {
                current = routers.get(i);
                switch (current.routerNumber) {
                    case 0:
                        current.connectionOne = new Connection(current, routers.get(1), 2);
                        current.connectionTwo = new Connection(current, routers.get(6), 6);
                        current.connectionThree = null;
                        break;
                    case 1:
                        current.connectionOne = new Connection(current, routers.get(0), 2);
                        current.connectionTwo = new Connection(current, routers.get(2), 7);
                        current.connectionThree = new Connection(current, routers.get(4), 2);
                        break;
                    case 2:
                        current.connectionOne = new Connection(current, routers.get(1), 7);
                        current.connectionTwo = new Connection(current, routers.get(3), 3);
                        current.connectionThree = new Connection(current, routers.get(5), 3);
                        break;
                    case 3:
                        current.connectionOne = new Connection(current, routers.get(2), 3);
                        current.connectionTwo = new Connection(current, routers.get(7), 2);
                        current.connectionThree = null;
                        break;
                    case 4:
                        current.connectionOne = new Connection(current, routers.get(1), 2);
                        current.connectionTwo = new Connection(current, routers.get(5), 2);
                        current.connectionThree = new Connection(current, routers.get(6), 1);
                        break;
                    case 5:
                        current.connectionOne = new Connection(current, routers.get(2), 3);
                        current.connectionTwo = new Connection(current, routers.get(4), 2);
                        current.connectionThree = new Connection(current, routers.get(7), 2);
                        break;
                    case 6:
                        current.connectionOne = new Connection(current, routers.get(0), 6);
                        current.connectionTwo = new Connection(current, routers.get(4), 1);
                        current.connectionThree = new Connection(current, routers.get(7), 4);
                        break;
                    case 7:
                        current.connectionOne = new Connection(current, routers.get(3), 2);
                        current.connectionTwo = new Connection(current, routers.get(5), 2);
                        current.connectionThree = new Connection(current, routers.get(6), 4);
                        break;
                    default:
                        System.out.println("This program was coded to work for exactly eight routers");

                }
            }
        } catch (NullPointerException e) {
            System.out.println("This program was coded to work for exactly eight routers");
        }
    }

    public static void startAll() throws Exception {
        for (int i = 0; i < routers.size(); i++) {
            int number = i;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        routers.get(number).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        for (int i = 0; i < endnodes.size(); i++) {
            int number = i;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        endnodes.get(number).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    controller.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static void main(String[] args) throws Exception {
        createAll(NUMBER_OF_ROUTERS, numberOfEndnodes);
        implementHardCodedConnections();
        startAll();
    }
}
