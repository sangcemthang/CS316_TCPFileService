package file_service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileClient {
    public static FileClient fileClient = new FileClient();
    public static SendCommand sendCommand = new SendCommand();
    public static Scanner keyboard = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }


        int serverPort = Integer.parseInt(args[1]);
        String command;
        ExecutorService es = Executors.newFixedThreadPool(1);

        do {
            System.out.println("\nPlease enter a command, type Q to quit");
            command = keyboard.nextLine();
            SocketChannel channel = SocketChannel.open();

            switch (command) {
                case "D":
                    System.out.println("please enter the file name");
                    String fileToDelete = keyboard.nextLine();
                    sendCommand.sendFilename(channel, fileToDelete, command, serverPort, args);

                    fileClient.getConfirmation(channel);
                    break;

                case "L":
                    sendCommand.send(channel, command, serverPort, args);

                    ByteBuffer list = ByteBuffer.allocate(1024);
                    int bytesRead;
                    while ((bytesRead = channel.read(list)) != -1) {
                        byte[] b = new byte[bytesRead];
                        list.flip();
                        list.get(b);
                        String listString = new String(b);
                        System.out.println(listString);//.substring(13, listString.length()-1));

                        list.clear();
                    }
                    channel.close();
                    break;

                case "R": //rename
                    System.out.println("please enter the file name to be renamed");
                    String oldName = keyboard.nextLine();
                    System.out.println("please enter the new name");
                    String newName = keyboard.nextLine();

                    sendCommand.sendRename(channel, oldName, newName, command, serverPort, args);
                    fileClient.getConfirmation(channel);
                    break;
                case "U": //upload
                    es.submit(new Upload(channel, command, serverPort, args));

                    break;

                case "N": //download
                    System.out.println("what is the name of the file you want to download?");
                    String fileDownload = keyboard.nextLine();

                    sendCommand.sendFilename(channel, fileDownload, command, serverPort, args);

                    FileOutputStream fos = new FileOutputStream(fileDownload);

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesReadDownload;

                    while((bytesReadDownload = channel.read(buffer)) != -1){
                        byte[] downloadByte = new byte[bytesReadDownload];
                        buffer.flip();
                        buffer.get(downloadByte);
                        fos.write(downloadByte);
                        buffer.clear();
                    }

                    fos.close();

                    break;

                default:
                    System.out.println("invalid command");
            }
        } while (!command.equals("Q"));
    }

    public static class Upload implements Runnable{
        SocketChannel channel;
        String command;
        int serverPort;
        String[] args;

        public Upload(SocketChannel channel, String command, int serverPort, String[] args){
            this.channel = channel;
            this.command = command;
            this.serverPort = serverPort;
            this.args = args;
        }

        public void run() {
            System.out.println("enter the name of the file to be uploaded");
            String uploadFileName = ("server files/" + keyboard.nextLine());
            try {
                sendCommand.sendUpload(channel, command, uploadFileName, serverPort, args);
                fileClient.getConfirmation(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getConfirmation(SocketChannel channel) throws IOException {
        ByteBuffer reply = ByteBuffer.allocate(1);
        channel.read(reply);
        channel.close();
        reply.flip();
        byte[] a = new byte[1];
        reply.get(a);
        String code = new String(a);

        if (code.equals("S")) {
            System.out.println("success");
        } else if (code.equals("F")) {
            System.out.println("failure");
        } else {
            System.out.println("An unexpected error occurred.");
        }
    }
}

