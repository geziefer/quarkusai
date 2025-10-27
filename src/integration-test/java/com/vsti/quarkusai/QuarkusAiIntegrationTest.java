package com.vsti.quarkusai;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuarkusAiIntegrationTest {

    private static List<String> testDocumentIds = new ArrayList<>();
    private static int initialDocumentCount;

    @BeforeAll
    static void recordInitialState() {
        // Record how many documents exist before our tests
        initialDocumentCount = given()
                .when()
                .get("/documents")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        
        System.out.println("Initial document count: " + initialDocumentCount);
    }

    @AfterAll
    static void cleanupTestData() {
        // Clean up any test documents we created
        for (String documentId : testDocumentIds) {
            try {
                given()
                        .when()
                        .delete("/documents/" + documentId)
                        .then()
                        .statusCode(anyOf(equalTo(200), equalTo(404))); // 404 is OK if already deleted
            } catch (Exception e) {
                System.out.println("Failed to cleanup document " + documentId + ": " + e.getMessage());
            }
        }
        System.out.println("Cleaned up " + testDocumentIds.size() + " test documents");
    }

    @Test
    @Order(1)
    void shouldServeMainPage() {
        given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body(containsString("vsti RAG Chat"))
                .body(containsString("Document Library"));
    }

    @Test
    @Order(2)
    void shouldListDocuments() {
        given()
                .when()
                .get("/documents")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class))
                .body("size()", equalTo(initialDocumentCount));
    }

    @Test
    @Order(3)
    void shouldUploadTestDocument() {
        String testContent = "Integration test document about machine learning algorithms.";
        
        given()
                .multiPart("file", "integration-test.txt", new ByteArrayInputStream(testContent.getBytes()), "text/plain")
                .formParam("filename", "integration-test.txt")
                .when()
                .post("/documents/upload")
                .then()
                .statusCode(200)
                .body(containsString("uploaded successfully"));

        // Verify document count increased by 1
        given()
                .when()
                .get("/documents")
                .then()
                .statusCode(200)
                .body("size()", equalTo(initialDocumentCount + 1));

        // Find and store our test document ID
        String testDocId = given()
                .when()
                .get("/documents")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("find { it.filename == 'integration-test.txt' }.id");

        if (testDocId != null) {
            testDocumentIds.add(testDocId);
        }
    }

    @Test
    @Order(4)
    void shouldHandleChatRequest() {
        given()
                .formParam("message", "What is machine learning?")
                .when()
                .post("/chat")
                .then()
                .statusCode(200)
                .body(notNullValue());
    }

    @Test
    @Order(5)
    void shouldServeDocumentListPage() {
        given()
                .when()
                .get("/documents/list")
                .then()
                .statusCode(200)
                .body(containsString("document-item")); // Check for actual content instead of "Documents"
    }

    @Test
    @Order(6)
    void shouldDeleteTestDocument() {
        if (!testDocumentIds.isEmpty()) {
            String testDocId = testDocumentIds.get(0);
            
            given()
                    .when()
                    .delete("/documents/" + testDocId)
                    .then()
                    .statusCode(200);

            // Verify document count decreased by 1
            given()
                    .when()
                    .get("/documents")
                    .then()
                    .statusCode(200)
                    .body("size()", equalTo(initialDocumentCount));

            // Remove from our tracking list since it's deleted
            testDocumentIds.remove(testDocId);
        }
    }

    @Test
    void shouldHandleInvalidDocumentDeletion() {
        given()
                .when()
                .delete("/documents/non-existent-test-id")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldHandleInvalidUpload() {
        given()
                .when()
                .post("/documents/upload")
                .then()
                .statusCode(415); // Unsupported Media Type is expected for no multipart data
    }

    @Test
    void shouldHandleInvalidChatRequest() {
        given()
                .when()
                .post("/chat")
                .then()
                .statusCode(500); // Null message causes IllegalArgumentException
    }

    @Test
    void shouldHandleNonExistentEndpoint() {
        given()
                .when()
                .get("/non-existent-endpoint")
                .then()
                .statusCode(404);
    }
}
