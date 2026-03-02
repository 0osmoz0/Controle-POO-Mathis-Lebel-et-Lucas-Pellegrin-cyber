package fr.redteam.core;

/**
 * Un scanner est un Module qui produit un rapport de scan.
 * Les implémentations n'ont qu'à implémenter scan(Target) ; run() fusionne le résultat dans le Report.
 */
public interface Scanner extends Module {

    /**
     * Effectue le scan de la cible et retourne un rapport.
     */
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
