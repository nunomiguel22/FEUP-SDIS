#!/bin/sh


if [ "$#" -lt 2 ]; then
  echo "Usage: test.sh <peer_id> BACKUP|RESTORE|DELETE|RECLAIM|STATE [<opnd_1> [<optnd_2]"
  exit 1
fi

java -classpath . distributedbackup.service.TestApp "$1" "$2" "$3" "$4"