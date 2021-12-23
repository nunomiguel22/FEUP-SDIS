#!/bin/sh

if [ "$#" -ne 6 ]; then
  echo "Usage: peer.sh <protocol_version> <peer_id> <service_ap> <MC> <MDB> <MDR>"
  exit 1
fi



java -classpath . distributedbackup.service.Peer "$1" "$2" "$3" "$4" "$5" "$6"