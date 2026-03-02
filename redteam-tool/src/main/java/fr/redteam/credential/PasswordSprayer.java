package fr.redteam.credential;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Password spraying : tester peu de mots de passe contre beaucoup d'utilisateurs
 * pour limiter le lockout. Target = serveur cible (host). Utilise les wordlists
 * users.txt et passwords_spray.txt.
 */
public class PasswordSprayer implements Module {

    private static final String USERS_LIST_PATH = "/wordlists/users.txt";
    private static final String PASSWORDS_SPRAY_PATH = "/wordlists/passwords_spray.txt";

    @Override
    public String getName() {
        return "PasswordSprayer";
    }

    @Override
    public String getDescription() {
        return "Password spraying (quelques mots de passe sur beaucoup d'utilisateurs)";
    }

    @Override
    public void run(Target target, Report report) {
        String targetHost = target.getHost();
        if (targetHost == null || targetHost.isEmpty()) {
            targetHost = "TARGET_HOST";
        }

        List<String> users = loadResourceLines(USERS_LIST_PATH);
        List<String> passwords = loadResourceLines(PASSWORDS_SPRAY_PATH);

        if (users.isEmpty()) {
            report.addFinding("PasswordSprayer", "Liste d'utilisateurs vide ou introuvable: " + USERS_LIST_PATH);
            report.addFinding("PasswordSprayer", "Ajoutez un fichier src/main/resources/wordlists/users.txt (un login par ligne)");
            return;
        }
        if (passwords.isEmpty()) {
            report.addFinding("PasswordSprayer", "Liste de mots de passe vide ou introuvable: " + PASSWORDS_SPRAY_PATH);
            report.addFinding("PasswordSprayer", "Ajoutez un fichier src/main/resources/wordlists/passwords_spray.txt (quelques mots de passe, ex: Summer2024!, Winter2023!)");
            return;
        }

        report.addFinding("PasswordSprayer", "Cible: " + targetHost + " | Utilisateurs: " + users.size() + " | Mots de passe: " + passwords.size());

        // Pour un PoC sans dépendance réseau, on simule la structure du spray
        for (String password : passwords) {
            for (String user : users) {
                // Ici on pourrait appeler LDAP/SMB/OWA selon le type de cible
                report.addFinding("PasswordSprayer", "Spray (simulé): " + user + " / " + password + " sur " + targetHost);
            }
        }

        report.addFinding("PasswordSprayer", "Spray terminé. En production, brancher LDAP/SMB/OWA et vérifier les réponses (succès / lockout).");
    }

    private List<String> loadResourceLines(String resourcePath) {
        List<String> lines = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return lines;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String t = line.trim();
                    if (!t.isEmpty()) lines.add(t);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return lines;
    }
}
