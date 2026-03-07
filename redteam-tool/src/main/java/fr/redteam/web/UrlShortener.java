package fr.redteam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;
import fr.redteam.util.Ansi;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Serveur URL courtes avec tracking des clics (IP, User-Agent, date).
 */
public class UrlShortener implements Module {

    private static final int DEFAULT_PORT = 9090;
    private static final Map<String, RedirectEntry> redirects = new ConcurrentHashMap<>();
    private static final List<ClickLog> clickLogs = new ArrayList<>();
    private static HttpServer server;

    @Override
    public String getName() {
        return "UrlShortener";
    }

    @Override
    public String getDescription() {
        return "URL courtes avec tracking des clics (IP, User-Agent)";
    }

    @Override
    public void run(Target target, Report report) {
        String destUrl = target.getHost();
        if (destUrl == null || destUrl.trim().isEmpty()) {
            report.addFinding("UrlShortener", "Indiquez l'URL de destination (ex. http://localhost:8080 ou https://phishing.ngrok.io)");
            return;
        }
        destUrl = destUrl.trim();
        if (!destUrl.startsWith("http://") && !destUrl.startsWith("https://")) {
            destUrl = "http://" + destUrl;
        }

        int port = target.getPort() > 0 ? target.getPort() : DEFAULT_PORT;
        String id = "r" + System.currentTimeMillis() % 10000;
        redirects.put(id, new RedirectEntry(destUrl));

        report.addFinding("UrlShortener", "URL courte: http://127.0.0.1:" + port + "/" + id);
        report.addFinding("UrlShortener", "Redirige vers: " + destUrl);
        report.addFinding("UrlShortener", "Démarrez le serveur avec startUrlShortenerServer() pour activer le tracking.");
    }

    public static void startServer(int port, String destUrl) throws IOException {
        String id = "r" + System.currentTimeMillis() % 10000;
        redirects.put(id, new RedirectEntry(destUrl));

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath().replaceFirst("^/", "");
            if (path.isEmpty()) path = id;
            RedirectEntry entry = redirects.get(path);
            if (entry != null) {
                String ip = ex.getRemoteAddress().getAddress().getHostAddress();
                String ua = ex.getRequestHeaders().getFirst("User-Agent");
                if (ua == null) ua = "";
                clickLogs.add(new ClickLog(ip, ua, path, System.currentTimeMillis()));
                System.out.println(Ansi.GREEN + "  ✓ [Click] " + Ansi.RESET + Ansi.cyan(ip) + Ansi.dim(" → ") + path + Ansi.dim(" | ") + ua.substring(0, Math.min(50, ua.length())) + (ua.length() > 50 ? "..." : ""));
                ex.getResponseHeaders().add("Location", entry.destUrl);
                ex.sendResponseHeaders(302, -1);
                ex.close();
            } else {
                try {
                    String body = "404 - Short URL not found";
                    ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    ex.sendResponseHeaders(404, body.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream out = ex.getResponseBody()) {
                        out.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (IOException ignored) {}
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public static void stopServer() {
        if (server != null) server.stop(0);
    }

    public static List<ClickLog> getClickLogs() {
        return new ArrayList<>(clickLogs);
    }

    public static String getShortUrl(int port) {
        if (redirects.isEmpty()) return "http://127.0.0.1:" + port;
        String id = redirects.keySet().iterator().next();
        return "http://127.0.0.1:" + port + "/" + id;
    }

    record RedirectEntry(String destUrl) {}
    public record ClickLog(String ip, String userAgent, String path, long timestamp) {}
}
