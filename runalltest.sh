#!/bin/bash

# Make sure only root can run our script
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

echo "##########################################"
echo "# AEROSPIKE"
echo "##########################################"

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

echo "##########################################"
echo "# REDIS"
echo "##########################################"

export THREADS=1
./runtest.sh redis -p redis.host=localhost

export THREADS=100
./runtest.sh redis -p redis.host=localhost

export THREADS=1000
./runtest.sh redis -p redis.host=localhost

echo "##########################################"
echo "# MONGODB"
echo "##########################################"

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

echo "##########################################"
echo "# HBASE"
echo "##########################################"

export THREADS=1
./runtest.sh hbase

export THREADS=100
./runtest.sh hbase

export THREADS=1000
./runtest.sh hbase

echo "##########################################"
echo "# ORION"
echo "##########################################"

export THREADS=1
./runtest.sh orion -p hosts=orion:localhost:9001:DEFAULT

export THREADS=100
./runtest.sh orion -p hosts=orion:localhost:9001:DEFAULT

export THREADS=1000
./runtest.sh orion -p hosts=orion:localhost:9001:DEFAULT

#####################################à####
# COLLECT
##########################################
now=$(date +"%Y%m%d_%H_%M_%S")
zip collect_$now.zip orion.* redis.* aerospike.* mongodb.* hbase.*
mv collect_$now.zip /var/www/html/test

#####################################à####
# CLEANUP
##########################################
./cleanup.sh aerospike
./cleanup.sh redis
./cleanup.sh mongodb
./cleanup.sh orion
./cleanup.sh hbase

