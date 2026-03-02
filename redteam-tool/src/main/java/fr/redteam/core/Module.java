package fr.redteam.core;

/**
 * Contrat pour tout module exécutable (recon, web, credential, etc.).
 * Permet un traitement polymorphique via run(target, report).
 */
public interface Module {

    String getName();

    String getDescription();

    void run(Target target, Report report);
}
