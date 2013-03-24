#!/bin/bash

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
zip collect.zip orion.* redis.*

#####################################à####
# CLEANUP
##########################################
./cleanup.sh redis
./cleanup.sh orion

