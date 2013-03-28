#!/bin/bash
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

echo "going to kill all the momory fuffa"

ipcs -m|grep ^0x|while read a b c ; 
  do 
if [ "$b" != '0' ]; 
then  
ipcrm -m $b;
ipcrm -m $b; 
fi; 
done
