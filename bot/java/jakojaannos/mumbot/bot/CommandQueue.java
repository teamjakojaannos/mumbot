package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandQueue extends Command {

    public CommandQueue(MumbleClient client){
        super(client);
    }

    @Override
    public void doExecute(String args) {
        // get current songs
        String[] songs = {"Metsäbileet", "Mökille", "Kaupan", "Kautta", "Ja", "Ostetaa", "Vitusti", "Kaljaaaa"};
        String message = "";

        if(songs.length == 0){
            message = "No songs in queue.";
        } else{
            message = "Songs in queue:";

            for (int i = 0; i < songs.length; i++) {
                if ( i == 4){
                    message += "\n(+ " + (songs.length-i) + " more songs)";
                    break;
                }
                message += "\n" + songs[i];
            }
        }

        // send message(s) to channel
        System.out.println(message);
        client.sendMessage(message);
    }

    @Override
    public String getManual() {
        return "- displays current song queue";
    }
}
