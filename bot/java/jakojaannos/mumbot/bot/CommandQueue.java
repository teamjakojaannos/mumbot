package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

public class CommandQueue extends Command {

    public CommandQueue(MumbleClient client){
        super(client);
        aliases.add("queue");
    }

    @Override
    public void execute(String args) {
        // get current songs
        String[] songs = {"Metsäbileet", "Mökille", "Kaupan", "Kautta", "Ja", "Ostetaa", "Vitusti", "Kaljaaaa"};
        String message = "";

        if(songs.length == 0){
            message = "No songs in queue.";
        } else{
            message = "Songs in queue:";

            // list up to 4 songs in queue
            for (int i = 0; i < songs.length; i++) {
                if ( i == 4){
                    // if there are more than 4 songs left in queue, display "(+ n more songs)"
                    message += "\n(+ " + (songs.length-i) + " more songs)";
                    break;
                }
                message += "\n" + songs[i];
            }
        }

        // send message to channel
        System.out.println(message);
        client.sendMessage(message);
    }

    @Override
    public String getManual() {
        return "- displays current song queue.";
    }
}
