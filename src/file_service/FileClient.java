package file_service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }

        FileClient fileClient = new FileClient();
        SendCommand sendCommand = new SendCommand();
        int serverPort = Integer.parseInt(args[1]);
        String command;

        do {
            System.out.println("\nPlease enter a command, type Q to quit");
            Scanner keyboard = new Scanner(System.in);
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
                        System.out.println(listString);

                        list.clear();
                    }
                    channel.close();
                    break;

                case "R":
                    System.out.println("please enter the file name to be renamed");
                    String oldName = keyboard.nextLine();
                    System.out.println("please enter the new name");
                    String newName = keyboard.nextLine();

                    sendCommand.sendRename(channel, oldName, newName, command, serverPort, args);
                    fileClient.getConfirmation(channel);
                    break;

                case "U":
                    System.out.println("Enter the name of the file to be uploaded");
                    String uploadFileName = "server files/" + keyboard.nextLine();

                    String finalCommand1 = command;
                    new Thread(() -> {
                        try {
                            sendCommand.sendUpload(channel, finalCommand1, uploadFileName, serverPort, args);
                            fileClient.getConfirmation(channel);
                        } catch (Exception e) {
                            System.err.println("Error during upload: " + e.getMessage());
                        }
                    }).start();
                    break;

                case "N":
                    System.out.println("What is the name of the file you want to download?");
                    String fileDownload = keyboard.nextLine();

                    String finalCommand = command;
                    new Thread(() -> {
                        try {
                            sendCommand.sendFilename(channel, fileDownload, finalCommand, serverPort, args);

                            try (FileOutputStream fos = new FileOutputStream(fileDownload)) {
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                int bytesReadDownload;

                                while ((bytesReadDownload = channel.read(buffer)) != -1) {
                                    byte[] downloadByte = new byte[bytesReadDownload];
                                    buffer.flip();
                                    buffer.get(downloadByte);
                                    fos.write(downloadByte);
                                    buffer.clear();
                                }
                            }
                            System.out.println("File downloaded successfully: " + fileDownload);
                        } catch (Exception e) {
                            System.err.println("Error during download: " + e.getMessage());
                        }
                    }).start();
                    break;

                default:
                    System.out.println("Invalid command");
            }
        } while (!command.equals("Q"));
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
            System.out.println("Success");
        } else if (code.equals("F")) {
            System.out.println("Failure");
        } else {
            System.out.println("An unexpected error occurred.");
        }
    }
}


