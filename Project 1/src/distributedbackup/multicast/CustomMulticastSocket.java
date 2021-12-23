package distributedbackup.multicast;

import java.io.IOException;
import java.net.*;


import distributedbackup.protocol.Dispatcher;
import distributedbackup.protocol.Message;
import distributedbackup.service.Peer;


public class CustomMulticastSocket extends MulticastSocket implements Runnable {
    private static final int MAX_MESSAGE_SIZE = 65000;
    private final Peer peer;
    private final InetAddress address;
    private final int port;

    public CustomMulticastSocket(Peer peer, String address, int port) throws IOException {
        super(port);
        this.peer = peer;
        this.address = InetAddress.getByName(address);
        this.port = port;
        this.setTimeToLive(1);
        this.joinGroup(this.address);
    }

    @Override
    public void run() {

        byte[] rbuf = new byte[MAX_MESSAGE_SIZE];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

        while(true){
            try {
                this.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Message msg = new Message(packet);
            if (msg.getSenderID() != this.peer.getPeerID())
                 this.peer.threadPool.execute(new Dispatcher(this.peer, msg));
        }
    }

    public boolean send(byte[] data) {
        try {
            DatagramPacket outPacket = new DatagramPacket(data, data.length, this.address, getLocalPort()); // create the packet to send through the socket
            send(outPacket);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
