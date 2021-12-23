package distributedbackup.protocol;

import distributedbackup.filesystem.Chunk;
import distributedbackup.service.Peer;

import java.nio.charset.StandardCharsets;

public class BackupInitiator implements Runnable {

    private final Peer peer;
    private final byte[] message;
    private final Chunk chunk;

    public BackupInitiator(Peer peer, Chunk chunk, byte[] body){
        this.peer = peer;
        this.chunk = chunk;

        String header = peer.getProtocolVersion() + " PUTCHUNK " + peer.getPeerID() + " "
                + chunk.getFileID() + " " + chunk.getChunkNo() + " " + chunk.getReplicationDegree();
        header += "\r\n\r\n";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        message = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, message, 0, headerBytes.length);
        System.arraycopy(body, 0, message, headerBytes.length, body.length);
    }

    @Override
    public void run(){

        int delay = 500, attempts = 0;

        while (this.chunk.getPerceivedRepDegree() < this.chunk.getReplicationDegree()) {
            delay *= 2;

            if (attempts >= 5)
                break;

            peer.getSocket("MDB").send(this.message);
            System.out.println("Chunk " + this.chunk.getChunkNo() + " length: " +  this.message.length);
            try {
                Thread.sleep(delay);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }

            ++attempts;
        }
    }

}
