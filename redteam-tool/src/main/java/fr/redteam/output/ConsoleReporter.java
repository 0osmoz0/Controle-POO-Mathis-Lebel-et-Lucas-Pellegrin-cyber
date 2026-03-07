package fr.redteam.output;

import fr.redteam.core.Report;

public class ConsoleReporter implements Reporter {

    @Override
    public void output(Report report) {
        if (report == null) {
            return;
        }
        for (String finding : report.getFindings()) {
            System.out.println(finding);
        }
    }
}
