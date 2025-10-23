#!/bin/bash

echo "🛑 Stopping QuarkusAI RAG Stack..."

docker-compose down

echo "✅ Stack stopped successfully!"
echo "💾 Data is preserved in Docker volumes"
echo "🔄 Run ./start-dev.sh to restart"
