package com.vsti.quarkusai;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMetadataTest {

    @Test
    void shouldCreateNewInstanceWithUpdatedChunkCount() {
        // Given
        DocumentMetadata original = DocumentMetadata.create("doc-123", "test.pdf", "application/pdf", 1024L);
        int newChunkCount = 5;
        
        // When
        DocumentMetadata updated = original.withChunkCount(newChunkCount);
        
        // Then
        assertEquals(original.id(), updated.id());
        assertEquals(original.filename(), updated.filename());
        assertEquals(original.contentType(), updated.contentType());
        assertEquals(original.size(), updated.size());
        assertEquals(original.uploadedAt(), updated.uploadedAt());
        assertEquals(newChunkCount, updated.chunkCount());
        
        // Original should remain unchanged (immutability)
        assertEquals(0, original.chunkCount());
    }

    @Test
    void shouldBeImmutable() {
        // Given
        DocumentMetadata metadata = DocumentMetadata.create("doc-123", "test.pdf", "application/pdf", 1024L);
        
        // When
        DocumentMetadata updated = metadata.withChunkCount(10);
        
        // Then
        assertNotSame(metadata, updated);
        assertEquals(0, metadata.chunkCount());
        assertEquals(10, updated.chunkCount());
    }

    @Test
    void shouldHandleChunkCountUpdates() {
        // Given
        DocumentMetadata original = DocumentMetadata.create("doc-123", "test.pdf", "application/pdf", 1024L);
        
        // When - multiple updates
        DocumentMetadata step1 = original.withChunkCount(5);
        DocumentMetadata step2 = step1.withChunkCount(10);
        DocumentMetadata step3 = step2.withChunkCount(0);
        
        // Then
        assertEquals(0, original.chunkCount());
        assertEquals(5, step1.chunkCount());
        assertEquals(10, step2.chunkCount());
        assertEquals(0, step3.chunkCount());
        
        // All other fields should remain the same
        assertEquals(original.id(), step3.id());
        assertEquals(original.filename(), step3.filename());
        assertEquals(original.uploadedAt(), step3.uploadedAt());
    }

    @Test
    void shouldCreateWithCurrentTimestamp() {
        // Given
        LocalDateTime before = LocalDateTime.now();
        
        // When
        DocumentMetadata metadata = DocumentMetadata.create("doc-123", "test.pdf", "application/pdf", 1024L);
        
        LocalDateTime after = LocalDateTime.now();
        
        // Then - timestamp should be between before and after
        assertTrue(metadata.uploadedAt().isAfter(before.minusSeconds(1)));
        assertTrue(metadata.uploadedAt().isBefore(after.plusSeconds(1)));
    }
}
