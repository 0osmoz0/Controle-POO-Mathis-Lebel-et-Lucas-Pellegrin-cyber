package fr.redteam.phishing;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.net.IDN;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Génère des domaines homographes (caractères Unicode ressemblant à des lettres ASCII).
 * Inclut le Punycode (xn--...) pour enregistrement et utilisation réelle.
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
        {"o", "ο"},  // Greek omicron (2e entrée pour variantes)
    };

    @Override
    public String getName() {
        return "HomographGenerator";
    }

    @Override
    public String getDescription() {
        return "Génère des domaines homographes (lookalike Unicode) avec Punycode";
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

        report.addFinding("HomographGenerator", "Domaine cible: " + domain);
        report.addFinding("HomographGenerator", "Variants homographes: " + list.size());
        report.addFinding("HomographGenerator", "Utilisez le Punycode (xn--...) pour enregistrer le domaine.");
        report.addFinding("HomographGenerator", "");

        int shown = 0;
        for (String v : list) {
            if (shown >= 10) break;
            String punycode = toPunycode(v);
            report.addFinding("HomographGenerator", "  → " + v);
            report.addFinding("HomographGenerator", "    Punycode: " + punycode);
            shown++;
        }
        if (list.size() > 10) {
            report.addFinding("HomographGenerator", "  ... et " + (list.size() - 10) + " autres");
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
