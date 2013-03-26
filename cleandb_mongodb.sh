#!/bin/bash

mongo <<EOF
use ycsb
db.dropDatabase();
quit
EOF
