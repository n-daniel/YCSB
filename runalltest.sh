#!/bin/bash

#####################################à####
# AEROSPIKE
##########################################
export THREADS=1
./runtest.sh aerospike

export THREADS=100
./runtest.sh aerospike

export THREADS=1000
./runtest.sh aerospike

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
zip collect.zip orion.* redis.* aerospike.* mongodb.*

#####################################à####
# CLEANUP
##########################################
./cleanup.sh aerospike
./cleanup.sh redis
./cleanup.sh mongodb
./cleanup.sh orion
