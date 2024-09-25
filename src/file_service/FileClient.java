package file_service;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }

        SendCommand sendCommand = new SendCommand();
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
                    String fileToDelete = keyboard.nextLine();
                    sendCommand.sendFilename(channel, fileToDelete, command, serverPort, args);

                    ByteBuffer reply = ByteBuffer.allocate(1);
                    channel.read(reply);
                    channel.close();
                    reply.flip();
                    byte[] a = new byte[1];
                    reply.get(a);
                    String code = new String(a);

                    if (code.equals("S")) {
                        System.out.println("File successfully deleted.");
                    } else if (code.equals("F")) {
                        System.out.println("Failed to delete file.");
                    } else {
                        System.out.println("An unexpected error occurred.");
                    }
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
                    ByteBuffer renameReply = ByteBuffer.allocate(1);
                    channel.read(renameReply);
                    channel.close();
                    renameReply.flip();
                    byte[] c = new byte[1];
                    renameReply.get(c);
                    String renameCode = new String(c);

                    if (renameCode.equals("S")) {
                        System.out.println("File successfully renamed");
                    } else if (renameCode.equals("F")) {
                        System.out.println("Failed to rename file.");
                    } else {
                        System.out.println("An unexpected error occurred.");
                    }
                    break;
                case "U": //upload
                    System.out.println("enter the name of the file to be uploaded");
                    String fileNameHH = keyboard.nextLine();

                    sendCommand.sendUpload(channel, command, fileNameHH, serverPort, args);

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
}

