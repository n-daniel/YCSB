#!/bin/bash

# if not set THREADS use default
if [ -z "$THREADS" ]; then
  THREADS=1
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
	bin/ycsb load $1 -P $f $2 $3 $4 $5 $6 $7 $8 $9
	bin/ycsb run $1 -P $f $2 $3 $4 $5 $6 $7 $8 $9 -threads $THREADS > $1.$THREADS.$(basename $f)
done
