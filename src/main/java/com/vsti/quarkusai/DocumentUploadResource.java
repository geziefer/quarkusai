package com.vsti.quarkusai;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
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
    public Response uploadDocuments(MultiFileUploadForm form) {
        List<DocumentMetadata> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (form.files != null && form.fileNames != null) {
            for (int i = 0; i < Math.min(form.files.size(), form.fileNames.size()); i++) {
                String fileName = form.fileNames.get(i);
                String contentType = (form.contentTypes != null && i < form.contentTypes.size()) 
                    ? form.contentTypes.get(i) : "application/octet-stream";
                
                if (fileName != null && !fileName.isEmpty()) {
                    try {
                        DocumentMetadata metadata = documentService.processDocument(
                            fileName,
                            contentType,
                            0, // Size not available in this approach
                            form.files.get(i)
                        );
                        results.add(metadata);
                    } catch (IOException e) {
                        errors.add("Failed to process " + fileName + ": " + e.getMessage());
                    }
                }
            }
        }

        return Response.ok(new UploadResponse(results, errors)).build();
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
    public Response deleteDocument(@PathParam("documentId") String documentId) {
        boolean deleted = documentService.deleteDocument(documentId);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    public record UploadResponse(List<DocumentMetadata> successful, List<String> errors) {}
}
