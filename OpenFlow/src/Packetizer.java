import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class Packetizer implements PacketContent {
    //Flag header = 1 byte containing 10101010
    //Flag header is followed by one byte containing the length of the message in bytes
    //length of message is followed by the bytes containing the message
    //message is followed by one byte containing task number
    //task is followed by one byte containing the number of the sender
    //followed by one byte containing the number of the intended recipient
    //followed by control footer = 1 byte containing 10101010
    //Tasks:
    public static byte OFPT_HELLO = 0;
    public static byte OFPT_FEATURES_REQUEST = 5;
    public static byte OFPT_FEATURES_REPLY = 6;
    public static byte OFPT_PACKET_IN = 10;
    public static byte OFPT_FLOW_MOD = 14;

    public DatagramPacket toDatagramPacket(String message, byte task, byte senderNumber, byte receiverNumber, InetSocketAddress dstAddress) {
        byte packetData[] = new byte[message.length() + 6];
        packetData[0] = (byte) 170; //10101010
        packetData[1] = (byte) message.length();

        for (int i = 0; i < message.length(); i++)
            packetData[i + 2] = (byte) message.charAt(i);

        packetData[message.length() + 2] = task;
        packetData[message.length() + 3] = senderNumber;
        packetData[message.length() + 4] = receiverNumber;
        packetData[message.length() + 5] = (byte) 170; //10101010
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, dstAddress);

        return packet;
    }


    public String getMessage(DatagramPacket datagramPacket) {
        byte[] data = datagramPacket.getData();
        String message = "";
        int messageLength = (int) data[1];
        for (int i = 0; i < messageLength; i++) {
            message += (char) data[i + 2];
        }
        return message;
    }

    public byte getTask(DatagramPacket datagramPacket) {
        byte[] data = datagramPacket.getData();
        int messageLength = (int) data[1];
        return data[2 + messageLength];
    }

    public byte getSenderNumber(DatagramPacket datagramPacket) {
        byte[] data = datagramPacket.getData();
        int messageLength = (int) data[1];
        return data[3 + messageLength];
    }

    public byte getReceiverNumber(DatagramPacket datagramPacket) {
        byte[] data = datagramPacket.getData();
        int messageLength = (int) data[1];
        return data[4 + messageLength];
    }
}

