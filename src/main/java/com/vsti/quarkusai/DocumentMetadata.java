package com.vsti.quarkusai;

import java.time.LocalDateTime;

public record DocumentMetadata(
    String id,
    String filename,
    String contentType,
    long size,
    LocalDateTime uploadedAt,
    int chunkCount
) {
    public static DocumentMetadata create(String id, String filename, String contentType, long size) {
        return new DocumentMetadata(id, filename, contentType, size, LocalDateTime.now(), 0);
    }
    
    public DocumentMetadata withChunkCount(int chunkCount) {
        return new DocumentMetadata(id, filename, contentType, size, uploadedAt, chunkCount);
    }
}
