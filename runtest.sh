#!/bin/bash

# if not set THREADS use default
if [ -z "$THREADS" ]; then
  THREADS=1
fi

# if not set GRANULARITY use default (1sec)
if [ -z "$GRANULARITY" ]; then
  GRANULARITY=1000
fi


# clean up 
CLEANUP=$1.$THREADS.* 
for f0 in $CLEANUP
do
	echo "delete $f0"
	rm $f0
done

# execute test
FILES=workloads/*
for f in $FILES
do
	now=$(date +"%Y%m%d_%H_%M_%S")
	bin/ycsb load $1 -P $f $2 $3 $4 $5 $6 $7 $8 $9 -p table=$(basename $f)
	bin/ycsb run $1 -P $f $2 $3 $4 $5 $6 $7 $8 $9 -threads $THREADS -p table=$(basename $f) -p measurementtype=timeseries -p timeseries.granularity=$GRANULARITY > $1.$THREADS.$(basename $f).$now
done
