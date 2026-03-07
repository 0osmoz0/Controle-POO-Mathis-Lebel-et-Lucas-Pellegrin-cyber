package fr.redteam.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Récupère l'URL publique ngrok via l'API locale (port 4040).
 */
public final class NgrokHelper {

    private static final String NGROK_API = "http://127.0.0.1:4040/api/tunnels";
    private static final Pattern URL_PATTERN = Pattern.compile("\"public_url\":\"(https?://[^\"]+)\"");

    private NgrokHelper() {}

    /**
     * Démarre ngrok en arrière-plan pour exposer le port donné.
     * @return le Process ngrok, ou null si échec
     */
    public static Process startNgrok(int port) {
        try {
            ProcessBuilder pb = new ProcessBuilder("ngrok", "http", String.valueOf(port));
            pb.redirectErrorStream(true);
            return pb.start();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Récupère l'URL publique du premier tunnel ngrok actif.
     * @param maxRetries nombre de tentatives
     * @param delayMs délai entre chaque tentative (ms)
     * @return l'URL https ou null si ngrok n'est pas disponible
     */
    public static String getPublicUrl(int maxRetries, int delayMs) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(delayMs);
                URL url = new URL(NGROK_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) sb.append(line);
                        String json = sb.toString();
                        Matcher m = URL_PATTERN.matcher(json);
                        while (m.find()) {
                            String u = m.group(1);
                            if (u.startsWith("https://")) return u;
                        }
                        m.reset();
                        if (m.find()) return m.group(1);
                    }
                }
            } catch (Exception e) {
                // retry
            }
        }
        return null;
    }
}
