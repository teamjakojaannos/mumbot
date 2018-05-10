package jakojaannos.mumbot.client.channels;

import java.util.ArrayList;
import java.util.List;

public class Channel {
    private final int id;

    private String name;
    private String description;

    private List<Channel> children;
    private Channel parent;

    public boolean isRoot() {
        return parent == null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Channel getParent() {
        return parent;
    }

    public Channel getChild(int index) {
        return children.get(index);
    }

    public int getNumChannels() {
        return children.size();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParent(Channel parent) {
        this.parent = parent;
        this.parent.children.add(this);
    }

    public List<Channel> getChildren() {
        return new ArrayList<>(children);
    }

    public Channel(int id) {
        this.id = id;

        this.name = "UNKNOWN";
        this.description = "";
        this.parent = null;
        this.children = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Channel channel = (Channel) o;

        return id == channel.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
