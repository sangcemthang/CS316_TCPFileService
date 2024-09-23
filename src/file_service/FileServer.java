package file_service;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileServer {
    public static void main(String[] args) throws Exception{
        int port = 3000;

        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(port));

        while (true){
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = serveChannel.read(request);
            request.flip();
            //the size of the byte[] should match the number of bytes of the commands
            byte[] a = new byte[1];
            request.get(a);
            String command = new String(a);
            switch(command){
                case "D": //delete
                    byte[] b = new byte[request.remaining()];
                    request.get(b);
                    String fileName = new String(b);
                    System.out.println("file to delete:"+fileName);
                    File file = new File("server files/"+fileName);
                    boolean success = false;
                    if(file.exists()){
                        success = file.delete();
                    }
                    if(success){
                        System.out.println("file deleted successfully");
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serveChannel.write(code);
                    } else{
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
                    byte[] c = new byte[request.remaining()];
                    request.get(c);
                    String fullName = new String(c);
                    String[] parts = fullName.split(";");
                    String oldName = parts[0];
                    String newName = parts[1];

                    File oldFile = new File("server files/"+oldName);
                    File newFile = new File("server files/"+newName);

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
                    break;
                case "N": //download
                    break;
                default:
                    System.out.println("invalid command");
            }
        }
    }
}
