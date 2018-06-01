package jakojaannos.mumbot.client;

/**
 * Listener for handling audio data produced by the client.
 */
public interface IAudioOutputHandler {
    void receiveFrame(IAudioFrame audioFrame);
}
