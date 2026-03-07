package fr.redteam.phishing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Génère un QR code pointant vers une URL (ex. page phishing).
 */
public class QrCodeGenerator implements Module {

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

        String outPath = "qrcode_phishing.png";
        try {
            Path output = Paths.get(outPath).toAbsolutePath();
            generateQrCode(url, output, 300, 300);
            report.addFinding("QrCodeGenerator", "QR code généré: " + output);
            report.addFinding("QrCodeGenerator", "URL encodée: " + url);
            report.addFinding("QrCodeGenerator", "Utilisez ce QR pour des tests physiques (stickers, affiches).");
        } catch (Exception e) {
            report.addFinding("QrCodeGenerator", "Erreur: " + e.getMessage());
        }
    }

    public Path generateQrCode(String url, Path outputPath, int width, int height) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", outputPath);
        return outputPath;
    }
}
