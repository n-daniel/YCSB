#!/bin/bash

# Make sure only root can run our script
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

#####################################à####
# AEROSPIKE
##########################################

/etc/init.d/citrusleaf start
sleep 10

export THREADS=1
./runtest.sh aerospike

export THREADS=100
./runtest.sh aerospike

export THREADS=1000
./runtest.sh aerospike

/etc/init.d/citrusleaf stop
sleep 10

#####################################à####
# REDIS
##########################################
export THREADS=1
./runtest.sh redis -p redis.host=localhost

export THREADS=100
./runtest.sh redis -p redis.host=localhost

export THREADS=1000
./runtest.sh redis -p redis.host=localhost

#####################################à####
# MONGODB
##########################################

service mongodb start
sleep 3

export THREADS=1
./runtest.sh mongodb
./cleandb_mongodb.sh

export THREADS=100
./runtest.sh mongodb
./cleandb_mongodb.sh

export THREADS=1000
./runtest.sh mongodb
./cleandb_mongodb.sh

service mongodb stop
sleep 3

#####################################à####
# HBASE
##########################################
export THREADS=1
./runtest.sh hbase

export THREADS=100
./runtest.sh hbase

export THREADS=1000
./runtest.sh hbase

#####################################à####
# ORION
##########################################
export THREADS=1
./runtest.sh orion -p hosts=orion:localhost:9001:DEFAULT

export THREADS=100
./runtest.sh orion -p hosts=orion:localhost:9001:DEFAULT

export THREADS=1000
./runtest.sh orion -p hosts=orion:localhost:9001:DEFAULT

#####################################à####
# COLLECT
##########################################
now=$(date +"%m_%d_%Y")
zip collect_$now.zip orion.* redis.* aerospike.* mongodb.* hbase.*

#####################################à####
# CLEANUP
##########################################
./cleanup.sh aerospike
./cleanup.sh redis
./cleanup.sh mongodb
./cleanup.sh orion
./cleanup.sh hbase

