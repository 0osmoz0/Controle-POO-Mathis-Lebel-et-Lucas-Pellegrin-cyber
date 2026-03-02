package fr.redteam.core;

import java.util.List;

public interface Report {

    void addFinding(String category, String message);

    List<String> getFindings();

    void clear();
}
