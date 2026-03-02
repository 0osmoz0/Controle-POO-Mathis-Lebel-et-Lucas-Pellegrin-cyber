package fr.redteam;

/**
 * Point d'entrée : affiche le logo ASCII et lance le CLI.
 */
public class Main {

    public static void main(String[] args) {
        RedTeamCli cli = new RedTeamCli();
        cli.run(args);
    }
}
