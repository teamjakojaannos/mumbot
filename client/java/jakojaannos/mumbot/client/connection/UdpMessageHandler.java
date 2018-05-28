package jakojaannos.mumbot.client.connection;

import jakojaannos.mumbot.client.MumbleClient;

/**
 * Relays handling of packets received over UDP to appropriate handlers and runs message handling loop task.
 */
public class UdpMessageHandler {
    private final MumbleClient client;

    public UdpMessageHandler(MumbleClient client) {
        this.client = client;
    }

    public void handle(UdpMessage msg) {
        switch (msg.getType()) {
            case 0:
                System.out.println("Received CELT audio packet!");
                break;
            case 1:
                System.out.println("Received Ping packet!");
                client.setCryptValid(true); // TODO: Validate the packet first :D
                break;
            case 2:
                System.out.println("Received Speex audio packet!");
                break;
            case 3:
                System.out.println("Received CELT Beta audio packet");
                break;
            case 4:
                //System.out.println("Received OPUS audio packet");
                break;
            default:
                System.out.printf("Received undefined audio packet type: %d\n", msg.getType());
                break;
        }
    }
}
