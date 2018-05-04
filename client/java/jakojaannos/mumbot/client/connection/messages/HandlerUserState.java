package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.connection.TcpWriter;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;

public class HandlerUserState implements TcpMessageHandler.IHandler<Mumble.UserState> {
    private final UserManager userManager;

    public HandlerUserState(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void handle(TcpWriter writer, Mumble.UserState userState) {
        System.out.printf("Received user state: #%d (#%d) %s, %s\n", userState.getSession(), userState.getUserId(), userState.getName(), userState.getComment());

        UserInfo user = new UserInfo();
        user.setSession(userState.getSession());

        user.setName(userState.getName());
        user.setUserId(userState.getUserId());
        user.setChannelId(userState.getChannelId());
        user.setMute(userState.getMute());
        user.setDeaf(userState.getDeaf());
        user.setSelf_mute(userState.getSelfMute());
        user.setSelf_deaf(userState.getSelfDeaf());
        user.setPlugin_context(userState.getPluginContext().toByteArray());
        user.setPlugin_identity(userState.getPluginIdentity());
        user.setComment(userState.getComment());
        user.setHash(userState.getHash());
        user.setComment_hash(userState.getCommentHash().toByteArray());
        user.setPriority_speaker(userState.getPrioritySpeaker());
        user.setRecording(userState.getRecording());

    }
}
