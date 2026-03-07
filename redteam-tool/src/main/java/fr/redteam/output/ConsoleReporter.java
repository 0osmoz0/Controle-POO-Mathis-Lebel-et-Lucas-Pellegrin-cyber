package fr.redteam.output;

import fr.redteam.core.Report;
import fr.redteam.util.Ansi;

public class ConsoleReporter implements Reporter {

    @Override
    public void output(Report report) {
        if (report == null) return;
        for (String finding : report.getFindings()) {
            String line = "  " + finding;
            if (finding.contains("CRACKED:") || finding.contains("POTENTIEL TAKEOVER") || finding.contains("✓ ")) {
                System.out.println(Ansi.GREEN + line + Ansi.RESET);
            } else if (finding.contains("Non trouvé") || finding.contains("vide") || finding.contains("introuvable") || finding.contains("✗")) {
                System.out.println(Ansi.YELLOW + line + Ansi.RESET);
            } else {
                System.out.println(Ansi.dim(line));
            }
        }
    }
}
