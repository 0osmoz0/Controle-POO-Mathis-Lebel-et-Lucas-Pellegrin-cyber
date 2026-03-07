package fr.redteam.phishing;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Génère un QR code pointant vers une URL (API publique + ImageIO pour affichage CLI).
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

    /**
     * Génère un QR code via l'API et le retourne en ASCII/Unicode pour affichage dans le terminal.
     * Utilise ImageIO (JDK) et l'API qrserver.com - aucune dépendance externe.
     * @param url URL à encoder
     * @return chaîne multiligne représentant le QR (blocs Unicode █ et espace), ou null en cas d'erreur
     */
    public static String toAsciiArt(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        try {
            // Image 120x120 : ~5 px/module pour un QR 21x21, permet un échantillonnage précis
            String apiUrl = QR_API.replace("300x300", "120x120") + URLEncoder.encode(url, StandardCharsets.UTF_8);
            try (InputStream in = new URL(apiUrl).openStream()) {
                BufferedImage img = ImageIO.read(in);
                if (img == null) return null;
                int w = img.getWidth();
                int h = img.getHeight();
                // 1 module QR ≈ 5 pixels ; échantillonner le centre de chaque bloc pour éviter le flou
                int block = 5;
                StringBuilder sb = new StringBuilder();
                for (int y = block / 2; y < h; y += block) {
                    for (int x = block / 2; x < w; x += block) {
                        int rgb = img.getRGB(x, y);
                        int gray = ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
                        boolean black = gray < 400; // seuil pour noir (0) vs blanc (765)
                        sb.append(black ? "██" : "  ");
                    }
                    sb.append("\n");
                }
                return sb.toString();
            }
        } catch (IOException e) {
            return null;
        }
    }
}
