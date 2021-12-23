package distributedbackup.protocol;

import distributedbackup.filesystem.LocalChunk;
import distributedbackup.service.Peer;

import java.nio.charset.StandardCharsets;

public class RestoreInitiator implements Runnable {

    private LocalChunk chunk;
    private Peer peer;

    public RestoreInitiator(Peer peer, LocalChunk chunk){
        this.chunk = chunk;
        this.peer = peer;
    }

    @Override
    public void run(){
        StringBuilder st = new StringBuilder();

        st.append(this.peer.getProtocolVersion()).append(" ");
        st.append("GETCHUNK ");
        st.append(this.peer.getPeerID()).append(" ");
        st.append(this.chunk.getFileID()).append(" ");
        st.append(this.chunk.getChunkNo()).append(" ");
        st.append("\r\n\r\n");

        this.peer.getSocket("MC").send(st.toString().getBytes(StandardCharsets.UTF_8));
        int attempts = 0;
        while (chunk.getData() == null && attempts < 20){
            ++attempts;
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }
}
