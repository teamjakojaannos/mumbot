package jakojaannos.mumbot.client.users;

public class UserInfo {

    private int session;
    private String name;
    private int userId;
    private int channelId;
    private boolean mute;
    private boolean deaf;
    private boolean self_mute;
    private boolean self_deaf;
    private byte[] plugin_context;
    private String plugin_identity;
    private String comment;
    private String hash;
    private byte[] comment_hash;
    private boolean priority_speaker;
    private boolean recording;

    public UserInfo() {
        session = -1;
        name = "UNKNOWN";
        userId = -1;
        channelId = 0;
        mute = false;
        deaf = false;
        self_mute = false;
        self_deaf = false;
        plugin_context = new byte[0];
        plugin_identity = "UNKNOWN";
        comment = "UNKNOWN";
        hash = "UNKNOWN";
        comment_hash = new byte[0];
        priority_speaker = false;
        recording = false;
    }

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isDeaf() {
        return deaf;
    }

    public void setDeaf(boolean deaf) {
        this.deaf = deaf;
    }

    public boolean isSelf_mute() {
        return self_mute;
    }

    public void setSelf_mute(boolean self_mute) {
        this.self_mute = self_mute;
    }

    public boolean isSelf_deaf() {
        return self_deaf;
    }

    public void setSelf_deaf(boolean self_deaf) {
        this.self_deaf = self_deaf;
    }

    public byte[] getPlugin_context() {
        return plugin_context;
    }

    public void setPlugin_context(byte[] plugin_context) {
        this.plugin_context = plugin_context;
    }

    public String getPlugin_identity() {
        return plugin_identity;
    }

    public void setPlugin_identity(String plugin_identity) {
        this.plugin_identity = plugin_identity;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public byte[] getComment_hash() {
        return comment_hash;
    }

    public void setComment_hash(byte[] comment_hash) {
        this.comment_hash = comment_hash;
    }

    public boolean isPriority_speaker() {
        return priority_speaker;
    }

    public void setPriority_speaker(boolean priority_speaker) {
        this.priority_speaker = priority_speaker;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }
}
