#!/bin/bash

echo "🚀 Setting up QuarkusAI RAG Chat Application..."
echo "⚠️  Initial build will download ~5GB of AI models and may take 10-15 minutes"

# Start all services with docker-compose (builds everything)
echo "🐳 Building and starting all services..."
docker-compose up -d

echo "✅ Setup complete!"
echo "🌐 Application available at: http://localhost:8080"
echo "📊 Qdrant dashboard at: http://localhost:6333/dashboard"
echo ""
echo "To stop all services: docker-compose down"
echo "To view logs: docker-compose logs -f"
