package fr.redteam.payload;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.util.ArrayList;
import java.util.List;

public class DownloadServer implements Module {

    private final List<String> servePaths = new ArrayList<>();

    @Override
    public String getName() {
        return "DownloadServer";
    }

    @Override
    public String getDescription() {
        return "Serveur de téléchargement (APK, binaires)";
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

        report.addFinding("DownloadServer", "Serveur configuré sur " + host + ":" + port);
        if (servePaths.isEmpty()) {
            report.addFinding("DownloadServer", "Aucun fichier à servir. Utiliser addServePath(path) pour ajouter des APK/binaires.");
        } else {
            for (String path : servePaths) {
                report.addFinding("DownloadServer", "Fichier à servir: " + path + " -> http://" + (host.equals("0.0.0.0") ? "IP_PUBLIQUE" : host) + ":" + port + "/download/" + path);
            }
        }
    }

    public void addServePath(String path) {
        if (path != null && !path.trim().isEmpty()) {
            servePaths.add(path.trim());
        }
    }
    
    public List<String> getServePaths() {
        return new ArrayList<>(servePaths);
    }

    public void clearServePaths() {
        servePaths.clear();
    }
}
