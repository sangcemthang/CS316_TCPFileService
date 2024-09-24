package file_service;

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
}
