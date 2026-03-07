package fr.redteam.recon;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Vérifie si des sous-domaines peuvent être victimes d'un takeover
 * (CNAME vers des services supprimés : GitHub Pages, Heroku, etc.).
 */
public class SubdomainTakeoverChecker implements Module {

    private static final String SUBS_PATH = "/wordlists/subdomains.txt";
    private static final String[] KNOWN_TAKEOVER_SIGNATURES = {
            "There isn't a GitHub Pages site here",
            "No such app",
            "Heroku | No such app",
            "The request could not be satisfied",
            "Repository not found",
            "Sorry, We Couldn't Find That Page",
            "You're Almost Done"
    };

    @Override
    public String getName() {
        return "SubdomainTakeoverChecker";
    }

    @Override
    public String getDescription() {
        return "Détecte les sous-domaines potentiellement vulnérables au takeover";
    }

    @Override
    public void run(Target target, Report report) {
        String domain = target.getHost();
        if (domain == null || domain.trim().isEmpty()) {
            report.addFinding("SubdomainTakeoverChecker", "Indiquez le domaine cible (ex. example.com)");
            return;
        }
        domain = domain.trim().toLowerCase().replaceAll("^https?://", "").replaceAll("/.*", "");

        List<String> subdomains = loadSubdomains();
        if (subdomains.isEmpty()) {
            subdomains = List.of("www", "mail", "dev", "staging", "test", "api", "admin", "blog", "cdn", "ftp");
        }

        report.addFinding("SubdomainTakeoverChecker", "Scan de " + domain + " (" + subdomains.size() + " sous-domaines)");

        int checked = 0;
        for (String sub : subdomains) {
            String host = sub + "." + domain;
            try {
                InetAddress addr = InetAddress.getByName(host);
                String ip = addr.getHostAddress();
                report.addFinding("SubdomainTakeoverChecker", host + " → " + ip + " (résolu)");
                String body = fetchBody("http://" + host, 3000);
                if (body != null) {
                    for (String sig : KNOWN_TAKEOVER_SIGNATURES) {
                        if (body.contains(sig)) {
                            report.addFinding("SubdomainTakeoverChecker", "⚠ POTENTIEL TAKEOVER: " + host + " (signature: " + sig.substring(0, Math.min(40, sig.length())) + "...)");
                            break;
                        }
                    }
                }
                checked++;
            } catch (Exception e) {
                report.addFinding("SubdomainTakeoverChecker", host + " → non résolu ou erreur");
            }
        }
        report.addFinding("SubdomainTakeoverChecker", "Scan terminé. " + checked + " sous-domaines vérifiés.");
    }

    private String fetchBody(String urlStr, int timeout) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("User-Agent", "RedTeam-SubdomainChecker/1.0");
            conn.connect();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null && sb.length() < 5000) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> loadSubdomains() {
        List<String> list = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(SUBS_PATH)) {
            if (is == null) return list;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String t = line.trim();
                    if (!t.isEmpty() && !t.startsWith("#")) list.add(t);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return list;
    }
}
