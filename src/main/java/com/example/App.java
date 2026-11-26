package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", App::handleHome);
        server.createContext("/register", App::handleRegister);
        server.setExecutor(null);
        server.start();

        System.out.println("Student Registration server running on http://localhost:" + port);
    }

    private static void handleHome(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }

        byte[] htmlBytes;
        try (InputStream is = App.class.getResourceAsStream("/index.html")) {
            if (is == null) {
                sendResponse(exchange, 404, "text/plain", "index.html not found");
                return;
            }
            htmlBytes = is.readAllBytes();
        }
        sendBytes(exchange, 200, "text/html; charset=UTF-8", htmlBytes);
    }

    private static void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Location", "/");
            sendResponse(exchange, 302, "text/plain", "");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseForm(body);

        String responseHtml = buildConfirmationHtml(
                params.getOrDefault("studentName", ""),
                params.getOrDefault("rollNumber", ""),
                params.getOrDefault("email", ""),
                params.getOrDefault("course", "")
        );
        sendResponse(exchange, 200, "text/html; charset=UTF-8", responseHtml);
    }

    public static Map<String, String> parseForm(String body) {
        Map<String, String> result = new LinkedHashMap<>();
        if (body == null || body.isEmpty()) {
            return result;
        }
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                result.put(key, value);
            }
        }
        return result;
    }

    public static String buildConfirmationHtml(String name, String roll, String email, String course) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Registration Successful</title>
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f6f8; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
                        .card { background: #fff; padding: 24px 32px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); max-width: 420px; width: 100%%; }
                        h2 { color: #1a7f37; margin-top: 0; }
                        p { margin: 8px 0; color: #333; }
                        a { display: inline-block; margin-top: 16px; color: #0066cc; text-decoration: none; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h2>Registration Submitted</h2>
                        <p><strong>Student Name:</strong> %s</p>
                        <p><strong>Roll Number:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Course:</strong> %s</p>
                        <a href="/">Register Another Student</a>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(name), escapeHtml(roll), escapeHtml(email), escapeHtml(course));
    }

    private static String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static void sendResponse(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        sendBytes(exchange, status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
