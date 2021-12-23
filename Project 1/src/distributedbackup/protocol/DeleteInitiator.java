package distributedbackup.protocol;

import distributedbackup.service.Peer;

import java.nio.charset.StandardCharsets;

public class DeleteInitiator implements Runnable {

    private final Peer peer;
    private final String fileId;

    public DeleteInitiator(Peer peer, String fileId) {
        this.peer = peer;
        this.fileId = fileId;
    }

    @Override
    public void run() {

        StringBuilder header = new StringBuilder();
        header.append(this.peer.getProtocolVersion()).append(" ");
        header.append("DELETE ");
        header.append(this.peer.getPeerID()).append(" ");
        header.append(this.fileId).append(" ");
        header.append("\r\n\r\n");

        peer.getSocket("MC").send(header.toString().getBytes(StandardCharsets.UTF_8));
    }
}
