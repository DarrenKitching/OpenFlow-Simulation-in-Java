import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public interface PacketContent {
	public String toString();
	public DatagramPacket toDatagramPacket(String message, byte task, byte number, byte acknowledgment, InetSocketAddress dstAddress);
}
