package file_service;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        String command;
        do{
            System.out.println("\nPlease enter a command.");
            Scanner keyboard = new Scanner(System.in);
            command = keyboard.nextLine();
            switch(command.toUpperCase()){
                case "D": //delete
                    System.out.println("please enter the file name:");
                    String fileName = keyboard.nextLine();
                    ByteBuffer request = ByteBuffer.wrap(
                            (command+fileName).getBytes());
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(request);
                    channel.shutdownOutput();
                    //TODO: receive the status code and tell the user
                    break;
                case "L": //list
                    break;
                case "R": //rename
                    break;
                case "U": //upload
                    break;
                case "N": //download
                    break;
                default:
                    if(!command.equals("Q"));
                    System.out.println("invalid command");
            }
        }while(!command.equals("Q"));
    }
}
