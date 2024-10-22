package jakojaannos.mumbot.client.util.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class Markers {
    public static final Marker CLIENT = MarkerFactory.getMarker("CLIENT");
    public static final Marker CONNECTION = MarkerFactory.getMarker("CONNECTION");
    public static final Marker USERS = MarkerFactory.getMarker("USERS");
    public static final Marker CHANNELS = MarkerFactory.getMarker("CHANNELS");
    public static final Marker TCP = MarkerFactory.getMarker("TCP");
    public static final Marker UDP = MarkerFactory.getMarker("UDP");
    public static final Marker CRYPTO = MarkerFactory.getMarker("CRYPTO");
    public static final Marker UDP_TUNNEL = MarkerFactory.getMarker("UDP_TUNNEL");
    public static final Marker PERMISSION_DENIED = MarkerFactory.getMarker("PERMISSION_DENIED");
    public static final Marker AUDIO = MarkerFactory.getMarker("AUDIO");
}
