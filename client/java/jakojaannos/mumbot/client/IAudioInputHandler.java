package jakojaannos.mumbot.client;

/**
 * Produces audio input for the client.
 */
public interface IAudioInputHandler {
    boolean canProvideAudio();

    /**
     * Returns lock object the client UDP writer can call {@link Object#wait()} on while waiting for input.
     */
    Object getLock();

    IAudioFrame popAudioFrame();
}
