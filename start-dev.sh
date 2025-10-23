#!/bin/bash

echo "🚀 Starting QuarkusAI RAG Stack..."

# Build the application first
echo "📦 Building Quarkus application..."
./mvnw package -DskipTests

# Start the stack
echo "🐳 Starting Docker containers..."
docker-compose up -d ollama chroma

echo "⏳ Waiting for services to be healthy..."
sleep 10

# Load models
echo "🤖 Loading AI models..."
docker-compose up model-loader

echo "🏗️  Starting application..."
docker-compose up -d app

echo "✅ Stack started successfully!"
echo "🌐 Application: http://localhost:8080"
echo "🤖 Ollama: http://localhost:11434"
echo "🔍 Chroma: http://localhost:8000"

echo "📊 Checking service status..."
docker-compose ps
