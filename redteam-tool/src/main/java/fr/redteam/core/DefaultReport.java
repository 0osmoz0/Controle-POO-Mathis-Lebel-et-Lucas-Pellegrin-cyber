package fr.redteam.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DefaultReport implements Report {

    private final List<String> findings = new ArrayList<>();

    @Override
    public void addFinding(String category, String message) {
        if (category != null && message != null) {
            findings.add("[" + category + "] " + message);
        }
    }

    @Override
    public List<String> getFindings() {
        return Collections.unmodifiableList(findings);
    }

    @Override
    public void clear() {
        findings.clear();
    }
}
