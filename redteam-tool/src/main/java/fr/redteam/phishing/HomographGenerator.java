package fr.redteam.phishing;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.IOException;
import java.net.IDN;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Génère des domaines homographes (caractères Unicode ressemblant à des lettres ASCII).
 * Génère un fichier avec toutes les variantes + Punycode pour enregistrement.
 */
public class HomographGenerator implements Module {

    /** Paires ASCII → Unicode (Cyrillic/Grec) visuellement identiques */
    private static final String[][] REPLACEMENTS = {
        {"a", "а"},  // Cyrillic а
        {"o", "о"},  // Cyrillic о
        {"e", "е"},  // Cyrillic е
        {"p", "р"},  // Cyrillic р
        {"c", "с"},  // Cyrillic с
        {"x", "х"},  // Cyrillic х
        {"y", "у"},  // Cyrillic у
        {"i", "і"},  // Cyrillic і
        {"o", "ο"},  // Greek omicron
    };

    @Override
    public String getName() {
        return "HomographGenerator";
    }

    @Override
    public String getDescription() {
        return "Génère des domaines homographes (lookalike Unicode) avec fichier Punycode";
    }

    @Override
    public void run(Target target, Report report) {
        String domain = target.getHost();
        if (domain == null || domain.trim().isEmpty()) {
            report.addFinding("HomographGenerator", "Indiquez le domaine cible (ex. google.com, netflix.com)");
            return;
        }
        domain = domain.trim().toLowerCase().replaceAll("^https?://", "").replaceAll("/.*", "");

        Set<String> variants = generateHomographs(domain);
        List<String> list = new ArrayList<>(variants);
        list.remove(domain);

        String safeName = domain.replace(".", "_");
        String outPath = "homograph_" + safeName + ".txt";
        Path file = Paths.get(outPath).toAbsolutePath();

        StringBuilder content = new StringBuilder();
        content.append("# Domaines homographes pour ").append(domain).append("\n");
        content.append("# Utilisez le Punycode pour enregistrer le domaine chez un registrar\n");
        content.append("# Format: variante | Punycode | URL\n\n");

        for (String v : list) {
            String punycode = toPunycode(v);
            content.append(v).append(" | ").append(punycode).append(" | https://").append(punycode).append("\n");
        }

        try {
            Files.writeString(file, content.toString());
            report.addFinding("HomographGenerator", "Fichier généré: " + file);
            report.addFinding("HomographGenerator", "Variants: " + list.size() + " (voir le fichier pour Punycode)");
        } catch (IOException e) {
            report.addFinding("HomographGenerator", "Erreur fichier: " + e.getMessage());
        }
    }

    /** Retourne le chemin du fichier généré, ou null. Utilisé par l'assistant phishing. */
    public static String runAndGetFilePath(String domain) {
        if (domain == null || domain.trim().isEmpty()) return null;
        domain = domain.trim().toLowerCase().replaceAll("^https?://", "").replaceAll("/.*", "");
        Set<String> variants = new HomographGenerator().generateHomographs(domain);
        List<String> list = new ArrayList<>(variants);
        list.remove(domain);
        if (list.isEmpty()) return null;

        String safeName = domain.replace(".", "_");
        Path file = Paths.get("homograph_" + safeName + ".txt").toAbsolutePath();
        StringBuilder content = new StringBuilder();
        content.append("# Domaines homographes pour ").append(domain).append("\n");
        content.append("# Punycode = format pour enregistrement\n\n");
        for (String v : list) {
            content.append(v).append(" | ").append(toPunycode(v)).append("\n");
        }
        try {
            Files.writeString(file, content.toString());
            return file.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static String toPunycode(String domain) {
        try {
            return IDN.toASCII(domain);
        } catch (Exception e) {
            return domain;
        }
    }

    private Set<String> generateHomographs(String domain) {
        Set<String> result = new LinkedHashSet<>();
        result.add(domain);

        for (int i = 0; i < REPLACEMENTS.length; i++) {
            String ascii = REPLACEMENTS[i][0];
            String unicode = REPLACEMENTS[i][1];
            if (domain.contains(ascii)) {
                String variant = domain.replace(ascii, unicode);
                result.add(variant);
            }
        }

        for (int i = 0; i < REPLACEMENTS.length; i++) {
            for (int j = i + 1; j < REPLACEMENTS.length; j++) {
                String a1 = REPLACEMENTS[i][0], u1 = REPLACEMENTS[i][1];
                String a2 = REPLACEMENTS[j][0], u2 = REPLACEMENTS[j][1];
                if (!a1.equals(a2) && domain.contains(a1) && domain.contains(a2)) {
                    String v = domain.replace(a1, u1).replace(a2, u2);
                    result.add(v);
                }
            }
        }
        return result;
    }
}
