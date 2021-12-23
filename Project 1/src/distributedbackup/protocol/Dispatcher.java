package distributedbackup.protocol;


import distributedbackup.filesystem.Chunk;
import distributedbackup.filesystem.LocalChunk;
import distributedbackup.filesystem.StoredChunk;
import distributedbackup.service.Peer;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.Random;

public class Dispatcher implements Runnable {

    private final Peer peer;
    private final Message msg;

    public Dispatcher(Peer peer, Message msg){
        this.peer = peer;
        this.msg = msg;
    }

    @Override
    public void run(){
        System.out.println(this.msg);

        switch(this.msg.getMessageType()){
            case "PUTCHUNK":{
                this.putChunkHandler();
                break;
            }
            case "STORED":{
                this.storedHandler();
                break;
            }
            case "DELETE":{
                this.deleteHandler();
                break;
            }
            case "CHUNK":{
                this.chunkHandler();
                break;
            }
            case "GETCHUNK":{
                this.getChunkHandler();
                break;
            }
            case "REMOVED":{
                this.removedHandler();
                break;
            }
            default:{
                System.out.println("Unknown message type");
                break;
            }
        }
    }

    public void storedHandler(){
        Chunk chunk = new Chunk(this.msg);

        if (this.peer.state.hasStoredChunk(chunk)) {
            this.peer.state.incPerceivedRepDegree(chunk);
            this.peer.state.save();
        }
        else if (this.peer.state.hasLocalFile(chunk.getFileID())){
            this.peer.state.getLocalChunk(chunk).incPerceivedRepDegree();
            this.peer.state.save();
        }
    }

    public void chunkHandler(){

        StoredChunk storedChunk = this.peer.state.getStoredChunk(this.msg);
        if (storedChunk != null){

            storedChunk.gotAnswer = true;
        }
        else{
            LocalChunk localChunk = this.peer.state.getLocalChunk(this.msg.getFileID(), this.msg.getChunkNo());
            if (localChunk != null){
                localChunk.gotAnswer = true;
                localChunk.setData(this.msg.getBody());
            }
        }
    }

    public void getChunkHandler(){

        StoredChunk storedChunk = this.peer.state.getStoredChunk(this.msg);

        if (storedChunk == null){
            System.out.println("Peer_" + this.peer.getPeerID() + "does not have requested chunk");
            return;
        }

        if (storedChunk.inProcess)
            return;

        storedChunk.inProcess = true;
        storedChunk.gotAnswer = false;


        //Wait for between 0-400ms
        try {
            Thread.sleep(new Random().nextInt(400));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (!storedChunk.gotAnswer){
            StringBuilder text = new StringBuilder();
            text.append(this.peer.getProtocolVersion()).append(" ");
            text.append("CHUNK ");
            text.append(this.peer.getPeerID()).append(" ");
            text.append(this.msg.getFileID()).append(" ");
            text.append(this.msg.getChunkNo()).append(" ");
            text.append("\r\n\r\n");
            byte[] header = text.toString().getBytes(StandardCharsets.UTF_8);
            int headerLength = header.length;
            int bodyLength = storedChunk.getData().length;
            byte[] chunkMsg = new byte[headerLength + bodyLength];
            System.arraycopy(header, 0, chunkMsg, 0, headerLength);
            System.arraycopy(storedChunk.getData(), 0, chunkMsg,headerLength, bodyLength);
            this.peer.getSocket("MDR").send(chunkMsg);
        }
        else {
            System.out.println("Chunk already sent by another peer, disregarding");
        }
        storedChunk.setData(null);
        storedChunk.inProcess = false;
    }

    public void removedHandler(){
        StoredChunk storedChunk = this.peer.state.getStoredChunk(this.msg);
        if (storedChunk == null)
            return;

        storedChunk.decPerceivedRepDegree();
        if (storedChunk.getPerceivedRepDegree() < storedChunk.getReplicationDegree()){
            storedChunk.gotAnswer = false;

            try {
                Thread.sleep(new Random().nextInt(400));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (!storedChunk.gotAnswer) {
                byte[] chunkBody = this.peer.state.getStoredChunkBody(storedChunk);
                this.peer.threadPool.execute(new BackupInitiator(this.peer, storedChunk, chunkBody));
            }
        }
    }

    public void deleteHandler(){
        String fileID = this.msg.getFileID();

        Collection<StoredChunk> chunks = this.peer.state.getStoredChunks().values();

        for(StoredChunk chunk : chunks){
            System.out.println(chunk.getFileID());
            if(chunk.getFileID().equals(fileID)){
                this.peer.state.deleteStoredChunk(chunk);
            }
        }

        String pathString = this.peer.state.getPeerFileSystemFolder()+ "/" + fileID;
        Path path = Paths.get(pathString);
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception e){
            System.out.println("Failed to delete file/chunks, reason: " + e.getMessage());
        }
        this.peer.state.save();
    }

    public void putChunkHandler(){
        StoredChunk chunk = new StoredChunk(this.msg);

        // PutChunk of own file chunk
        if (this.peer.state.hasLocalFile(this.msg.getFileID()))
            return;

        if (!this.peer.state.hasStoredChunk(chunk)){
            chunk.gotAnswer = true;
            if (this.peer.state.getRemainingSpace() > chunk.size()) {
                if (chunk.getPerceivedRepDegree() < chunk.getReplicationDegree()) {
                    this.peer.state.saveStoredChunk(chunk);
                     try {
                        Thread.sleep(new Random().nextInt(400));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    chunk.incPerceivedRepDegree();
                    this.sendStoredMessage(chunk);
                    this.peer.state.save();
                }
            }
        }
    }

    public void sendStoredMessage(StoredChunk chunk){
        String header = peer.getProtocolVersion() + " STORED " + peer.getPeerID() + " " + chunk.getFileID() +
                " " + chunk.getChunkNo();
        header += "\r\n\r\n";

        peer.getSocket("MC").send(header.getBytes(StandardCharsets.UTF_8));

    }

}
