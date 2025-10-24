#!/bin/bash

# Wait for Qdrant to be ready
echo "Waiting for Qdrant to start..."
until curl -s http://localhost:6333/health > /dev/null; do
  sleep 1
done

echo "Creating documents collection..."
curl -X PUT "http://localhost:6333/collections/documents" \
  -H "Content-Type: application/json" \
  -d '{
    "vectors": {
      "size": 768,
      "distance": "Cosine"
    }
  }'

echo "Collection initialization complete."
