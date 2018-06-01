package jakojaannos.mumbot.client.users;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private final List<UserInfo> users = new ArrayList<>();

    public void addUser(UserInfo user) {
        users.add(user);
    }

    public UserInfo getByName(String name) {
        for (UserInfo user : users) {
            if (user.getName().equals(name)) return user;
        }
        return null;
    }

    public UserInfo getByUserId(int id) {
        for (UserInfo user : users) {
            if (user.getUserId() == id) return user;
        }
        return null;
    }

    public UserInfo getBySession(int session) {
        for (UserInfo user : users) {
            if (user.getSession() == session) return user;
        }
        return null;
    }


    public void removeBySession(int session) {
        int index = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getSession() == session) {
                index = i;
                break;
            }
        }

        users.remove(index);
    }
}
