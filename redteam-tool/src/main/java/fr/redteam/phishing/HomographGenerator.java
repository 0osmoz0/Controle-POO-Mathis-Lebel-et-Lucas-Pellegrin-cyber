package fr.redteam.phishing;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Génère des domaines homographes (caractères Unicode ressemblant à des lettres ASCII).
 */
public class HomographGenerator implements Module {

    @Override
    public String getName() {
        return "HomographGenerator";
    }

    @Override
    public String getDescription() {
        return "Génère des domaines homographes (lookalike Unicode)";
    }

    @Override
    public void run(Target target, Report report) {
        String domain = target.getHost();
        if (domain == null || domain.trim().isEmpty()) {
            report.addFinding("HomographGenerator", "Indiquez le domaine cible (ex. google.com, netflix.com)");
            return;
        }
        domain = domain.trim().toLowerCase().replaceAll("^https?://", "").replaceAll("/.*", "");

        List<String> variants = generateHomographs(domain);
        report.addFinding("HomographGenerator", "Domaine cible: " + domain);
        report.addFinding("HomographGenerator", "Variants homographes générés: " + variants.size());
        int shown = 0;
        for (String v : variants) {
            if (!v.equals(domain) && shown < 15) {
                report.addFinding("HomographGenerator", "  → " + v);
                shown++;
            }
        }
        if (variants.size() > 15) {
            report.addFinding("HomographGenerator", "  ... et " + (variants.size() - 15) + " autres");
        }
        report.addFinding("HomographGenerator", "Utilisation: enregistrer ces domaines pour des tests de phishing (avec autorisation).");
    }

    private List<String> generateHomographs(String domain) {
        List<String> result = new ArrayList<>();
        result.add(domain);

        // Remplacements simples : a→а, o→о, e→е, p→р, c→с, x→х, y→у
        String[] replacements = {"a", "а", "o", "о", "e", "е", "p", "р", "c", "с", "x", "х", "y", "у", "i", "і"};
        for (int i = 0; i < replacements.length; i += 2) {
            String ascii = replacements[i];
            String unicode = replacements[i + 1];
            if (domain.contains(ascii)) {
                String variant = domain.replace(ascii, unicode);
                if (!result.contains(variant)) result.add(variant);
            }
        }

        // Combinaisons avec 2 remplacements
        for (int i = 0; i < replacements.length; i += 2) {
            for (int j = i + 2; j < replacements.length; j += 2) {
                String a1 = replacements[i], u1 = replacements[i + 1];
                String a2 = replacements[j], u2 = replacements[j + 1];
                if (domain.contains(a1) && domain.contains(a2)) {
                    String v = domain.replace(a1, u1).replace(a2, u2);
                    if (!result.contains(v)) result.add(v);
                }
            }
        }
        return result;
    }
}
