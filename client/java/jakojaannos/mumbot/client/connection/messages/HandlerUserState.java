package jakojaannos.mumbot.client.connection.messages;

import MumbleProto.Mumble;
import jakojaannos.mumbot.client.MumbleClient;
import jakojaannos.mumbot.client.connection.TcpMessageHandler;
import jakojaannos.mumbot.client.connection.TcpWriter;
import jakojaannos.mumbot.client.users.UserInfo;
import jakojaannos.mumbot.client.users.UserManager;

public class HandlerUserState implements TcpMessageHandler.IHandler<Mumble.UserState> {
    private final UserManager userManager;
    private final MumbleClient client;

    public HandlerUserState(UserManager userManager, MumbleClient client) {
        this.userManager = userManager;
        this.client = client;
    }

    @Override
    public void handle(TcpWriter writer, Mumble.UserState userState) {
        // System.out.printf("Received user state: #%d (#%d) %s, %s\n", userState.getSession(), userState.getUserId(), userState.getName(), userState.getComment());
        System.out.printf("Received user state: s = %d, id = %d, name = %s\n", userState.getSession(), userState.getUserId(), userState.getName());

        UserInfo user = userManager.getBySession(userState.getSession());

        if (user == null) {
            user = new UserInfo();
            userManager.addUser(user);
        }

        if (userState.hasSession()) user.setSession(userState.getSession());
        if (userState.hasName()) user.setName(userState.getName());
        if (userState.hasUserId()) user.setUserId(userState.getUserId());
        if (userState.hasChannelId()) user.setChannelId(userState.getChannelId());
        if (userState.hasMute()) user.setMute(userState.getMute());
        if (userState.hasDeaf()) user.setDeaf(userState.getDeaf());
        if (userState.hasSelfMute()) user.setSelf_mute(userState.getSelfMute());
        if (userState.hasSelfDeaf()) user.setSelf_deaf(userState.getSelfDeaf());
        if (userState.hasPluginContext()) user.setPlugin_context(userState.getPluginContext().toByteArray());
        if (userState.hasPluginIdentity()) user.setPlugin_identity(userState.getPluginIdentity());
        if (userState.hasComment()) user.setComment(userState.getComment());
        if (userState.hasHash()) user.setHash(userState.getHash());
        if (userState.hasCommentHash()) user.setComment_hash(userState.getCommentHash().toByteArray());
        if (userState.hasPrioritySpeaker()) user.setPriority_speaker(userState.getPrioritySpeaker());
        if (userState.hasRecording()) user.setRecording(userState.getRecording());

        if (userState.getSession() == client.getSession()) client.updateChannel();
    }
}
