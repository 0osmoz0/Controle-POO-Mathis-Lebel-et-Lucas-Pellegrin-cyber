package fr.redteam.output;

import fr.redteam.core.Report;

/**
 * Contrat pour l'affichage / l'export des rapports (console, fichier, etc.).
 */
public interface Reporter {

    /**
     * Affiche ou exporte le rapport.
     */
    void output(Report report);
}
