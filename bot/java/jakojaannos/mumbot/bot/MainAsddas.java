package jakojaannos.mumbot.bot;

import jakojaannos.mumbot.client.MumbleClient;

import java.util.Scanner;

public class MainAsddas {

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);

        MessageParser mp = new MessageParser(new MumbleClient());
        String s = "";

        boolean running = true;
        while (running) {
            s = scanner.nextLine();
            if(s.equals("exit")|| s.equals("stop")){
                running = false;
                break;
            }

            mp.receive(null, s);
        }


    }
}
