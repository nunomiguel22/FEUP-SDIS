package distributedbackup.service;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import distributedbackup.filesystem.LocalFile;
import distributedbackup.filesystem.LocalFileInfo;
import distributedbackup.filesystem.State;
import distributedbackup.multicast.CustomMulticastSocket;
import distributedbackup.protocol.DeleteInitiator;


public class Peer implements InitiatorPeer {

    private final HashMap<String, CustomMulticastSocket> MCSockets;
    public ScheduledExecutorService threadPool;
    private final int peerID;
    private final String protocolVersion;
    public State state;


    public Peer(String protocolVersion, int peerID, String AP, String MC, String MDR, String MBR) throws IOException {
        this.peerID = peerID;
        this.protocolVersion = protocolVersion;
        this.threadPool = new ScheduledThreadPoolExecutor(32);

        this.MCSockets = new HashMap<>();

        String[] MCAddress = MC.split(":");
        String[] MDBAddress = MDR.split(":");
        String[] MDRAddress = MBR.split(":");

        CustomMulticastSocket MCSocket = new CustomMulticastSocket(this, MCAddress[0], Integer.parseInt(MCAddress[1]));
        CustomMulticastSocket MDBSocket = new CustomMulticastSocket(this, MDBAddress[0], Integer.parseInt(MDBAddress[1]));
        CustomMulticastSocket MDRSocket = new CustomMulticastSocket(this, MDRAddress[0], Integer.parseInt(MDRAddress[1]));

        MCSockets.put("MC", MCSocket);
        MCSockets.put("MDB", MDBSocket);
        MCSockets.put("MDR", MDRSocket);

        new Thread(MCSocket).start();
        new Thread(MDBSocket).start();
        new Thread(MDRSocket).start();

        if (State.hasExistingDatabase(this.peerID))
            this.state = State.load(this.peerID);
        else this.state = new State(this.peerID);

        System.out.println("Peer ready");
    }

    public static void main(String[] args) throws IOException{
        if (args.length < 6) {
            System.out.println("Usage: <protocol_version> <peer_ID> <service_access_point> <MC_IP> <MC_Port> <MD_BIP> <MDB_Port> <MDR_IP> <MDR_Port>");
            System.exit(-1);
        }

        int peerID = Integer.parseInt(args[1]);
        Peer peer = new Peer(args[0], peerID, args[2], args[3], args[4], args[5]);

        InitiatorPeer initiatorPeer = (InitiatorPeer) UnicastRemoteObject.exportObject(peer, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(args[1], initiatorPeer);

    }

    public HashMap<String, CustomMulticastSocket> getMCSockets() {
        return MCSockets;
    }

    @Override
    public void backup(String pathname, int replicationDegree) {
        LocalFile file = new LocalFile(pathname, replicationDegree, this);
        file.backup();
    }

    @Override
    public void restore(String pathname) {
        LocalFileInfo fileInfo = this.state.getLocalFileByPathname(pathname);
        LocalFile file = new LocalFile(fileInfo, this);
        file.restore();
    }

    @Override
    public void delete(String pathname) {
        LocalFileInfo file = this.state.getLocalFileByPathname(pathname);
        if (file == null) {
            System.out.println("This file doesnt belong to this peer!");
            return;
        }
        file.clearChunks();
        this.state.removeLocalFile(file);

        this.threadPool.execute(new DeleteInitiator(this, file.getFileID()));

        System.out.println("Removed file: " + pathname);
        this.state.save();
    }

    @Override
    public void reclaim(int maxDiskSpace) {
        this.state.reclaim(maxDiskSpace, this);
    }

    @Override
    public void getState(){
        System.out.println("---------------------------------");
        System.out.println("LOCAL FILES BACKED UP");
        this.state.printLocalFiles();
        System.out.println("---------------------------------");
        System.out.println("CHUNKS STORED LOCALLY");
        this.state.printStoredChunks();
        this.state.printSpace();
        System.out.println("---------------------------------");
    }


    public int getPeerID() {
        return peerID;
    }

    public CustomMulticastSocket getSocket(String name){
        return this.MCSockets.get(name);
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }
}
