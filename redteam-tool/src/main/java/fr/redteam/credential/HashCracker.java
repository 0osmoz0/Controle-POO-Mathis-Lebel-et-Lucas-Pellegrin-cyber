package fr.redteam.credential;

import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class HashCracker implements Module {

    private static final String ROCKYOU_RESOURCE = "/wordlists/rockyou.txt";
    private static final String ROCKYOU_SUBSET = "/wordlists/rockyou-subset.txt";

    @Override
    public String getName() {
        return "HashCracker";
    }

    @Override
    public String getDescription() {
        return "Crack de hashes (MD5, SHA-1) par wordlist";
    }

    @Override
    public void run(Target target, Report report) {
        String input = target.getHost();
        if (input == null || input.isEmpty()) {
            report.addFinding("HashCracker", "Indiquez un hash MD5/SHA1 ou le chemin d'un fichier de hashes (un par ligne).");
            return;
        }
        input = input.trim();

        List<String> hashes = loadHashesFromFile(input);
        if (hashes.isEmpty() && looksLikeHash(input)) {
            hashes = List.of(input.toLowerCase());
        }
        if (hashes.isEmpty()) {
            report.addFinding("HashCracker", "Aucun hash valide. Donnez un hash MD5 (32 car. hex) / SHA1 (40 car. hex) ou un chemin vers un fichier.");
            return;
        }

        List<String> wordlist = loadWordlist();
        if (wordlist.isEmpty()) {
            report.addFinding("HashCracker", "Wordlist rockyou introuvable. Placez rockyou.txt dans src/main/resources/wordlists/ ou à la racine du projet.");
            return;
        }

        report.addFinding("HashCracker", "Cracking " + hashes.size() + " hash(es) avec rockyou (" + wordlist.size() + " mots)...");

        for (String hash : hashes) {
            String trimmed = hash.trim().toLowerCase();
            if (trimmed.isEmpty()) continue;

            String found = crackHash(trimmed, wordlist);
            if (found != null) {
                report.addFinding("HashCracker", "CRACKED: " + trimmed + " => " + found);
            } else {
                report.addFinding("HashCracker", "Non trouvé: " + trimmed);
            }
        }
    }

    private static boolean looksLikeHash(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return (t.length() == 32 && t.matches("[0-9a-f]{32}")) || (t.length() == 40 && t.matches("[0-9a-f]{40}"));
    }

    private List<String> loadHashesFromFile(String path) {
        List<String> lines = new ArrayList<>();
        try {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // ignore
        }
        return lines;
    }

    private List<String> loadWordlist() {
        // 1. rockyou.txt depuis les resources (classpath)
        List<String> words = loadWordlistFromResource(ROCKYOU_RESOURCE);
        // 2. rockyou.txt depuis le système de fichiers
        if (words.isEmpty()) {
            words = loadWordlistFromFile("src/main/resources/wordlists/rockyou.txt");
        }
        if (words.isEmpty()) {
            words = loadWordlistFromFile("wordlists/rockyou.txt");
        }
        // 3. rockyou-subset.txt (inclus dans le projet)
        if (words.isEmpty()) {
            words = loadWordlistFromResource(ROCKYOU_SUBSET);
        }
        return words;
    }

    private List<String> loadWordlistFromResource(String resourcePath) {
        List<String> words = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return words;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) words.add(line.trim());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return words;
    }

    private List<String> loadWordlistFromFile(String path) {
        List<String> words = new ArrayList<>();
        try {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                    if (!line.trim().isEmpty()) words.add(line.trim());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return words;
    }

    private String crackHash(String hash, List<String> wordlist) {
        int len = hash.length();
        boolean isMd5 = (len == 32 && hash.matches("[0-9a-f]{32}"));
        boolean isSha1 = (len == 40 && hash.matches("[0-9a-f]{40}"));

        for (String word : wordlist) {
            try {
                if (isMd5) {
                    String md5 = md5(word);
                    if (hash.equals(md5)) return word;
                }
                if (isSha1) {
                    String sha1 = sha1(word);
                    if (hash.equals(sha1)) return word;
                }
                if (!isMd5 && !isSha1) {
                    String md5 = md5(word);
                    String sha1 = sha1(word);
                    if (hash.equals(md5) || hash.equals(sha1)) return word;
                }
            } catch (Exception e) {
                // skip
            }
        }
        return null;
    }

    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    private static String sha1(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
