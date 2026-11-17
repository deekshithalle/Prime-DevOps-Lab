import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PrimeServer {
    private static void send(HttpExchange ex, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (var output = ex.getResponseBody()) { output.write(bytes); }
    }
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/health", ex -> send(ex, 200, "application/json", "{\"status\":\"ok\"}"));
        server.createContext("/api/prime", ex -> {
            if (!ex.getRequestMethod().equals("GET")) {
                send(ex, 405, "application/json", "{\"error\":\"Use GET.\"}");
                return;
            }
            String value = null;
            String query = ex.getRequestURI().getRawQuery();
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] parts = pair.split("=", 2);
                    if (parts[0].equals("number") && parts.length == 2) value = parts[1];
                }
            }
            try {
                int n = Integer.parseInt(URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8));
                send(ex, 200, "application/json", "{\"number\":" + n + ",\"prime\":" + PrimeNumber.isPrime(n) + "}");
            } catch (IllegalArgumentException e) {
                send(ex, 400, "application/json", "{\"error\":\"Enter a valid 32-bit whole number.\"}");
            }
        });
        String html = Files.readString(Path.of("web/index.html"));
        server.createContext("/", ex -> {
            if (!ex.getRequestURI().getPath().equals("/")) {
                send(ex, 404, "text/plain", "Page not found.");
                return;
            }
            send(ex, 200, "text/html", html);
        });
        server.start();
        System.out.println("Prime Lab running on http://localhost:" + port);
    }
}
