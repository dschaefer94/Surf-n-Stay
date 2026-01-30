package http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dao.OfferDao;
import model.Offer;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Helper;
import util.ErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OfferHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET"    -> handleGet(exchange);
                case "POST"   -> handlePost(exchange);
                case "PUT"    -> handlePut(exchange);
                case "DELETE" -> handleDelete(exchange);
                default       -> Helper.sendResponse(exchange, 405, ErrorHandler.errorMessage("Was hast du vor..!?"));
            }
        } catch (Exception e) {
            ErrorHandler.handle(exchange, e);
        }
    }
    private void handleGet(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String[] teile = path.split("/");
        final String OWNER_ID = teile.length == 3 ? teile[2] : "";

        List<Offer> offers = OfferDao.selectOffers(OWNER_ID);
        JSONArray array = new JSONArray();
        for (Offer o : offers) {
            array.put(Helper.setOfferJsonObject(o));
        }
        Helper.sendResponse(exchange, 200, array.toString());
    }
    private void handlePost(HttpExchange exchange) throws Exception {
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = Helper.getJsonObject(body);
            Offer offer = new Offer();
            Helper.setOffer(offer, json);

            int newId = OfferDao.insertOffer(offer);
            offer.setId(newId);

            exchange.getResponseHeaders().set("Location", "/offers/" + newId);
            Helper.sendResponse(exchange, 201, Helper.setOfferJsonObject(offer).toString());
        }
    }
    private void handlePut(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String[] teile = path.split("/");
        if (teile.length < 3) throw new IllegalArgumentException("Keine ID angegeben.");

        int id = Integer.parseInt(teile[2]);
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);
            Offer offer = new Offer();
            offer.setId(id);
            Helper.setOffer(offer, json);

            if (OfferDao.updateOffer(offer)) {
                Helper.sendResponse(exchange, 200, Helper.setOfferJsonObject(offer).toString());
            } else {
                throw new java.util.NoSuchElementException("Angebot mit ID " + id + " nicht gefunden.");
            }
        }
    }
    private void handleDelete(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String[] teile = path.split("/");
        if (teile.length < 3) throw new IllegalArgumentException("Keine Angebots-ID angegeben.");

        int offerId = Integer.parseInt(teile[2]);
        if (OfferDao.pseudoDeleteOffer(offerId)) {
            Helper.sendResponse(exchange, 200, "{\"message\":\"Angebot wurde erfolgreich entfernt.\"}");
        } else {
            throw new java.util.NoSuchElementException("Angebot nicht gefunden.");
        }
    }
}