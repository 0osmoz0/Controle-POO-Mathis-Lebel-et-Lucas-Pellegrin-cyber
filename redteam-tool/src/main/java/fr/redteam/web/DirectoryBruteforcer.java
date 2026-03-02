package fr.redteam.web;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DirectoryBruteforcer implements Module {

    private static final String WORDLIST_PATH = "/wordlists/directories.txt";

    @Override
    public String getName() {
        return "DirectoryBruteforcer";
    }

    @Override
    public String getDescription() {
        return "Bruteforce de répertoires web (wordlist)";
    }

    @Override
    public void run(Target target, Report report) {
        String base = target.getHost();
        int port = target.getPort();
        if (base == null || base.isEmpty()) {
            report.addFinding("DirectoryBruteforcer", "URL de base non renseignée (Target.host)");
            return;
        }

        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            base = "http://" + base;
        }
        if (port > 0 && port != 80 && port != 443) {
            try {
                URL u = new URL(base);
                String pathPart = u.getPath() != null && !u.getPath().isEmpty() ? u.getPath() : "";
                base = u.getProtocol() + "://" + u.getHost() + ":" + port + pathPart;
            } catch (Exception e) {
                base = base + ":" + port;
            }
        }

        List<String> paths = loadWordlist();
        if (paths.isEmpty()) {
            paths = getDefaultPaths();
            report.addFinding("DirectoryBruteforcer", "Wordlist vide, utilisation de la liste par défaut (" + paths.size() + " chemins).");
        }

        report.addFinding("DirectoryBruteforcer", "Cible: " + base + " | Chemins à tester: " + paths.size());

        for (String path : paths) {
            String url = base.endsWith("/") ? base + path.replaceFirst("^/", "") : base + (path.startsWith("/") ? path : "/" + path);
            int code = fetchStatus(url);
            if (code >= 200 && code < 400) {
                report.addFinding("DirectoryBruteforcer", "TROUVÉ [" + code + "]: " + url);
            }
        }
    }

    private int fetchStatus(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code;
        } catch (Exception e) {
            return -1;
        }
    }

    private List<String> loadWordlist() {
        List<String> lines = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(WORDLIST_PATH)) {
            if (is == null) return lines;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String t = line.trim();
                    if (!t.isEmpty()) lines.add(t);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return lines;
    }

    private List<String> getDefaultPaths() {
        List<String> d = new ArrayList<>();
        d.add("/");
        d.add("/admin");
        d.add("/login");
        d.add("/wp-admin");
        d.add("/.git");
        d.add("/backup");
        d.add("/api");
        d.add("/config");
        return d;
    }
}
