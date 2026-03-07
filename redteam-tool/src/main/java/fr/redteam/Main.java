package fr.redteam;

import fr.redteam.web.DashboardServer;

import java.util.Scanner;

/**
 * Point d'entrée : CLI ou GUI Bootstrap.
 * Usage: ./run.sh gui  → lance le dashboard web
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && ("gui".equalsIgnoreCase(args[0]) || "--gui".equalsIgnoreCase(args[0]))) {
            runGui();
        } else {
            RedTeamCli cli = new RedTeamCli();
            cli.run(args);
        }
    }

    private static void runGui() {
        try {
            DashboardServer dashboard = new DashboardServer();
            dashboard.start();
            System.out.print("  [Entrée] pour arrêter › ");
            new Scanner(System.in).nextLine();
            dashboard.stop();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }
}
