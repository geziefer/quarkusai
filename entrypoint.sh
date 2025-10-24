#!/bin/bash

# Start Qdrant in background
./qdrant &
QDRANT_PID=$!

# Run initialization script
/docker-entrypoint-initdb.d/init-collection.sh

# Bring Qdrant to foreground
wait $QDRANT_PID
