package com.vsti;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class ChatResourceTest {
    @Test
    void testIndexEndpoint() {
        given()
          .when().get("/")
          .then()
             .statusCode(200)
             .contentType("text/html");
    }

    @Test
    void testChatEndpoint() {
        given()
          .formParam("message", "test message")
          .when().post("/chat")
          .then()
             .statusCode(200)
             .contentType("text/html")
             .body(containsString("You said: test message"));
    }

}