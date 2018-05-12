package jakojaannos.mumbot.client.connection;

/**
 * Relays handling of packets received over UDP to appropriate handlers and runs message handling loop task.
 */
public class UdpMessageHandler {
    public void handle(UdpMessage msg) {
        switch (msg.getType()) {
            case 0:
                System.out.println("Received CELT audio packet!");
                break;
            case 1:
                System.out.println("Received Ping packet!");
                break;
            case 2:
                System.out.println("Received Speex audio packet!");
                break;
            case 3:
                System.out.println("Received CELT Beta audio packet");
                break;
            case 4:
                System.out.println("Received OPUS audio packet");
                break;
            default:
                System.out.println("Received undefined audio packet type!");
                break;
        }
    }
}
