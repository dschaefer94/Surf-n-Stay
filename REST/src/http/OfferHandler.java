package http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dao.OfferDao;
import model.Offer;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Helper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OfferHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET" -> {
                List<Offer> offers = OfferDao.getAllOffers();
                JSONArray array = new JSONArray();
                for (Offer o : offers) {
                    JSONObject json = getJsonObject(o);
                    array.put(json);
                }
                Helper.sendResponse(exchange, 200, array.toString());
            }
            case "POST" -> {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(body);
                    String[] required = {"ownerId", "lat", "lon", "address", "price", "beds", "startDate", "endDate"};
                    for (String key : required) {
                        if (!json.has(key)) {
                            Helper.sendResponse(exchange, 400, "{\"error\":\"Fehlendes Feld: " + key + "\"}");
                            return;
                        }
                    }
                    if (json.getString("address").isBlank()) {
                        Helper.sendResponse(exchange, 400, "{\"error\":\"address darf nicht leer sein\"}");
                        return;
                    }
                    if (json.getInt("beds") <= 0) {
                        Helper.sendResponse(exchange, 400, "{\"error\":\"beds muss > 0 sein\"}");
                        return;
                    }
                    if (!json.getString("startDate").matches("\\d{4}-\\d{2}-\\d{2}") ||
                            !json.getString("endDate").matches("\\d{4}-\\d{2}-\\d{2}")) {
                        Helper.sendResponse(exchange, 400, "{\"error\":\"startDate/endDate müssen im Format YYYY-MM-DD sein\"}");
                        return;
                    }
                    Offer offer = new Offer();
                    offer.setOwnerId(json.getInt("ownerId"));
                    offer.setLat(json.getDouble("lat"));
                    offer.setLon(json.getDouble("lon"));
                    offer.setAddress(json.getString("address"));
                    offer.setPrice(json.getString("price"));
                    offer.setBeds(json.getInt("beds"));
                    offer.setStartDate(json.getString("startDate"));
                    offer.setEndDate(json.getString("endDate"));
                    offer.setHasSauna(json.optBoolean("hasSauna", false));
                    offer.setHasFireplace(json.optBoolean("hasFireplace", false));
                    offer.setSmoker(json.optBoolean("isSmoker", false));
                    offer.setHasPets(json.optBoolean("hasPets", false));
                    offer.setHasInternet(json.optBoolean("hasInternet", true));
                    offer.setPublished(json.optBoolean("isPublished", false));
                    int newId = OfferDao.insertOffer(offer);
                    offer.setId(newId);

                    exchange.getResponseHeaders().set("Location", "/offers/" + newId);
                    Helper.sendResponse(exchange, 201, getJsonObject(offer).toString());

                } catch (RuntimeException re) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"server error\"}");
                }
            }
            case "DELETE" -> {
                System.out.println("delete");
            }
            default -> {
                System.out.println("default");
            }
        }
    }

    private static JSONObject getJsonObject(Offer o) {
        JSONObject json = new JSONObject();
        json.put("id", o.getId());
        json.put("ownerId", o.getOwnerId());
        json.put("lat", o.getLat());
        json.put("lon", o.getLon());
        json.put("address", o.getAddress());
        json.put("price", o.getPrice());
        json.put("beds", o.getBeds());
        json.put("startDate", o.getStartDate());
        json.put("endDate", o.getEndDate());
        json.put("hasPets", o.getHasPets());
        json.put("hasFireplace", o.getHasFireplace());
        json.put("isSmoker", o.getSmoker());
        json.put("hasInternet", o.getHasInternet());
        json.put("hasSauna", o.getHasSauna());
        json.put("isPublished", o.getPublished());
        return json;
    }
}

