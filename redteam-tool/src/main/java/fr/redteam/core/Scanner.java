package fr.redteam.core;

public interface Scanner extends Module {

    Report scan(Target target);

    @Override
    default void run(Target target, Report report) {
        Report scanResult = scan(target);
        String category = getName();
        for (String finding : scanResult.getFindings()) {
            report.addFinding(category, finding);
        }
    }
}
