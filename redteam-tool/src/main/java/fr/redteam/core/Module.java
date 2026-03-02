package fr.redteam.core;

public interface Module {

    String getName();

    String getDescription();

    void run(Target target, Report report);
}
