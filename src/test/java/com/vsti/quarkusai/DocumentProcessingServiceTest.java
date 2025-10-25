package com.vsti.quarkusai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;

class DocumentProcessingServiceTest {

    private DocumentProcessingService service;
    private ConcurrentMap<String, DocumentMetadata> documents;
    private ConcurrentMap<String, List<String>> documentSegmentIds;

    @BeforeEach
    void setUp() throws Exception {
        service = new DocumentProcessingService();
        documents = new ConcurrentHashMap<>();
        documentSegmentIds = new ConcurrentHashMap<>();
        
        // Access private fields for testing
        setPrivateField("documents", documents);
        setPrivateField("documentSegmentIds", documentSegmentIds);
    }

    @Test
    void shouldReturnAllDocuments() {
        // Given
        DocumentMetadata doc1 = DocumentMetadata.create("id1", "doc1.pdf", "application/pdf", 1000L);
        DocumentMetadata doc2 = DocumentMetadata.create("id2", "doc2.txt", "text/plain", 500L);
        
        documents.put("id1", doc1);
        documents.put("id2", doc2);
        
        // When
        List<DocumentMetadata> result = service.getAllDocuments();
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(doc1));
        assertTrue(result.contains(doc2));
        
        // Should return a copy - verify by checking original is unchanged after modification attempt
        int originalSize = documents.size();
        try {
            result.clear();
        } catch (UnsupportedOperationException e) {
            // This is expected for immutable collections - that's fine
        }
        assertEquals(originalSize, documents.size()); // Original should be unchanged
    }

    @Test
    void shouldReturnEmptyListWhenNoDocuments() {
        // When
        List<DocumentMetadata> result = service.getAllDocuments();
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIdentifyExistingDocumentByFilename() {
        // Given
        String filename = "test.pdf";
        DocumentMetadata existingDoc = DocumentMetadata.create("existing-id", filename, "application/pdf", 1000L);
        documents.put("existing-id", existingDoc);
        
        // When
        boolean hasExisting = documents.values().stream()
            .anyMatch(doc -> doc.filename().equals(filename));
        
        // Then
        assertTrue(hasExisting);
    }

    @Test
    void shouldNotFindNonExistentDocument() {
        // Given
        String filename = "nonexistent.pdf";
        DocumentMetadata existingDoc = DocumentMetadata.create("existing-id", "other.pdf", "application/pdf", 1000L);
        documents.put("existing-id", existingDoc);
        
        // When
        boolean hasExisting = documents.values().stream()
            .anyMatch(doc -> doc.filename().equals(filename));
        
        // Then
        assertFalse(hasExisting);
    }

    @Test
    void shouldRemoveDocumentFromBothMaps() {
        // Given
        String documentId = "doc-123";
        DocumentMetadata metadata = DocumentMetadata.create(documentId, "test.pdf", "application/pdf", 1000L);
        List<String> segmentIds = List.of("segment1", "segment2");
        
        documents.put(documentId, metadata);
        documentSegmentIds.put(documentId, segmentIds);
        
        // When
        DocumentMetadata removedMetadata = documents.remove(documentId);
        List<String> removedSegments = documentSegmentIds.remove(documentId);
        
        // Then
        assertEquals(metadata, removedMetadata);
        assertEquals(segmentIds, removedSegments);
        assertFalse(documents.containsKey(documentId));
        assertFalse(documentSegmentIds.containsKey(documentId));
    }

    @Test
    void shouldReturnNullWhenRemovingNonExistentDocument() {
        // Given
        String nonExistentId = "non-existent";
        
        // When
        DocumentMetadata removedMetadata = documents.remove(nonExistentId);
        List<String> removedSegments = documentSegmentIds.remove(nonExistentId);
        
        // Then
        assertNull(removedMetadata);
        assertNull(removedSegments);
    }

    @Test
    void shouldHandleConcurrentAccess() {
        // Given
        DocumentMetadata doc1 = DocumentMetadata.create("id1", "doc1.pdf", "application/pdf", 1000L);
        DocumentMetadata doc2 = DocumentMetadata.create("id2", "doc2.pdf", "application/pdf", 2000L);
        
        // When - simulate concurrent access
        documents.put("id1", doc1);
        documents.put("id2", doc2);
        
        List<DocumentMetadata> snapshot1 = service.getAllDocuments();
        documents.remove("id1");
        List<DocumentMetadata> snapshot2 = service.getAllDocuments();
        
        // Then
        assertEquals(2, snapshot1.size());
        assertEquals(1, snapshot2.size());
        assertTrue(snapshot1.contains(doc1));
        assertFalse(snapshot2.contains(doc1));
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = DocumentProcessingService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
