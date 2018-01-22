package jakojaannos.mumbot.client;

import jakojaannos.mumbot.client.users.UserInfo;

public interface IChatListener {
    void receive(UserInfo userInfo, String message);
}
