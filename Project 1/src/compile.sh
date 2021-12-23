#!/bin/sh

clear
rm -rf build/distributedbackup
mkdir build/distributedbackup

javac -cp .:./build/:./ -d build distributedbackup/*/*.java
echo "Source code compiled"