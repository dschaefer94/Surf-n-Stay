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
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class OfferHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET" -> {
                String path = exchange.getRequestURI().getPath();
                String[] teile = path.split("/");
                final String OWNER_ID = teile.length == 3 ? teile[2] : "";
                try {
                    List<Offer> offers = OfferDao.selectOffers(OWNER_ID);
                    JSONArray array = new JSONArray();
                    for (Offer o : offers) {
                        array.put(Helper.setOfferJsonObject(o));
                    }
                    Helper.sendResponse(exchange, 200, array.toString());
                } catch (SQLException e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Datenbank-Server antwortet nicht.\"}");
                } catch (IllegalArgumentException e) {
                    Helper.sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (NoSuchElementException e) {
                    Helper.sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (Exception e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Unerwarteter Fehler.\"}");
                }
            }
            case "POST" -> {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject json = Helper.getJsonObject(body);
                    Offer offer = new Offer();
                    Helper.setOffer(offer, json);
                    int newId = OfferDao.insertOffer(offer);
                    offer.setId(newId);
                    exchange.getResponseHeaders().set("Location", "/offers/" + newId);
                    Helper.sendResponse(exchange, 201, Helper.setOfferJsonObject(offer).toString());
                } catch (IllegalArgumentException e) {
                    Helper.sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (SQLException e) {
                    if (e.getMessage().contains("UNIQUE constraint failed")) {
                        Helper.sendResponse(exchange, 409,
                                "{\"error\":\"Dieses Angebot existiert bereits (Kombination aus User, Standort und Datum ist doppelt).\"}");
                    } else {
                        Helper.sendResponse(exchange, 500,
                                "{\"error\":\"Ein Datenbankfehler ist aufgetreten. " + e.getMessage() + "\"}");
                    }
                } catch (Exception e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Ein unerwarteter Fehler ist aufgetreten:" + e.getMessage() + "\"}");
                }
            }
            case "PUT" -> {
                try (InputStream is = exchange.getRequestBody()) {
                    String path = exchange.getRequestURI().getPath();
                    String[] teile = path.split("/");
                    if (teile.length < 3) throw new IllegalArgumentException("Keine ID angegeben.");
                    int id = Integer.parseInt(teile[2]);
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(body);
                    Offer offer = new Offer();
                    offer.setId(id);
                    Helper.setOffer(offer, json);
                    boolean success = OfferDao.updateOffer(offer);
                    if (success) {
                        Helper.sendResponse(exchange, 200, Helper.setOfferJsonObject(offer).toString());
                    } else {
                        Helper.sendResponse(exchange, 404, "{\"error\":\"Angebot mit ID " + id + " nicht gefunden.\"}");
                    }
                } catch (NumberFormatException e) {
                    Helper.sendResponse(exchange, 400, "{\"error\":\"ID muss eine Zahl sein.\"}");
                } catch (SQLException e) {
                    if (e.getMessage().contains("FOREIGN KEY")) {
                        Helper.sendResponse(exchange, 400, "{\"error\":\"Der angegebene Besitzer existiert nicht.\"}");
                    } else if (e.getMessage().contains("UNIQUE")) {
                        Helper.sendResponse(exchange, 409, "{\"error\":\"Ein identisches Angebot existiert bereits.\"}");
                    } else {
                        Helper.sendResponse(exchange, 500, "{\"error\":\"Datenbankfehler beim Update.\"}");
                    }
                } catch (Exception e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Fehler: " + e.getMessage() + "\"}");
                }
            }
            case "DELETE" -> {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String[] teile = path.split("/");
                    if (teile.length < 3) throw new IllegalArgumentException("Keine Angebots-ID angegeben.");
                    int offerId = Integer.parseInt(teile[2]);
                    boolean success = OfferDao.pseudoDeleteOffer(offerId);
                    if (success) {
                        Helper.sendResponse(exchange, 200, "{\"message\":\"Angebot wurde erfolgreich entfernt.\"}");
                    } else {
                        Helper.sendResponse(exchange, 404, "{\"error\":\"Angebot nicht gefunden.\"}");
                    }
                } catch (NumberFormatException e) {
                    Helper.sendResponse(exchange, 400, "{\"error\":\"Ungültige ID.\"}");
                } catch (SQLException e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Datenbankfehler beim Löschen.\"}");
                }
            }
            default -> {
                util.Helper.sendResponse(exchange, 405, "{\"error\":\"Wie bist du denn hier gelandet???\"}");
            }
        }
    }
}

