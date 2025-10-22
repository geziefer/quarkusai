# RAG Implementation Plan

## Project Overview
Extend existing Quarkus + LangChain4j + Ollama chat application with RAG (Retrieval Augmented Generation) capabilities for document processing.

## Technical Choices
- **Vector DB**: Chroma (lightweight, persistent, excellent Quarkus integration)
- **Embeddings**: Ollama with `nomic-embed-text` model (optimized for RAG, good performance)
- **Chunk size**: 500 tokens with 50 token overlap (good balance for accuracy)
- **Document processing**: Apache Tika for universal document parsing

## Implementation Steps

### Step 1: Dependencies & Configuration ✅
- [x] Add LangChain4j vector store, document loaders, and Chroma dependencies
- [x] Configure Chroma database path and Ollama embedding model
- [x] Set up file upload configuration

### Step 2: Document Processing Service ✅
- [x] Create document parser using Tika
- [x] Implement chunking with RecursiveCharacterTextSplitter
- [x] Create embedding service using Ollama
- [x] Build vector store integration with Chroma

### Step 3: File Upload Endpoint ⏳
- [ ] REST endpoint for multi-file upload with progress tracking
- [ ] Background processing with WebSocket progress updates
- [ ] Document metadata storage (filename, upload date, chunk count)

### Step 4: RAG Integration ⏳
- [ ] Modify existing chat service to include vector search
- [ ] Implement similarity search before LLM query
- [ ] Add document references to chat responses

### Step 5: UI Enhancements ⏳
- [ ] Add file upload component with progress bar
- [ ] Display uploaded documents list
- [ ] Add delete functionality for documents
- [ ] Show document sources in chat responses

### Step 6: Document Management ⏳
- [ ] REST endpoints for listing/deleting documents
- [ ] Remove document chunks from vector store on deletion

### Step 7: Docker Containerization ⏳
- [ ] Create docker-compose.yml with Ollama, Chroma, and application
- [ ] Configure Ollama with required models (mistral:7b-instruct-q4_K_M, nomic-embed-text)
- [ ] Set up persistent volumes for Ollama models and Chroma data
- [ ] Update application configuration for containerized services
- [ ] Add development and production docker-compose variants

## Status Legend
- ⏳ Not started
- 🔄 In progress
- ✅ Completed
- ❌ Blocked/Issues

## Notes
- File size limit: Not defined (typical documents ~couple MB)
- Processing: Immediate upon upload with progress indication
- Multi-file upload supported
- Persistent vector storage required
- Document references shown in chat responses
- Simple UI with upload capability and document list
- Document deletion removes from RAG
