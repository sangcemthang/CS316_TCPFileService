package file_service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileServer {
    public static void main(String[] args) throws Exception {
        int port = 3000;
        FileServer fileServer = new FileServer();

        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(port));

        while (true) {
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = serveChannel.read(request);

            request.flip();
            //the size of the byte[] should match the number of bytes of the commands
            byte[] commandByte = new byte[1];
            request.get(commandByte);
            String command = new String(commandByte);

            switch (command) {
                case "D": //delete
                    String fileNameToDelete = fileServer.getRemainingString(request);
                    System.out.println("file to delete:" + fileNameToDelete);
                    File file = new File("server files/" + fileNameToDelete);
                    boolean success = false;

                    if (file.exists()) {
                        success = file.delete();
                    }
                    if (success) {
                        System.out.println("file deleted successfully");
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serveChannel.write(code);
                    } else {
                        System.out.println("unable to delete file");
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serveChannel.write(code);
                    }
                    serveChannel.close();
                    break;

                case "L": //list
                    File dir = new File("server files");
                    File[] fileList = (dir.listFiles());

                    assert fileList != null;
                    int listLength = fileList.length;
                    //Checks length of files in directory;

                    for (int i = 0; i <= listLength - 1; i++) {
                        ByteBuffer list = ByteBuffer.wrap((fileList[i] + "\n").getBytes());
                        serveChannel.write(list);
                    }
                    serveChannel.close();
                    break;

                case "R": //rename
                    String nameAndRename = fileServer.getRemainingString(request);
                    String[] parts = nameAndRename.split(";");
                    String oldName = parts[0];
                    String newName = parts[1];

                    File oldFile = new File("server files/" + oldName);
                    File newFile = new File("server files/" + newName);

                    //renames oldFile to newFile
                    if (oldFile.renameTo(newFile)) {
                        System.out.println("File renamed successfully");
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serveChannel.write(code);
                    } else {
                        System.out.println("failed to rename file");
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serveChannel.write(code);
                    }
                    serveChannel.close();
                    break;

                case "U": //upload
                    String fileToUpload = fileServer.getRemainingString(request);
                    String[] parts2 = fileToUpload.split(";", 2);
                    FileOutputStream fos = new FileOutputStream(parts2[0]);
                    fos.write(parts2[1].getBytes());

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int uploadBytesRead;
                    while ((uploadBytesRead = serveChannel.read(buffer)) != -1) {
                        byte[] uploadBytes = new byte[uploadBytesRead];
                        buffer.flip();
                        buffer.get(uploadBytes);
                        fos.write(uploadBytes);
                        buffer.clear();
                    }

                    File checkUpload = new File(parts2[0]);

                    fos.close();

                    if (checkUpload.exists()) {
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serveChannel.write(code);
                    }
                    else {
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serveChannel.write(code);
                    }
                    serveChannel.close();
                    break;

                case "N": //download
                    String fileToDownload = fileServer.getRemainingString(request);

                    System.out.println("file to download: " + fileToDownload); //delete later
                    File newDownload = new File(fileToDownload);

                    FileInputStream fis = new FileInputStream(newDownload);
                    byte[] downloadArray = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = fis.read(downloadArray)) != -1) {
                        ByteBuffer downloadRequest = ByteBuffer.wrap(downloadArray, 0, bytesRead);
                        serveChannel.write(downloadRequest);
                    }
                    fis.close();
                    serveChannel.shutdownOutput();

                    break;

                default:
                    System.out.println("invalid command");
            }
        }
    }
    private String getRemainingString(ByteBuffer request){
        byte[] bytes = new byte[request.remaining()];
        request.get(bytes);

        return new String(bytes);
    }
}