package distributedbackup.protocol;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Message {

    private final String protocolVersion;
    private final String messageType;
    private final int senderID;
    private final String fileID;
    private final int chunkNo;
    private final int replicationDegree;
    private byte[] body;

    public Message(DatagramPacket packet) {
        String message = new String(packet.getData());
        message = message.substring(0, Math.min(packet.getLength(), message.length()));
        System.out.println("string length=" + message.length() + " bytes length:" + message.getBytes(StandardCharsets.UTF_8).length);

        String[] headerAndBody = message.split("\r\n\r\n", 2);
        String[] headerFields = headerAndBody[0].split(" ");

        this.protocolVersion = headerFields[0];
        this.messageType = headerFields[1];
        this.senderID = Integer.parseInt(headerFields[2]);
        this.fileID = headerFields[3];
        this.chunkNo = (headerFields.length >= 5) ? Integer.parseInt(headerFields[4]) : -1;
        this.replicationDegree = (headerFields.length >= 6) ? Integer.parseInt(headerFields[5]) : -1;

        if (headerAndBody.length == 2) {
            int headerSize = headerAndBody[0].getBytes(StandardCharsets.UTF_8).length;
            this.body = Arrays.copyOfRange(packet.getData(), headerSize, packet.getLength());
        }
    }
    public Message (String[] header, byte[] body){
        this.protocolVersion = header[0];
        this.messageType = header[1];
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];
        this.chunkNo = (header.length >= 5) ? Integer.parseInt(header[4]) : -1;
        this.replicationDegree = (header.length >= 6) ? Integer.parseInt(header[5]) : -1;
        this.body = body;
    }

    public Message (String[] header){
        this.protocolVersion = header[0];
        this.messageType = header[1];
        this.senderID = Integer.parseInt(header[2]);
        this.fileID = header[3];
        this.chunkNo = (header.length >= 5) ? Integer.parseInt(header[4]) : -1;
        this.replicationDegree = (header.length >= 6) ? Integer.parseInt(header[5]) : -1;
        this.body = null;
    }

    public byte[] getBody() {
        return body;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public int getSenderID() {
        return senderID;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getFileID() {
        return fileID;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String toString() {

        return "Message header:" +
                " protocolVersion='" + this.getProtocolVersion() + '\'' +
                " messageType='" + this.getMessageType() + '\'' +
                " senderId=" + this.senderID +
                " fileID='" + this.fileID + '\'' +
                " chunkNo=" + this.chunkNo +
                " replicationDegree=" + this.getReplicationDegree() +
                " body length: " + this.getBody().length;
    }

}
