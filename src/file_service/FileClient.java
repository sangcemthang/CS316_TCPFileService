package file_service;

import java.io.File;
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

        FileClient fileClient = new FileClient();
        int serverPort = Integer.parseInt(args[1]);
        String command;

        do {
            System.out.println("\nPlease enter a command");
            Scanner keyboard = new Scanner(System.in);
            command = keyboard.nextLine();
            SocketChannel channel = SocketChannel.open();

            switch (command) {
                case "D":
                    System.out.println("please enter the file name");
                    String fileName = keyboard.nextLine();
                    fileClient.sendCommandAndFile(channel, fileName, command, serverPort, args);

                    ByteBuffer reply = ByteBuffer.allocate(1);
                    channel.read(reply);
                    channel.close();
                    reply.flip();
                    byte[] a = new byte[1];
                    reply.get(a);
                    String code = new String(a);

                    if (code.equals("S")){
                        System.out.println("File successfully deleted.");
                    }
                    else if (code.equals("F")){
                        System.out.println("Failed to delete file.");
                    }
                    else {
                        System.out.println("An unexpected error occurred.");
                    }
                    break;
                case "L":
                    fileClient.sendCommand(channel, command, serverPort, args);

                    ByteBuffer list = ByteBuffer.allocate(1024);
                    int bytesRead;
                    while ((bytesRead = channel.read(list)) != -1) {
                        byte[] b = new byte[bytesRead];
                        list.flip();
                        list.get(b);
                        String listString = new String(b);
                        System.out.println(listString);

                        list.clear();
                    }
                    channel.close();
                    break;
                case "R": //rename
                    break;
                case "U": //upload
                    break;
                case "N": //download
                    break;
                default:
                    System.out.println("invalid command");
            }
        } while(!command.equals("Q"));
    }

    private void sendCommandAndFile(SocketChannel channel, String fileName, String command, int serverPort, String[] args) throws Exception {
        ByteBuffer request = ByteBuffer.wrap((command + fileName).getBytes());

        channel.connect(new InetSocketAddress(args[0], serverPort));
        channel.write(request);
        channel.shutdownOutput();
    }
    private void sendCommand(SocketChannel channel, String command, int serverPort, String[] args) throws Exception {
        ByteBuffer request = ByteBuffer.wrap((command).getBytes());

        channel.connect(new InetSocketAddress(args[0], serverPort));
        channel.write(request);
        channel.shutdownOutput();
    }

}

