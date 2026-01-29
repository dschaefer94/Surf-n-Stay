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

public class UserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET" -> {
                try {
                    User user = UserDao.selectUser();
                    JSONObject json = new JSONObject()
                            .put("username", user.getUsername())
                            .put("displayName", user.getDisplayName());
                    Helper.sendResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    Helper.sendResponse(exchange, 500, "{\"error\":\"Serverfehler :(\"}");
                }
            }
            case "POST" -> {
                try {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(body);
                    User user = new User(json.getString("username"), json.getString("displayName"));
                    dao.UserDao.insertUser(user);
                } catch (JSONException e) {
                    util.Helper.sendResponse(exchange, 400, "Invalide JSON :(");
                } catch (Exception e) {
                    util.Helper.sendResponse(exchange, 500, "Serverfehler :(");
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
}