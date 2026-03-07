package fr.redteam;

import fr.redteam.core.DefaultReport;
import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;
import fr.redteam.c2.AgentStubGenerator;
import fr.redteam.c2.Listener;
import fr.redteam.credential.HashCracker;
import fr.redteam.credential.PasswordSprayer;
import fr.redteam.output.ConsoleReporter;
import fr.redteam.output.Reporter;
import fr.redteam.payload.ApkPayloadBuilder;
import fr.redteam.payload.DownloadServer;
import fr.redteam.web.CredentialHarvester;
import fr.redteam.web.DirectoryBruteforcer;
import fr.redteam.web.PhishingHttpServer;
import fr.redteam.web.PhishingPageGenerator;
import fr.redteam.web.PhishingServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


public class RedTeamCli {

    private static final String LOGO =
            "  ____            ______          __  __   \n" +
            " |  _ \\ ___  __ _| ___ \\ \\        / /_| |_  \n" +
            " | |_) / _ \\/ _` | |_ \\ \\  /\\  / / __| __| \n" +
            " |  _ <  __/ (_| |  _| \\ \\/  \\/ /| |_| |_  \n" +
            " |_| \\_\\___|\\__, |_|    \\__/\\__/  \\__|\\__| \n" +
            "            |___/                          \n" +
            "  RedTeam Tool - Offensive Security (PoC)  \n";

    private final Map<String, Module> modules = new LinkedHashMap<>();
    private final Reporter reporter = new ConsoleReporter();

    public RedTeamCli() {
        register(new Listener());
        register(new AgentStubGenerator());
        register(new HashCracker());
        register(new PasswordSprayer());
        register(new ApkPayloadBuilder());
        register(new DownloadServer());
        register(new PhishingServer());
        register(new CredentialHarvester());
        register(new DirectoryBruteforcer());
    }

    private void register(Module m) {
        modules.put(m.getName().toLowerCase(), m);
    }

    public void printLogo() {
        System.out.println(LOGO);
    }

    public void run(String[] args) {
        printLogo();
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            listModules();
            return;
        }
        if (args.length > 0 && "run".equalsIgnoreCase(args[0])) {
            String name = args.length > 1 ? args[1] : null;
            String host = args.length > 2 ? args[2] : "127.0.0.1";
            int port = args.length > 3 ? parseInt(args[3], -1) : -1;
            runModule(name, host, port);
            return;
        }
        runInteractive();
    }

    private void listModules() {
        System.out.println("Modules disponibles:");
        System.out.println("--------------------");
        for (Module m : modules.values()) {
            System.out.println("  " + m.getName() + " - " + m.getDescription());
        }
    }

    private void runModule(String name, String host, int port) {
        if (name == null || name.isEmpty()) {
            System.out.println("Usage: run <module_name> [host] [port]");
            listModules();
            return;
        }
        Module m = modules.get(name.toLowerCase());
        if (m == null) {
            System.out.println("Module inconnu: " + name);
            listModules();
            return;
        }
        Target target = new Target(host, port > 0 ? port : -1);
        Report report = new DefaultReport();
        m.run(target, report);
        System.out.println("\n--- Rapport " + m.getName() + " ---");
        reporter.output(report);
    }

    private void runInteractive() {
        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n  [1] Lister les modules");
                System.out.println("  [2] Exécuter un module");
                System.out.println("  [3] Démarrer serveur phishing (HTTP sur 127.0.0.1:8080)");
                System.out.println("  [q] Quitter");
                System.out.print("Choix: ");
                String line = scan.nextLine();
                if (line == null) break;
                line = line.trim();
                if ("q".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) break;
                if ("1".equals(line)) {
                    listModules();
                    continue;
                }
                if ("3".equals(line)) {
                    startPhishingServer(scan);
                    continue;
                }
                if ("2".equals(line)) {
                    System.out.print("Nom du module: ");
                    String name = scan.nextLine();
                    if (name != null) name = name.trim();
                    String host;
                    int port;
                    if ("hashcracker".equalsIgnoreCase(name)) {
                        System.out.print("Hash MD5/SHA1 ou chemin fichier de hashes: ");
                        String h = scan.nextLine();
                        host = (h != null && !h.trim().isEmpty()) ? h.trim() : "";
                        port = -1;
                    } else {
                        System.out.print("Host (défaut 127.0.0.1): ");
                        String hostLine = scan.nextLine();
                        host = (hostLine != null && !hostLine.trim().isEmpty()) ? hostLine.trim() : "127.0.0.1";
                        System.out.print("Port (défaut -1): ");
                        String portLine = scan.nextLine();
                        port = parseInt(portLine != null ? portLine.trim() : "", -1);
                    }
                    runModule(name, host, port);
                    continue;
                }
                System.out.println("Choix invalide.");
            }
        }
        System.out.println("Bye.");
    }

    private void startPhishingServer(Scanner scan) {
        System.out.println("\nChoisissez le template de connexion :");
        System.out.println("  [1] Netflix");
        System.out.println("  [2] Instagram");
        System.out.print("Choix (1-2): ");
        String choice = scan.nextLine();
        if (choice != null) choice = choice.trim();
        String[] names = PhishingPageGenerator.TEMPLATE_NAMES;
        String template = null;
        int idx = parseInt(choice, 0);
        if (idx >= 1 && idx <= names.length) {
            template = names[idx - 1];
        }
        int port = 8080;
        String host = "127.0.0.1";
        try {
            PhishingHttpServer srv = new PhishingHttpServer(host, port, new PhishingPageGenerator(), new CredentialHarvester(), template);
            srv.start();
            System.out.println("\n>>> Serveur phishing démarré sur http://" + host + ":" + port);
            if (template != null) {
                System.out.println(">>> Template: " + template + " (page de connexion réaliste)");
            } else {
                System.out.println(">>> Ouvrez l'URL pour choisir un template.");
            }
            System.out.println(">>> Les identifiants soumis s'afficheront ici.");
            System.out.println(">>> Appuyez sur [Entrée] pour revenir au menu.");
            scan.nextLine();
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
