#!/bin/bash

echo "Running QuarkusAI Integration Tests..."
echo "======================================="

# Check if Docker containers are running
echo "Checking Docker containers..."
if ! docker ps | grep -q "qdrant"; then
    echo "❌ Qdrant container not running. Please start with: docker-compose up -d"
    exit 1
fi

if ! docker ps | grep -q "ollama"; then
    echo "❌ Ollama container not running. Please start with: docker-compose up -d"
    exit 1
fi

echo "✅ Required containers are running"
echo ""

# Package the application first (required for integration tests)
echo "Packaging application..."
./mvnw clean package -DskipTests

echo ""
echo "Running integration tests..."
./mvnw test -Pintegration-tests

echo ""
echo "Integration tests completed!"
