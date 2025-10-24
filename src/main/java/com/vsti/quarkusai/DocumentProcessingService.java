package com.vsti.quarkusai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class DocumentProcessingService {

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    private final Tika tika = new Tika();
    private final DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
    private final ConcurrentMap<String, DocumentMetadata> documents = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<String>> documentSegmentIds = new ConcurrentHashMap<>();

    @PostConstruct
    void initialize() {
        try {
            // Search for all documents to rebuild metadata from existing embeddings
            var searchResult = embeddingStore.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(embeddingModel.embed("document").content())
                    .maxResults(1000)
                    .minScore(0.0)
                    .build());
            
            // Rebuild document metadata from stored segments
            searchResult.matches().forEach(match -> {
                var segment = match.embedded();
                var metadata = segment.metadata();
                String documentId = metadata.getString("documentId");
                String filename = metadata.getString("filename");
                
                if (documentId != null && filename != null && !documents.containsKey(documentId)) {
                    // Create metadata for restored document
                    DocumentMetadata docMetadata = DocumentMetadata.create(documentId, filename, "text/plain", 0);
                    documents.put(documentId, docMetadata);
                }
            });
            
            System.out.println("Restored " + documents.size() + " documents from vector store");
        } catch (Exception e) {
            System.out.println("Collection initialization completed: " + e.getMessage());
        }
    }

    public DocumentMetadata processDocument(String filename, String contentType, long size, InputStream inputStream) throws IOException {
        String documentId = UUID.randomUUID().toString();
        
        try {
            // Extract text using Tika
            String content = tika.parseToString(inputStream);
            
            // Create document and split into chunks
            Document document = Document.from(content);
            List<TextSegment> segments = splitter.split(document);
            
            // Add metadata to segments
            segments.forEach(segment -> segment.metadata().put("documentId", documentId)
                                               .put("filename", filename));
            
            // Generate embeddings and store
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            List<String> segmentIds = embeddingStore.addAll(embeddings, segments);
            
            // Track segment IDs for deletion
            documentSegmentIds.put(documentId, segmentIds);
            
            // Create and store metadata
            DocumentMetadata metadata = DocumentMetadata.create(documentId, filename, contentType, size)
                                                       .withChunkCount(segments.size());
            documents.put(documentId, metadata);
            
            return metadata;
        } catch (TikaException e) {
            throw new IOException("Failed to parse document content: " + filename, e);
        }
    }

    public List<DocumentMetadata> getAllDocuments() {
        return List.copyOf(documents.values());
    }

    public boolean deleteDocument(String documentId) {
        DocumentMetadata metadata = documents.remove(documentId);
        List<String> segmentIds = documentSegmentIds.remove(documentId);
        
        if (metadata != null && segmentIds != null) {
            // Remove segments from vector store
            segmentIds.forEach(embeddingStore::remove);
            return true;
        }
        return false;
    }
}
