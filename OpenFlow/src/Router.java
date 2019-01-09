import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import tcdIO.*;

public class Router extends Node {
    ArrayList<FlowTable> flowTables = new ArrayList<FlowTable>();
    Terminal terminal;
    InetSocketAddress srcAddress;
    int routerNumber;
    ArrayList<DatagramPacket> waitingForFlowMod = new ArrayList<>();
    Connection connectionOne;
    Connection connectionTwo;
    Connection connectionThree; // Each router has at least two connections(as per diagram in report) and possibly a third one

    Router(Terminal terminal, String dstHost, int srcPort, int routerNumber) {
        try {
            this.terminal = terminal;
            this.routerNumber = routerNumber;
            socket = new DatagramSocket(srcPort);
            srcAddress = new InetSocketAddress(dstHost, srcPort);
            listener.go();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public void onReceipt(DatagramPacket packet) throws IOException {
        if (Initialize.packetizer.getTask(packet) == 0) {
            terminal.println("'Hello' response received");
        } else if (Initialize.packetizer.getTask(packet) == Packetizer.OFPT_FEATURES_REQUEST) {
            terminal.println("Feature request received sending feature reply");
            DatagramPacket responsePacket = Initialize.packetizer.toDatagramPacket("", Packetizer.OFPT_FEATURES_REPLY, (byte) this.routerNumber, (byte) 0,
                    Initialize.controller.srcAddress);
            responsePacket.setSocketAddress(Initialize.controller.srcAddress);
            socket.send(responsePacket);
        } else if (Initialize.packetizer.getTask(packet) == Packetizer.OFPT_PACKET_IN) {
            terminal.println("Packet In received addressed to endnode " + Initialize.packetizer.getReceiverNumber(packet) + " forwarding");
            forwardMessage(packet);
        } else if (Initialize.packetizer.getTask(packet) == Packetizer.OFPT_FLOW_MOD) {
            //add flow mod to flow mod table

            for (int i = 0; i < waitingForFlowMod.size(); i++) {
                DatagramPacket packetOnHold = waitingForFlowMod.get(i);
                forwardMessage(packetOnHold);
            }
        }
    }

    public void sendHelloToController() throws IOException {
        DatagramPacket packet = Initialize.packetizer.toDatagramPacket("Hello", (byte) 0, (byte) this.routerNumber, (byte) 0, Initialize.controller.srcAddress);
        packet.setSocketAddress(Initialize.controller.srcAddress);
        socket.send(packet);
    }

    public void forwardMessage(DatagramPacket packet) throws IOException {
        FlowTable flowTable;
        if (this.routerNumber == 0 && Initialize.packetizer.getReceiverNumber(packet) == 0) { //hardcoded router last stop to endnode destination
            DatagramPacket forward = Initialize.packetizer.toDatagramPacket(Initialize.packetizer.getMessage(packet), Packetizer.OFPT_PACKET_IN, (byte) this.routerNumber,
                    Initialize.packetizer.getReceiverNumber(packet), Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
            forward.setSocketAddress(Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
            socket.send(forward);
        } else if (this.routerNumber == 2 && Initialize.packetizer.getReceiverNumber(packet) == 1) { //hardcoded router last stop to endnode destination
            DatagramPacket forward = Initialize.packetizer.toDatagramPacket(Initialize.packetizer.getMessage(packet), Packetizer.OFPT_PACKET_IN, (byte) this.routerNumber,
                    Initialize.packetizer.getReceiverNumber(packet), Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
            forward.setSocketAddress(Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
            socket.send(forward);
        } else if (this.routerNumber == 7 && Initialize.packetizer.getReceiverNumber(packet) == 2) { //hardcoded router last stop to endnode destination
            DatagramPacket forward = Initialize.packetizer.toDatagramPacket(Initialize.packetizer.getMessage(packet), Packetizer.OFPT_PACKET_IN, (byte) this.routerNumber,
                    Initialize.packetizer.getReceiverNumber(packet), Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
            forward.setSocketAddress(Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
            socket.send(forward);
        } else {
            for (int i = 0; i < flowTables.size(); i++) { //flow table already exists to this destination
                flowTable = flowTables.get(i);
                if (flowTable.getDestination() == Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet))) {
                    terminal.println("Flow table found, forwarding packet");
                    flowTableExists(packet, flowTable);
                    if (waitingForFlowMod.contains(packet))
                        waitingForFlowMod.remove(packet);
                    return;
                }
            }
            terminal.println("Flow table not found, requesting flow table from controller");
            requestFlowModFromController(packet);
        }
    }

    public void flowTableExists(DatagramPacket packet, FlowTable flowTable) throws IOException {
        Router node;
        for (int i = 0; i < flowTable.getRoute().size(); i++) {
            node = flowTable.getRoute().get(i);
            if (node == this) {
                if (i == flowTable.getRoute().size() - 1) { //this is last router before sending to endnode
                    DatagramPacket forward = Initialize.packetizer.toDatagramPacket(Initialize.packetizer.getMessage(packet), Packetizer.OFPT_PACKET_IN, (byte) this.routerNumber,
                            Initialize.packetizer.getReceiverNumber(packet), Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
                    forward.setSocketAddress(Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
                    socket.send(forward);
                    break;
                } else { //must forward packet to next router
                    Router nextRouter = flowTable.getRoute().get(i + 1); //next hop
                    DatagramPacket forward = Initialize.packetizer.toDatagramPacket(Initialize.packetizer.getMessage(packet), Packetizer.OFPT_PACKET_IN, (byte) this.routerNumber,
                            Initialize.packetizer.getReceiverNumber(packet), nextRouter.srcAddress);
                    forward.setSocketAddress(nextRouter.srcAddress);
                    socket.send(forward);
                }
            }
        }
    }

    public void requestFlowModFromController(DatagramPacket packet) throws IOException {
        if (!waitingForFlowMod.contains(packet)) { //make sure packet isn't already in queue waiting for FlowMod
            waitingForFlowMod.add(packet); //add to storage until reply from controller is received
            DatagramPacket flowMod = Initialize.packetizer.toDatagramPacket(Initialize.packetizer.getMessage(packet), Packetizer.OFPT_FLOW_MOD, (byte) this.routerNumber,
                    Initialize.packetizer.getReceiverNumber(packet), Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)).srcAddress);
            flowMod.setSocketAddress(Initialize.controller.srcAddress);
            socket.send(flowMod);
        }
    }

    public synchronized void start() throws Exception {
        terminal.println("Sending hello message to controller");
        sendHelloToController();
    }
}