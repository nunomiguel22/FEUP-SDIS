#!/bin/sh

if [ "$#" -ne 1 ]; then
  echo "Usage: peer_default.sh <peer_id>"
  exit 1
fi



java -classpath . distributedbackup.service.Peer 1.0 "$1" localhost 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002