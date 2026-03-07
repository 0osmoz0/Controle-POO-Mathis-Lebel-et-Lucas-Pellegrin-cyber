package fr.redteam.web;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

public class PhishingServer implements Module {

    private final PhishingPageGenerator pageGenerator = new PhishingPageGenerator();

    @Override
    public String getName() {
        return "PhishingServer";
    }

    @Override
    public String getDescription() {
        return "Serveur de pages de phishing (login) et récolte des identifiants";
    }

    @Override
    public void run(Target target, Report report) {
        String host = target.getHost();
        int port = target.getPort();
        if (host == null || host.isEmpty()) {
            host = "0.0.0.0";
        }
        if (port <= 0) {
            port = 8080;
        }

        String formAction = "/harvest";
        String html = pageGenerator.generate("Connexion - Veuillez vous authentifier", formAction, "Accès réservé.");

        report.addFinding("PhishingServer", "Serveur phishing configuré sur " + host + ":" + port);
        report.addFinding("PhishingServer", "Page login générée (template), formulaire POST vers " + formAction);
        if (html.isEmpty()) {
            report.addFinding("PhishingServer", "Attention: template login-phishing.html introuvable dans resources/templates/");
        } else {
            report.addFinding("PhishingServer", "Template chargé, " + html.length() + " octets. Démarrer un HttpServer pour servir GET / et POST " + formAction);
        }
    }

    
    public String getLoginPageHtml(String title, String formAction) {
        return pageGenerator.generate(title, formAction, "Accès sécurisé.");
    }
}
