package fr.redteam.credential;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Analyse la force d'un mot de passe (entropie, motifs courants, présence en wordlist).
 */
public class PasswordStrengthAnalyzer implements Module {

    private static final String WORDLIST_PATH = "/wordlists/passwords.txt";
    private static final Set<String> COMMON_PATTERNS = Set.of(
            "123", "abc", "qwe", "password", "admin", "letmein", "welcome",
            "monkey", "dragon", "master", "sunshine", "princess", "football",
            "iloveyou", "admin123", "password1", "qwerty", "123456"
    );

    @Override
    public String getName() {
        return "PasswordStrengthAnalyzer";
    }

    @Override
    public String getDescription() {
        return "Analyse la force d'un mot de passe (entropie, motifs, wordlist)";
    }

    @Override
    public void run(Target target, Report report) {
        String password = target.getHost();
        if (password == null || password.isEmpty()) {
            report.addFinding("PasswordStrengthAnalyzer", "Indiquez le mot de passe à analyser");
            return;
        }

        report.addFinding("PasswordStrengthAnalyzer", "Longueur: " + password.length() + " caractères");
        report.addFinding("PasswordStrengthAnalyzer", "Entropie (bits): " + String.format("%.1f", calculateEntropy(password)));

        int score = 0;
        if (password.length() >= 8) { score += 1; report.addFinding("PasswordStrengthAnalyzer", "✓ Longueur >= 8"); }
        else report.addFinding("PasswordStrengthAnalyzer", "✗ Longueur < 8 (recommandé: 12+)");
        if (password.matches(".*[A-Z].*")) { score += 1; report.addFinding("PasswordStrengthAnalyzer", "✓ Contient majuscule"); }
        else report.addFinding("PasswordStrengthAnalyzer", "✗ Pas de majuscule");
        if (password.matches(".*[a-z].*")) { score += 1; report.addFinding("PasswordStrengthAnalyzer", "✓ Contient minuscule"); }
        if (password.matches(".*[0-9].*")) { score += 1; report.addFinding("PasswordStrengthAnalyzer", "✓ Contient chiffre"); }
        else report.addFinding("PasswordStrengthAnalyzer", "✗ Pas de chiffre");
        if (password.matches(".*[^A-Za-z0-9].*")) { score += 1; report.addFinding("PasswordStrengthAnalyzer", "✓ Contient caractère spécial"); }
        else report.addFinding("PasswordStrengthAnalyzer", "✗ Pas de caractère spécial");

        for (String p : COMMON_PATTERNS) {
            if (password.toLowerCase().contains(p)) {
                report.addFinding("PasswordStrengthAnalyzer", "⚠ Motif courant détecté: " + p);
                score = Math.max(0, score - 1);
            }
        }

        List<String> wordlist = loadWordlist();
        if (wordlist.contains(password.toLowerCase())) {
            report.addFinding("PasswordStrengthAnalyzer", "⚠ CRITIQUE: Mot de passe dans la wordlist (très faible)");
            score = 0;
        }

        String strength = score <= 2 ? "Faible" : score <= 4 ? "Moyen" : "Fort";
        report.addFinding("PasswordStrengthAnalyzer", "Score: " + score + "/5 → " + strength);
    }

    private double calculateEntropy(String s) {
        Set<Character> chars = new HashSet<>();
        for (char c : s.toCharArray()) chars.add(c);
        int pool = 0;
        if (s.matches(".*[a-z].*")) pool += 26;
        if (s.matches(".*[A-Z].*")) pool += 26;
        if (s.matches(".*[0-9].*")) pool += 10;
        if (s.matches(".*[^A-Za-z0-9].*")) pool += 32;
        if (pool == 0) pool = 26;
        return s.length() * (Math.log(pool) / Math.log(2));
    }

    private List<String> loadWordlist() {
        List<String> list = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(WORDLIST_PATH)) {
            if (is == null) return list;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String t = line.trim().toLowerCase();
                    if (!t.isEmpty()) list.add(t);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return list;
    }
}
