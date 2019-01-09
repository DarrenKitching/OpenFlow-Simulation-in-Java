/*
• Send messages addressed to another end-node
• Receive messages
*/

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import tcdIO.*;

public class Endnode extends Node {
    int endNodeNumber;
    InetSocketAddress srcAddress;
    Terminal terminal;

    Endnode(Terminal terminal, String dstHost, int srcPort, int endNodeNumber) {
        try {
            this.terminal = terminal;
            socket = new DatagramSocket(srcPort);
            srcAddress = new InetSocketAddress(dstHost, srcPort);
            listener.go();
            this.endNodeNumber = endNodeNumber;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public void onReceipt(DatagramPacket packet) {
        terminal.println("Message: " + Initialize.packetizer.getMessage(packet) + " received");
    }

    public synchronized void start() throws Exception {
        int endNodeReceiver;
        String message;
        while (true) {
            while (true) {
                terminal.println("Enter the endnode number you would like to send a message to in digit form: ");
                endNodeReceiver = terminal.readInt();
                if (endNodeReceiver >= Initialize.endnodes.size()) {
                    terminal.println("There is no endnode of that number");
                } else if (endNodeReceiver == this.endNodeNumber) {
                    terminal.println("You can't send a message from one endnode to itself");
                } else {
                    break;
                }
            }
            terminal.println("Enter the message you would like to send to endnode " + endNodeNumber + ":");
            message = terminal.readString();
            if(this.endNodeNumber == 0) { //endnode 0 connected to router 0
                DatagramPacket packet = Initialize.packetizer.toDatagramPacket(message, Packetizer.OFPT_PACKET_IN, (byte) this.endNodeNumber, (byte) endNodeReceiver, Initialize.endnodes.get(endNodeReceiver).srcAddress);
                packet.setSocketAddress(Initialize.routers.get(0).srcAddress); //Endnode 0 connected to router 0
                socket.send(packet);
                terminal.println("Sent packet to router 0 to handle forwarding" );
            }
            else if(this.endNodeNumber == 1) {//endnode 1 connected to router 2
                DatagramPacket packet = Initialize.packetizer.toDatagramPacket(message, Packetizer.OFPT_PACKET_IN, (byte) this.endNodeNumber, (byte) endNodeReceiver, Initialize.endnodes.get(endNodeReceiver).srcAddress);
                packet.setSocketAddress(Initialize.routers.get(2).srcAddress); //Endnode 1 connected to router 2
                socket.send(packet);
                terminal.println("Sent packet to router 2 to handle forwarding" );
            }
            else if(this.endNodeNumber == 2) {//endnode 2 connected to router 7
                DatagramPacket packet = Initialize.packetizer.toDatagramPacket(message, Packetizer.OFPT_PACKET_IN, (byte) this.endNodeNumber, (byte) endNodeReceiver, Initialize.endnodes.get(endNodeReceiver).srcAddress);
                packet.setSocketAddress(Initialize.routers.get(7).srcAddress); //Endnode 2 connected to router 7
                socket.send(packet);
                terminal.println("Sent packet to router 7 to handle forwarding" );
            }
        }
    }
}
