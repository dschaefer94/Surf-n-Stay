package server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import db.Database;
import http.UserHandler;
import http.OfferHandler;

public class REST {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/user", new UserHandler());
        server.createContext("/offer", new OfferHandler());
        Database.initialize();
        server.start();
        System.out.println("Server läuft auf http://localhost:8080/offer");
    }
}
