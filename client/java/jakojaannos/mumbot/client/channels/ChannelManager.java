package jakojaannos.mumbot.client.channels;

import java.util.*;

public class ChannelManager {
    private final Map<Integer, Channel> channels = new HashMap<>();

    public ChannelManager(){
    }

    public void addChannel(Channel channel){
        channels.put(channel.getId(), channel);
    }

    public Channel getByName(String name){
        return channels.values().stream().filter(channel -> channel.getName().equals(name)).findFirst().orElse(null);
    }

    public Channel getByPath(String path){
        Channel channel = channels.get(0);

        String[] split = path.split("/");
        outerloop : for (String s: split) {

            for (Channel c: channel.getChildren()) {
                if (c.getName().equals(s)){
                    channel = c;
                    continue outerloop;
                }
            }
            // did not find correct child channel
            return null;
        }

        return channel;
    }

    public Channel getById(int id){
        return channels.get(id);
    }

}
