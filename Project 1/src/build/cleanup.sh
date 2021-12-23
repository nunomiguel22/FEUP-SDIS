#!/bin/sh

if [ "$#" -ne 1 ]; then
  echo "Usage: cleanup.sh <peer_id>"
  exit 1
fi

rm -rf "data/peer_$1"