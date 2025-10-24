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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
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
            Map<String, Integer> chunkCounts = new HashMap<>();
            Map<String, List<String>> segmentIdsByDoc = new HashMap<>();
            
            searchResult.matches().forEach(match -> {
                var segment = match.embedded();
                var metadata = segment.metadata();
                String documentId = metadata.getString("documentId");
                String filename = metadata.getString("filename");
                String segmentId = match.embeddingId();
                
                if (documentId != null && filename != null && segmentId != null) {
                    chunkCounts.put(documentId, chunkCounts.getOrDefault(documentId, 0) + 1);
                    segmentIdsByDoc.computeIfAbsent(documentId, k -> new ArrayList<>()).add(segmentId);
                    
                    if (!documents.containsKey(documentId)) {
                        // Create metadata for restored document
                        DocumentMetadata docMetadata = DocumentMetadata.create(documentId, filename, "text/plain", 0);
                        documents.put(documentId, docMetadata);
                    }
                }
            });
            
            // Update chunk counts and segment IDs
            chunkCounts.forEach((docId, count) -> {
                DocumentMetadata existing = documents.get(docId);
                if (existing != null) {
                    documents.put(docId, existing.withChunkCount(count));
                }
            });
            
            // Restore segment ID tracking
            segmentIdsByDoc.forEach((docId, segmentIds) -> {
                documentSegmentIds.put(docId, segmentIds);
            });
            
            System.out.println("Restored " + documents.size() + " documents from vector store");
        } catch (Exception e) {
            System.out.println("Collection initialization completed: " + e.getMessage());
        }
    }

    public DocumentMetadata processDocument(String filename, String contentType, long size, InputStream inputStream) throws IOException {
        // Check for existing document with same filename and remove from metadata only
        String existingDocumentId = documents.values().stream()
            .filter(doc -> doc.filename().equals(filename))
            .map(DocumentMetadata::id)
            .findFirst()
            .orElse(null);
        
        if (existingDocumentId != null) {
            // Remove from metadata maps (vector store entries will remain but become orphaned)
            documents.remove(existingDocumentId);
            documentSegmentIds.remove(existingDocumentId);
        }
        
        String documentId = UUID.randomUUID().toString();
        
        // Extract text using Tika with embedded content disabled
        String content;
        try {
            // Read stream into byte array first
            byte[] data = inputStream.readAllBytes();
            
            // Configure parser to skip embedded content
            org.apache.tika.parser.AutoDetectParser parser = new org.apache.tika.parser.AutoDetectParser();
            org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
            org.apache.tika.parser.ParseContext context = new org.apache.tika.parser.ParseContext();
            
            // Disable embedded document extraction
            context.set(org.apache.tika.extractor.EmbeddedDocumentExtractor.class, 
                       new org.apache.tika.extractor.EmbeddedDocumentExtractor() {
                           @Override
                           public boolean shouldParseEmbedded(org.apache.tika.metadata.Metadata metadata) {
                               return false;
                           }
                           @Override
                           public void parseEmbedded(java.io.InputStream stream, org.xml.sax.ContentHandler handler, 
                                                   org.apache.tika.metadata.Metadata metadata, boolean outputHtml) {
                               // Do nothing - skip embedded content
                           }
                       });
            
            java.io.StringWriter writer = new java.io.StringWriter();
            org.apache.tika.sax.BodyContentHandler handler = new org.apache.tika.sax.BodyContentHandler(writer);
            
            parser.parse(new java.io.ByteArrayInputStream(data), handler, tikaMetadata, context);
            content = writer.toString();
        } catch (Exception e) {
            // If all else fails, return a basic error message
            content = "Document content could not be extracted due to parsing issues.";
        }
        
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
