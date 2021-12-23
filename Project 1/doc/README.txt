
To compile simple run the compile.sh without arguments.

To start the peers two scripts exist:
    Peer.sh runs as specified with <protocol_version> <peer_id> <service_ap> <MC> <MDB> <MDR>
    as arguments. The MC-MDB-MDR channels are given in as a single argument each as "addr:port"
    instead of separate arguments this is the way we had very early in the project.
    ex: ./peer.sh 1.0 3 localhost 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002


    Peer_default.sh only requires the peer id and uses protocol 1.0, localhost as the service AP,
    and 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002 as the MC, MBD and MDR channels respectively.
    Our tests were done using this script mainly.

To start the test client application run test.sh with <peer_id> BACKUP|RESTORE|DELETE|RECLAIM|STATE [<opnd_1> [<optnd_2]
as arguments.

Finally to clean a peer data folder, located under src/build/data/ run the script cleanup.sh with the
peer id as argument.

No setup.sh was needed as folders are create by the application itself.

RMI is required to run the application, which can be done by using the rmiregistry command or
running rmi.sh without arguments.


Group members:

1. Nuno Marques (up201708997@fe.up.pt)
2. Miguel Romariz (up201708809@fe.up.pt)