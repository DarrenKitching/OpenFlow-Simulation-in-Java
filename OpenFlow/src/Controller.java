/*
• Accept contact from switches and issue feature request
• Accept PacketIn messages
• Send out FlowMod messages
*/

import tcdIO.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;

public class Controller extends Node {
    InetSocketAddress srcAddress;
    Terminal terminal;

    public Controller(Terminal terminal, String dstHost, int srcPort) {
        try {
            this.terminal = terminal;
            socket = new DatagramSocket(srcPort);
            srcAddress = new InetSocketAddress(dstHost, srcPort);
            listener.go();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public FlowTable getPath(Router start, Endnode destination) { //function uses Dijkstra's shortest path algorithm to determine optimal route between router and endnode
        FlowTable flowTable = new FlowTable(destination, (new ArrayList<Router>()));
        LinkedList<TentativeNode> tentativeNodes = new LinkedList<>();
        LinkedList<PermanentNode> permanentNodes = new LinkedList<>();

        tentativeNodes.add(new TentativeNode(start, new ArrayList<Router>(), 0));// add first node with cost zero
        while (tentativeNodes.size() != 0) {
            TentativeNode smallestCost = tentativeNodes.get(0); //start with first in list and compare with others
            for (int i = 1; i < tentativeNodes.size(); i++) {
                if (tentativeNodes.get(i).cost < smallestCost.cost)
                    smallestCost = tentativeNodes.get(i);
            }
            //smallest node found
            PermanentNode permanentNode = new PermanentNode(smallestCost.node, new ArrayList<Router>(smallestCost.pathSoFar), smallestCost.cost);
            permanentNode.path.add(permanentNode.node);
            permanentNodes.add(permanentNode); //make smallest cost permanent
            tentativeNodes.remove(smallestCost);
            for (int i = 0; i < tentativeNodes.size(); i++) { //remove repeat of node that was made permanent if there's a repeat in the tentative nodes(two ways to get to same node we only keep the smallest route)
                if (smallestCost.node == tentativeNodes.get(i).node) {
                    tentativeNodes.remove(tentativeNodes.get(i));
                }
            }
            //ensure that we are not making permanent nodes tentative again
            boolean containsConnectionOne = false;
            boolean containsConnectionTwo = false;
            boolean containsConnectionThree = false;
            for(int i = 0; i < permanentNodes.size(); i++) {
                PermanentNode checkNode = permanentNodes.get(i);
                if(checkNode.node == smallestCost.node.connectionOne.toRouter)
                    containsConnectionOne = true;
                if(checkNode.node == smallestCost.node.connectionTwo.toRouter)
                    containsConnectionTwo = true;
                if(smallestCost.node.connectionThree == null || checkNode.node == smallestCost.node.connectionThree.toRouter)
                    containsConnectionThree = true;
            }
            if(!containsConnectionOne) {
                TentativeNode newNode = new TentativeNode(smallestCost.node.connectionOne.toRouter, new ArrayList<Router>(smallestCost.pathSoFar), smallestCost.cost + smallestCost.node.connectionOne.cost);
                newNode.pathSoFar.add(smallestCost.node);
                tentativeNodes.add(newNode);
            }
            if(!containsConnectionTwo) {
                TentativeNode newNode = new TentativeNode(smallestCost.node.connectionTwo.toRouter, new ArrayList<Router>(smallestCost.pathSoFar), smallestCost.cost + smallestCost.node.connectionTwo.cost);
                newNode.pathSoFar.add(smallestCost.node);
                tentativeNodes.add(newNode);
            }
            if(!containsConnectionThree) {
                TentativeNode newNode = new TentativeNode(smallestCost.node.connectionThree.toRouter, new ArrayList<Router>(smallestCost.pathSoFar), smallestCost.cost + smallestCost.node.connectionThree.cost);
                newNode.pathSoFar.add(smallestCost.node);
                tentativeNodes.add(newNode);
            }
        }
        switch (destination.endNodeNumber) { //Endnode 0 is connected to router 0. Endnode 1 is connected to router 2. Endnode 3 is connected to router 7.
            case 0:
                for (int i = 0; i < permanentNodes.size(); i++) {
                    if (permanentNodes.get(i).node.routerNumber == 0) {
                        for (int j = 0; j < permanentNodes.get(i).path.size(); j++) {
                            flowTable.addNodeToRoute(permanentNodes.get(i).path.get(j));
                        }
                    }
                }
                break;
            case 1:
                for (int i = 0; i < permanentNodes.size(); i++) {
                    if (permanentNodes.get(i).node.routerNumber == 2) {
                        for (int j = 0; j < permanentNodes.get(i).path.size(); j++) {
                            flowTable.addNodeToRoute(permanentNodes.get(i).path.get(j));
                        }
                    }
                }
                break;
            case 2:
                for (int i = 0; i < permanentNodes.size(); i++) {
                    if (permanentNodes.get(i).node.routerNumber == 7) {
                        for (int j = 0; j < permanentNodes.get(i).path.size(); j++) {
                            flowTable.addNodeToRoute(permanentNodes.get(i).path.get(j));
                        }
                    }
                }
                break;
            default:
                System.out.println("Error with number of endnodes");
        }
        return flowTable;
    }


    public void onReceipt(DatagramPacket packet) throws IOException {
        if (Initialize.packetizer.getTask(packet) == Packetizer.OFPT_HELLO) {
            terminal.println("Hello message received from router " + Initialize.packetizer.getSenderNumber(packet));
            terminal.println("Sending response");
            DatagramPacket responsePacket = Initialize.packetizer.toDatagramPacket("Hello", Packetizer.OFPT_HELLO, (byte) 0, Initialize.packetizer.getSenderNumber(packet), Initialize.routers.get(Initialize.packetizer.getSenderNumber(packet)).srcAddress);
            responsePacket.setSocketAddress(Initialize.routers.get(Initialize.packetizer.getSenderNumber(packet)).srcAddress);
            socket.send(responsePacket);
            terminal.println("Hello response sent to router " + Initialize.packetizer.getSenderNumber(packet) + " sending feature request");
            responsePacket = Initialize.packetizer.toDatagramPacket("", Packetizer.OFPT_FEATURES_REQUEST, (byte) 0, Initialize.packetizer.getSenderNumber(packet), Initialize.routers.get(Initialize.packetizer.getSenderNumber(packet)).srcAddress);
            socket.send(responsePacket);
            terminal.println();
        } else if (Initialize.packetizer.getTask(packet) == Packetizer.OFPT_FEATURES_REPLY) {
            terminal.println("Feature Reply received from router number " + Initialize.packetizer.getSenderNumber(packet));
        } else if (Initialize.packetizer.getTask(packet) == Packetizer.OFPT_FLOW_MOD) {
            terminal.println("Received request for new flow mod from router " + Initialize.packetizer.getSenderNumber(packet));
            sendFlowMod(packet);
        }

    }

    public void sendFlowMod(DatagramPacket packet) throws IOException {
        FlowTable flowTable = getPath(Initialize.routers.get(Initialize.packetizer.getSenderNumber(packet)), Initialize.endnodes.get(Initialize.packetizer.getReceiverNumber(packet)));
        Initialize.routers.get(Initialize.packetizer.getSenderNumber(packet)).flowTables.add(flowTable);
        DatagramPacket responsePacket = Initialize.packetizer.toDatagramPacket("", Packetizer.OFPT_FLOW_MOD, (byte) 0, Initialize.packetizer.getSenderNumber(packet), Initialize.routers.get(Initialize.packetizer.getSenderNumber(packet)).srcAddress);
        responsePacket.setSocketAddress(Initialize.routers.get(Initialize.packetizer.getSenderNumber(packet)).srcAddress);
        socket.send(responsePacket);
        terminal.println("Sending FlowMod");
    }//send new FlowTable that has been calculated onto the router.

    public synchronized void start() {
        terminal.println("Controller started");
    }
}
