package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.IChatListener;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.TcpWriter;
import jakojaannos.mumbot.client.users.UserInfo;

public class HandlerTextMessage implements TcpMessageHandler.IHandler<Mumble.TextMessage> {

    private final MumbleClient client;

    public HandlerTextMessage(MumbleClient client) { this.client = client; }

    @Override
    public void handle(TcpWriter writer, Mumble.TextMessage textMessage) {
        System.out.println("Received a text message: '" + textMessage.getMessage() + "'");
        for (IChatListener listener : client.getChatListeners()) {
            listener.receive(new UserInfo(), textMessage.getMessage());
        }



    }
}
