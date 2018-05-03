package jakojaannos.mumbot.ytapi;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;

public class YoutubeDownloader {

    public static final String OUTPUT_FOLDER = "mumbot_downloads";

    public static void download(String videoUrl, String outputName) {
        // Video url to download
        //String videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        //YoutubeDL.setExecutablePath("C:/Users/Lassi_2/IdeaProjects/mumbot/youtube-dl.exe");


        // Build request
        YoutubeDLRequest request = new YoutubeDLRequest(videoUrl);
        //request.setOption("ignore-errors");		// --ignore-errors
        //request.setOption("output", "audio_test_ba.mp4");
        request.setOption("output", "audio_test_ba.mp4");
        request.setOption("retries", 10);
        request.setOption("extract-audio");
        request.setOption("audio-format", "mp3");
        request.setOption("format", "bestaudio");

        // Make request and return response
        try {
            YoutubeDLResponse response = YoutubeDL.execute(request);

            String stdOut = response.getOut();
            System.out.println("Response: " + stdOut);
        } catch (YoutubeDLException e) {
            System.err.println("Something went wrong.");
            e.printStackTrace();
        }
    }

}
