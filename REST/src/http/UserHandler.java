package http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;

public class UserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String response;
        int statusCode;

        // WICHTIG: Header für JSON setzen
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        switch (method) {
            case "GET" -> {
                // Ein JSON-Array mit Test-Daten
                response = "[" +
                        "{\"id\": 1, \"name\": \"Lukas\", \"email\": \"lukas@example.com\"}," +
                        "{\"id\": 2, \"name\": \"Anna\", \"email\": \"anna@example.com\"}" +
                        "]";
                statusCode = 200;
            }
            case "POST" -> {
                // 1. Den InputStream holen (hier kommen die Daten vom curl an)
                InputStream is = exchange.getRequestBody();

                // 2. Den Stream in einen String umwandeln
                // (Wir nutzen einen Scanner, das ist der einfachste Weg in Standard-Java)
                String body = "";
                try (java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A")) {
                    body = s.hasNext() ? s.next() : "";
                }

                // 3. Jetzt kannst du den Inhalt loggen oder verarbeiten
                System.out.println("Empfangene Daten: " + body);

                response = "{\"message\": \"Daten empfangen\", \"deinBody\": " + body + "}";
                statusCode = 201;
            }
            case "DELETE" -> {
                response = "{\"message\": \"User gelöscht\"}";
                statusCode = 200;
            }
            default -> {
                response = "{\"error\": \"Methode " + method + " nicht erlaubt\"}";
                statusCode = 405;
            }
        }

        // Antwort senden
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }
}