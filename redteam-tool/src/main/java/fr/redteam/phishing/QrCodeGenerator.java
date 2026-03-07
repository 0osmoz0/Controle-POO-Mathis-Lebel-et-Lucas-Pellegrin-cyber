package fr.redteam.phishing;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Génère un QR code pointant vers une URL (via API publique, pas de dépendance).
 */
public class QrCodeGenerator implements Module {

    private static final String QR_API = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=";

    @Override
    public String getName() {
        return "QrCodeGenerator";
    }

    @Override
    public String getDescription() {
        return "Génère un QR code pointant vers une URL (phishing, etc.)";
    }

    @Override
    public void run(Target target, Report report) {
        String url = target.getHost();
        if (url == null || url.trim().isEmpty()) {
            report.addFinding("QrCodeGenerator", "Indiquez l'URL cible (ex. http://localhost:8080 ou https://ton-ngrok.ngrok.io)");
            return;
        }
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        String encoded = URLEncoder.encode(url, StandardCharsets.UTF_8);
        String qrApiUrl = QR_API + encoded;

        String outPath = "qrcode_phishing.html";
        try {
            Path output = Paths.get(outPath).toAbsolutePath();
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>QR Code</title></head><body style='font-family:sans-serif;text-align:center;padding:2rem;'>"
                    + "<h1>QR Code - Scannez avec votre téléphone</h1>"
                    + "<img src='" + qrApiUrl.replace("'", "&#39;") + "' alt='QR Code'/>"
                    + "<p>URL: " + escapeHtml(url) + "</p>"
                    + "<p><a href='" + escapeHtml(qrApiUrl) + "'>Télécharger l'image PNG</a></p>"
                    + "</body></html>";
            Files.writeString(output, html);
            report.addFinding("QrCodeGenerator", "Fichier généré: " + output);
            report.addFinding("QrCodeGenerator", "Ouvrez ce fichier HTML dans un navigateur pour afficher le QR code.");
            report.addFinding("QrCodeGenerator", "URL encodée: " + url);
        } catch (IOException e) {
            report.addFinding("QrCodeGenerator", "Erreur: " + e.getMessage());
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
