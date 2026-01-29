package util;

import com.sun.net.httpserver.HttpExchange;
import model.Offer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Helper {
    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void setOffer(Offer offer, JSONObject json) {
        offer.setOwnerId(json.getInt("ownerId"));
        offer.setLat(json.getDouble("lat"));
        offer.setLon(json.getDouble("lon"));
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
    }

    public static JSONObject setOfferJsonObject(Offer o) {
        JSONObject json = new JSONObject();
        json.put("id", o.getId());
        json.put("ownerId", o.getOwnerId());
        json.put("lat", o.getLat());
        json.put("lon", o.getLon());
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

    public static JSONObject getJsonObject(String body) {
        JSONObject json = new JSONObject(body);
        String[] required = {"ownerId", "lat", "lon", "price", "beds", "startDate", "endDate"};
        for (String key : required) {
            if (!json.has(key)) throw new IllegalArgumentException("Fehlendes Feld: " + key);
        }
        if (json.getInt("beds") <= 0) {
            throw new IllegalArgumentException("Es muss mindestens ein Bett angeboten werden.");
        }
        return json;
    }
}
