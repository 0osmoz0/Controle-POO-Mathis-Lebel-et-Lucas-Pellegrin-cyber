package fr.redteam.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Récupère l'URL publique ngrok via l'API locale (port 4040).
 * Compatible ngrok 2 et 3 (tunnels + endpoints).
 */
public final class NgrokHelper {

    private static final String NGROK_TUNNELS = "http://127.0.0.1:4040/api/tunnels";
    private static final String NGROK_ENDPOINTS = "http://127.0.0.1:4040/api/endpoints";
    private static final Pattern PUBLIC_URL = Pattern.compile("\"public_url\":\"(https?://[^\"]+)\"");
    private static final Pattern ENDPOINT_URL = Pattern.compile("\"url\":\"(https?://[^\"]+)\"");

    private NgrokHelper() {}

    /** Chemins possibles pour ngrok (Java n'hérite pas toujours du PATH du shell). */
    private static String findNgrokPath() {
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(Pattern.quote(File.pathSeparator))) {
                Path exe = Paths.get(dir, "ngrok");
                if (Files.isExecutable(exe)) return exe.toString();
            }
        }
        String[] candidates = {"/opt/homebrew/bin/ngrok", "/usr/local/bin/ngrok", "ngrok"};
        for (String c : candidates) {
            if ("ngrok".equals(c)) return c;
            if (Files.isExecutable(Paths.get(c))) return c;
        }
        return "ngrok";
    }

    /**
     * Démarre ngrok en arrière-plan pour exposer le port donné.
     * @return le Process ngrok, ou null si échec
     */
    public static Process startNgrok(int port) {
        String ngrokPath = findNgrokPath();
        try {
            ProcessBuilder pb = new ProcessBuilder(ngrokPath, "http", String.valueOf(port));
            pb.redirectErrorStream(true);
            return pb.start();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère l'URL publique du premier tunnel/endpoint ngrok actif.
     * Supporte /api/tunnels (public_url) et /api/endpoints (url) pour ngrok 3.
     */
    public static String getPublicUrl(int maxRetries, int delayMs) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(delayMs);
                String u = fetchFromTunnels();
                if (u != null) return u;
                u = fetchFromEndpoints();
                if (u != null) return u;
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static String fetchFromTunnels() throws Exception {
        return fetchUrl(NGROK_TUNNELS, PUBLIC_URL, true);
    }

    private static String fetchFromEndpoints() throws Exception {
        return fetchUrl(NGROK_ENDPOINTS, ENDPOINT_URL, true);
    }

    private static String fetchUrl(String apiUrl, Pattern pattern, boolean preferHttps) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);
        conn.connect();
        if (conn.getResponseCode() != 200) return null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            String json = sb.toString();
            Matcher m = pattern.matcher(json);
            while (m.find()) {
                String u = m.group(1);
                if (preferHttps && u.startsWith("https://")) return u;
            }
            m.reset();
            return m.find() ? m.group(1) : null;
        }
    }
}
