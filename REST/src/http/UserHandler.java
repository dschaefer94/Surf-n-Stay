package http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dao.UserDao;
import model.User;
import org.json.JSONObject;
import util.Helper;
import util.ErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UserHandler implements HttpHandler {
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
        if (teile.length < 3) throw new IllegalArgumentException("Keine ID angegeben.");
        int id = Integer.parseInt(teile[2]);
        User user = UserDao.selectUser(id);
        JSONObject json = new JSONObject()
                .put("username", user.getUsername())
                .put("displayName", user.getDisplayName());
        Helper.sendResponse(exchange, 200, json.toString());
    }
    private void handlePost(HttpExchange exchange) throws Exception {
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);
            if (!json.has("username") || !json.has("displayName")) {
                throw new IllegalArgumentException("Username und DisplayName werden benötigt.");
            }

            User user = new User();
            user.setUsername(json.getString("username"));
            user.setDisplayName(json.getString("displayName"));
            int newId = UserDao.insertUser(user);
            user.setId(newId);
            JSONObject response = new JSONObject()
                    .put("id", user.getId())
                    .put("username", user.getUsername());
            Helper.sendResponse(exchange, 201, response.toString());
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
            User user = new User();
            user.setId(id);
            user.setUsername(json.getString("username"));
            user.setDisplayName(json.getString("displayName"));
            if (UserDao.updateUser(user)) {
                Helper.sendResponse(exchange, 200, "{\"message\":\"User aktualisiert.\"}");
            } else {
                throw new java.util.NoSuchElementException("User mit ID " + id + " nicht gefunden.");
            }
        }
    }
    private void handleDelete(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();
        String[] teile = path.split("/");
        if (teile.length < 3) throw new IllegalArgumentException("Keine ID angegeben.");
        int id = Integer.parseInt(teile[2]);
        if (id == 1) {
            throw new IllegalArgumentException("Der Dummy-User kann nicht gelöscht werden, du Schlingel!");
        }
        UserDao.pseudoDeleteUser(id);
        Helper.sendResponse(exchange, 200, "{\"message\":\"User gelöscht, Angebote wurden archiviert.\"}");
    }
}