import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class REST {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

//        server.createContext("/api/test", exchange -> {
//            String response = "Hallo von deiner Java API!";
//            exchange.sendResponseHeaders(200, response.length());
//            exchange.getResponseBody().write(response.getBytes());
//            exchange.getResponseBody().close();
//        });

        server.createContext("/user", new UserHandler());

        server.start();
        System.out.println("Server läuft auf http://localhost:8080/user");
//curl.exe -X POST http://localhost:8080/api/user `
//     -H "Content-Type: application/json" `
//     -d '{\"name\": \"Max\", \"email\": \"max@test.de\"}'
    }
}
