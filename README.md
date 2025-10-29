# QuarkusAI - RAG Chat Application

A Retrieval-Augmented Generation (RAG) chat application built with Quarkus that allows users to upload documents and chat with an AI that can reference the uploaded content.

## Features

- **Document Upload**: Support for multiple file formats (.txt, .pdf, .doc, .docx, .md)
- **Multiple File Upload**: Select and upload multiple documents at once with progress tracking
- **RAG Chat**: Chat with AI that can reference uploaded document content
- **Document Management**: View, delete, and manage uploaded documents
- **Vector Search**: Semantic search through document content using embeddings
- **Real-time UI**: HTMX-powered interface with live updates

## Components

- **Backend**: Quarkus framework with JAX-RS REST endpoints
- **AI Model**: Ollama with Llama 3.2 3B for chat and nomic-embed-text for embeddings
- **Vector Database**: Qdrant for storing document embeddings
- **Document Processing**: Apache Tika for text extraction from various file formats
- **Frontend**: HTMX-powered web interface with real-time chat and document management

## Architecture

The application follows a clean architecture pattern:

- **REST Layer**: Document upload/management and chat endpoints
- **Service Layer**: RAG chat service, document processing service
- **Integration Layer**: Ollama AI models, Qdrant vector store
- **Frontend**: HTMX-based reactive web interface

## Prerequisites

- Docker and Docker Compose
- (Optional) Ollama with required models if running separately

## Setup

### Quick Start with Docker Compose

```bash
docker-compose up -d
```

**⚠️ Caution**: Initial setup downloads ~8.5 GB of AI models and images and takes about 15-20 minutes.

This command:
1. Builds the Quarkus application (Maven build inside Docker)
2. Builds Ollama container with pre-loaded models (llama3.2:3b, nomic-embed-text)
3. Starts all services (Qdrant, Ollama, QuarkusAI)

Alternatively, use the provided setup script:
```bash
./setup.sh
```

### Using Existing Ollama Installation

If you already have Ollama running locally with the required models:

```bash
# Build and start only Qdrant and the application
docker run -d --name qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant
docker build -t quarkusai-app .
docker run -d --name quarkusai-app -p 8080:8080 \
  -e QUARKUS_LANGCHAIN4J_QDRANT_HOST=host.docker.internal \
  -e QUARKUS_LANGCHAIN4J_OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  quarkusai-app
```

Required models in your existing Ollama:
```bash
ollama pull llama3.2:3b
ollama pull nomic-embed-text
```

### Manual Setup

#### 1. Start External Services

Start Qdrant vector database:
```bash
docker run -d --name qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant
```

Install and start Ollama with required models:
```bash
# Install Ollama (see https://ollama.ai)
ollama pull llama3.2:3b
ollama pull nomic-embed-text
```

#### 2. Run the Application

##### Development Mode
```bash
./mvnw quarkus:dev
```

##### Production Mode (Docker)
```bash
docker build -t quarkusai-app .
docker run -d --name quarkusai-app -p 8080:8080 \
  -e QUARKUS_LANGCHAIN4J_QDRANT_HOST=host.docker.internal \
  -e QUARKUS_LANGCHAIN4J_OLLAMA_BASE_URL=http://host.docker.internal:11434 \
  quarkusai-app
```

### Access the Application

Open your browser and navigate to: http://localhost:8080

**Additional URLs:**
- Qdrant Dashboard: http://localhost:6333/dashboard
- Ollama API: http://localhost:11434

## Usage

1. **Upload Documents**: Use the file upload section to add documents (supports multiple file selection)
2. **Chat**: Ask questions in the chat interface - the AI will reference uploaded documents when relevant
3. **Manage Documents**: View uploaded documents and delete them as needed

## Testing

The project includes both unit tests and integration tests that are properly separated:

### Unit Tests
Run unit tests (fast, no external dependencies required):
```bash
./mvnw test
```

### Integration Tests
Integration tests require the full environment (Qdrant, Ollama) to be running.

**Option 1: Using the provided script**
```bash
./run-integration-tests.sh
```

**Option 2: Using Maven profile directly**
```bash
./mvnw test -Pintegration-tests
```

**Prerequisites for Integration Tests:**
- Docker containers must be running: `docker-compose up -d`
- All services (Qdrant, Ollama) must be healthy and accessible

The integration tests are excluded from regular builds to prevent failures in CI/CD environments where the complete infrastructure isn't available.

## Configuration

Key configuration properties in `src/main/resources/application.properties`:

- `quarkus.langchain4j.ollama.base-url`: Ollama service URL
- `quarkus.langchain4j.qdrant.host`: Qdrant database host
- `quarkus.http.limits.max-body-size`: Maximum file upload size (default: 50M)
