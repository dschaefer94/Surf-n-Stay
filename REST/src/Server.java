import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import db.Database;
import http.UserHandler;
import http.OfferHandler;
import util.Helper;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/user", new UserHandler());
        server.createContext("/offers", new OfferHandler());
        server.createContext("/", ex ->
                Helper.sendResponse(ex, 404, "{\"error\":\"Voll verirrt!\"}")
        );
        Database.initialize();
        server.start();
        System.out.println("Server läuft auf http://localhost:8080/offers");
    }
}
