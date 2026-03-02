package fr.redteam.core;

import java.util.List;

/**
 * Contrat pour un rapport contenant les findings (résultats) d'un scan ou d'une opération.
 */
public interface Report {

    void addFinding(String category, String message);

    List<String> getFindings();

    void clear();
}
