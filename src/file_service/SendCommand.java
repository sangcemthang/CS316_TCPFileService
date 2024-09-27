package file_service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SendCommand {
    void sendFilename(SocketChannel channel, String fileName, String command, int serverPort, String[] args) throws Exception {
        ByteBuffer request = ByteBuffer.wrap((command + fileName).getBytes());

        channel.connect(new InetSocketAddress(args[0], serverPort));
        channel.write(request);
        channel.shutdownOutput();
    }
    void send (SocketChannel channel, String command, int serverPort, String[] args) throws Exception {
        ByteBuffer request = ByteBuffer.wrap((command).getBytes());

        channel.connect(new InetSocketAddress(args[0], serverPort));
        channel.write(request);
        channel.shutdownOutput();
    }
    void sendRename(SocketChannel channel, String oldName, String newName, String command, int serverPort, String[] args) throws Exception {
        ByteBuffer request = ByteBuffer.wrap((command + oldName + ";" + newName).getBytes());

        channel.connect(new InetSocketAddress(args[0], serverPort));
        channel.write(request);
        channel.shutdownOutput();
    }

    void sendUpload(SocketChannel channel, String command, String fileName, int serverPort, String[] args) throws Exception {
        channel.connect(new InetSocketAddress(args[0], serverPort));
        File file = new File(fileName);

        try (FileInputStream fis = new FileInputStream(file)){
            byte[] fileData = new byte[1024];
            int bytesRead;
            ByteBuffer request = ByteBuffer.wrap((command + fileName + ";").getBytes());
            channel.write(request);

            while ((bytesRead = fis.read(fileData)) != -1){
                ByteBuffer request2 = ByteBuffer.wrap(fileData, 0, bytesRead);
                channel.write(request2);
            }
            fis.close();
            channel.shutdownOutput();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
