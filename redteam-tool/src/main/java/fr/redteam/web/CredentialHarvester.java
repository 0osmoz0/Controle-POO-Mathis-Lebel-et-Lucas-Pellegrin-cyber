package fr.redteam.web;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.util.ArrayList;
import java.util.List;

public class CredentialHarvester implements Module {

    private final List<String> harvested = new ArrayList<>();

    @Override
    public String getName() {
        return "CredentialHarvester";
    }

    @Override
    public String getDescription() {
        return "Harvest des identifiants (formulaire phishing)";
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

        report.addFinding("CredentialHarvester", "Endpoint harvest configuré sur " + host + ":" + port + " (POST /harvest, champs: login, password)");
        report.addFinding("CredentialHarvester", "En production: écouter les POST, appeler recordCredential(login, password) et rediriger la victime.");
        if (!harvested.isEmpty()) {
            for (String line : harvested) {
                report.addFinding("CredentialHarvester", "Récolté: " + line);
            }
        }
    }

    public void recordCredential(String login, String password) {
        if (login != null) {
            harvested.add(login + ":" + (password != null ? password : ""));
        }
    }

    public List<String> getHarvested() {
        return new ArrayList<>(harvested);
    }

    public void clearHarvested() {
        harvested.clear();
    }
}
