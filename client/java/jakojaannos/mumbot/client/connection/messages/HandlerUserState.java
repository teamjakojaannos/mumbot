package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.connection.SocketWriter;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.users.UserManager;

public class HandlerUserState implements TcpMessageHandler.Handler<Mumble.UserState> {
    private final UserManager userManager;

    public HandlerUserState(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void handle(SocketWriter writer, Mumble.UserState userState) {
        System.out.printf("Received user state: #%d (#%d) %s, %s", userState.getSession(), userState.getUserId(), userState.getName(), userState.getComment());
    }
}
