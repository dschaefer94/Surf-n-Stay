package util;

import com.sun.net.httpserver.HttpExchange;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class ErrorHandler {

    public static void handle(HttpExchange exchange, Exception e) {
        try {
            switch (e) {
                case IllegalArgumentException iae ->
                        Helper.sendResponse(exchange, 400, errorMessage(iae.getMessage()));
                case NoSuchElementException nsee ->
                        Helper.sendResponse(exchange, 404, errorMessage(nsee.getMessage()));
                case SQLException se ->
                        handleSqlException(exchange, se);
                case null, default ->
                        Helper.sendResponse(exchange, 500, errorMessage("Ein unerwarteter Fehler ist aufgetreten."));
            }
        } catch (Exception fatal) {
            System.err.println("Kritischer Fehler im ErrorHandler: " + fatal.getMessage());
        }
    }

    private static void handleSqlException(HttpExchange exchange, SQLException e) throws Exception {
        String msg = e.getMessage();
        if (msg.contains("FOREIGN KEY")) {
            Helper.sendResponse(exchange, 400, errorMessage("Der angegebene Besitzer existiert nicht."));
        } else if (msg.contains("UNIQUE")) {
            Helper.sendResponse(exchange, 409, errorMessage("Dieser Datensatz (Name oder ID) existiert bereits."));
        } else {
            Helper.sendResponse(exchange, 500, errorMessage("Datenbankfehler: " + msg));
        }
    }

    public static String errorMessage(String msg) {
        return "{\"error\":\"" + msg + "\"}";
    }
}