package http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dao.UserDao;
import model.User;
import org.json.JSONException;
import org.json.JSONObject;
import util.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class UserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET" -> {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String[] teile = path.split("/");
                    if (teile.length < 3) {
                        throw new IllegalArgumentException("Keine ID angegeben.");
                    }
                    int id = Integer.parseInt(teile[2]);
                    User user = UserDao.selectUser(id);
                    JSONObject json = new JSONObject()
                            .put("username", user.getUsername())
                            .put("displayName", user.getDisplayName());
                    Helper.sendResponse(exchange, 200, json.toString());
                } catch (NumberFormatException e) {
                    Helper.sendResponse(exchange, 400, "{\"error\":\"Die ID muss eine Zahl sein.\"}");
                } catch (IllegalArgumentException e) {
                    Helper.sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (NoSuchElementException e) {
                    Helper.sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (SQLException e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Datenbankfehler: Verbindung fehlgeschlagen.\"}");
                } catch (Exception e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Unerwarteter Serverfehler.\"}");
                }
            }

            case "POST" -> {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(body);
                    if (!json.has("username") || !json.has("displayName")) {
                        throw new IllegalArgumentException("Username und DisplayName werden benötigt.");
                    }
                    User user = new User();
                    user.setUsername(json.getString("username"));
                    user.setDisplayName(json.getString("displayName"));
                    int newId = dao.UserDao.insertUser(user);
                    user.setId(newId);
                    JSONObject response = new JSONObject()
                            .put("id", user.getId())
                            .put("username", user.getUsername());
                    util.Helper.sendResponse(exchange, 201, response.toString());
                } catch (IllegalArgumentException | JSONException e) {
                    util.Helper.sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (SQLException e) {
                    if (e.getMessage().contains("UNIQUE constraint failed")) {
                        util.Helper.sendResponse(exchange, 409, "{\"error\":\"Benutzername bereits vergeben.\"}");
                    } else {
                        util.Helper.sendResponse(exchange, 500, "{\"error\":\"Datenbankfehler: " + e.getMessage() + "\"}");
                    }
                } catch (Exception e) {
                    util.Helper.sendResponse(exchange, 500, "{\"error\":\"Serverfehler :(\"}");
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
                    User user = new User();
                    user.setId(id);
                    user.setUsername(json.getString("username"));
                    user.setDisplayName(json.getString("displayName"));
                    boolean success = dao.UserDao.updateUser(user);
                    if (success) {
                        util.Helper.sendResponse(exchange, 200, "{\"message\":\"User aktualisiert.\"}");
                    } else {
                        util.Helper.sendResponse(exchange, 404, "{\"error\":\"User mit ID " + id + " nicht gefunden.\"}");
                    }
                } catch (NumberFormatException e) {
                    util.Helper.sendResponse(exchange, 400, "{\"error\":\"Ungültige ID-Formatierung.\"}");
                } catch (JSONException | IllegalArgumentException e) {
                    util.Helper.sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (SQLException e) {
                    if (e.getMessage().contains("UNIQUE constraint failed")) {
                        util.Helper.sendResponse(exchange, 409, "{\"error\":\"Benutzername bereits vergeben.\"}");
                    } else {
                        util.Helper.sendResponse(exchange, 500, "{\"error\":\"Datenbankfehler: " + e.getMessage() + "\"}");
                    }
                } catch (Exception e) {
                    util.Helper.sendResponse(exchange, 500, "{\"error\":\"Serverfehler :(\"}");
                }
            }
            case "DELETE" -> {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String[] teile = path.split("/");
                    if (teile.length < 3) throw new IllegalArgumentException("Keine ID angegeben.");
                    int id = Integer.parseInt(teile[2]);
                    if (id == 1)
                        throw new IllegalArgumentException("Der Dummy-User kann nicht gelöscht werden, du Schlingel!");
                    dao.UserDao.pseudoDeleteUser(id);
                    util.Helper.sendResponse(exchange, 200, "{\"message\":\"User gelöscht, Angebote wurden archiviert.\"}");
                } catch (IllegalArgumentException e) {
                    util.Helper.sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                } catch (SQLException e) {
                    util.Helper.sendResponse(exchange, 404, "{\"error\":\"User nicht gefunden oder Datenbankfehler.\"}");
                } catch (Exception e) {
                    util.Helper.sendResponse(exchange, 500, "{\"error\":\"Serverfehler :(\"}");
                }
            }
            default -> {
                util.Helper.sendResponse(exchange, 405, "{\"error\":\"Wie bist du denn hier gelandet???\"}");
            }
        }
    }
}