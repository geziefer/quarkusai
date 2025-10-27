package com.vsti.quarkusai;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("/documents")
public class DocumentUploadResource {

    @Inject
    DocumentProcessingService documentService;

    @Inject
    Template documents;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadDocument(@FormParam("file") InputStream fileStream,
                                   @FormParam("filename") String filename) {
        List<DocumentMetadata> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (fileStream != null && filename != null && !filename.isEmpty()) {
            try {
                DocumentMetadata metadata = documentService.processDocument(
                    filename,
                    "text/plain", // We could determine this from file extension
                    0, // Size not available
                    fileStream
                );
                results.add(metadata);
            } catch (IOException e) {
                errors.add("Failed to process " + filename + ": " + e.getMessage());
            }
        } else {
            errors.add("No file uploaded or filename missing");
        }

        // Add success message for UI feedback
        String message = results.isEmpty() ? "Upload failed" : 
                        results.size() + " document(s) uploaded successfully";

        return Response.ok(new UploadResponse(results, errors, message)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DocumentMetadata> listDocuments() {
        return documentService.getAllDocuments();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_HTML)
    public String listDocumentsHtml() {
        return documents.data("documents", documentService.getAllDocuments()).render();
    }

    @DELETE
    @Path("/{documentId}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocument(@PathParam("documentId") String documentId) {
        boolean deleted = documentService.deleteDocument(documentId);
        if (deleted) {
            return Response.ok(documents.data("documents", documentService.getAllDocuments()).render()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    public record UploadResponse(List<DocumentMetadata> successful, List<String> errors, String message) {}
}
