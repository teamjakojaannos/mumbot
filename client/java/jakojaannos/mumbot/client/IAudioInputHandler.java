package jakojaannos.mumbot.client;

/**
 * Produces audio input for the client.
 */
public interface IAudioInputHandler {
    boolean canProvideAudio();

    IAudioFrame popAudioFrame();
}
