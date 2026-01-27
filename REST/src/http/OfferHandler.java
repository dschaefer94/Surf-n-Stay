package http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dao.OfferDao;
import model.Offer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OfferHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String response;
        int statusCode;
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        switch (method) {
            case "GET" -> {
                List<Offer> offers = OfferDao.getAllOffers();
                response
                        = offers.stream()
                        .map(o -> "{" +
                                "\"id\":" + o.getId() +
                                ",\"ownerId\":" + o.getOwnerId() +
                                ",\"lat\":" + o.getLat() +
                                ",\"lon\":" + o.getLon() +
                                ",\"address\":\"" + o.getAddress() + "\"" +
                                ",\"price\":\"" + o.getPrice() + "\"" +
                                ",\"beds\":" + o.getBeds() +
                                ",\"startDate\":\"" + o.getStartDate() + "\"" +
                                ",\"hasFireplace\":" + o.getHasFireplace() +
                                ",\"isSmoker\":" + o.getSmoker() +
                                ",\"hasInternet\":" + o.getHasInternet() +
                                ",\"isPublished\":" + o.getPublished() +
                                "}")
                        .collect(java.util.stream.Collectors.joining(",", "[", "]"));
                statusCode = 200;
            }
            case "POST" -> {
                InputStream is = exchange.getRequestBody();
                String body;
                try (java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A")) {
                    body = s.hasNext() ? s.next() : "";
                }
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
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }
}
