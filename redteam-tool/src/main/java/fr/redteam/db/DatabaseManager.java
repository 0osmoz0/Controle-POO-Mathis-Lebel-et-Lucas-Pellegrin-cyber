package fr.redteam.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

/**
 * Base locale légère (JSON Lines) pour historiser les exécutions.
 * Le fichier est créé dans: redteam-tool/data/redteam_runs.jsonl
 */
public final class DatabaseManager {

    private static final Path DB_DIR = Paths.get("data");
    private static final Path DB_FILE = DB_DIR.resolve("redteam_runs.jsonl");

    private DatabaseManager() {
    }

    public static synchronized void saveRun(String source, String module, String target, List<String> findings) {
        try {
            initIfNeeded();
            String line = toJsonLine(source, module, target, findings);
            Files.writeString(
                    DB_FILE,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception e) {
            System.err.println("[DB] Impossible d'enregistrer l'exécution: " + e.getMessage());
        }
    }

    public static Path getDbFilePath() {
        return DB_FILE.toAbsolutePath();
    }

    private static void initIfNeeded() throws IOException {
        if (!Files.exists(DB_DIR)) {
            Files.createDirectories(DB_DIR);
        }
        if (!Files.exists(DB_FILE)) {
            Files.createFile(DB_FILE);
        }
    }

    private static String toJsonLine(String source, String module, String target, List<String> findings) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"timestamp\":\"").append(escape(Instant.now().toString())).append("\",");
        sb.append("\"source\":\"").append(escape(nullToEmpty(source))).append("\",");
        sb.append("\"module\":\"").append(escape(nullToEmpty(module))).append("\",");
        sb.append("\"target\":\"").append(escape(nullToEmpty(target))).append("\",");
        sb.append("\"findingsCount\":").append(findings == null ? 0 : findings.size()).append(",");
        sb.append("\"findings\":").append(toJsonArray(findings));
        sb.append("}");
        return sb.toString();
    }

    private static String toJsonArray(List<String> findings) {
        if (findings == null || findings.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < findings.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(findings.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
