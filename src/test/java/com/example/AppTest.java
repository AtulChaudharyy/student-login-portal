package com.example;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void parseFormDecodesFields() {
        String body = "studentName=Atul+Chaudhary&rollNumber=101&email=atul%40example.com&course=DevOps";
        Map<String, String> parsed = App.parseForm(body);

        assertEquals("Atul Chaudhary", parsed.get("studentName"));
        assertEquals("101", parsed.get("rollNumber"));
        assertEquals("atul@example.com", parsed.get("email"));
        assertEquals("DevOps", parsed.get("course"));
    }

    @Test
    void confirmationHtmlIncludesEscapedValues() {
        String html = App.buildConfirmationHtml("Alice", "22", "alice@test.com", "Cloud");
        assertTrue(html.contains("Alice"));
        assertTrue(html.contains("22"));
        assertTrue(html.contains("alice@test.com"));
        assertTrue(html.contains("Cloud"));
    }
}
