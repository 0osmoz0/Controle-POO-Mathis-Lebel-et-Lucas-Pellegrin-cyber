# RedTeam Tool

Outil Red Team / offensive security (portfolio) — Java POO.

---

## Arborescence complète du projet

```
redteam-tool/
├── pom.xml
├── README.md
│
└── src/
    ├── main/
    │   ├── java/fr/redteam/
    │   │   ├── Main.java
    │   │   ├── RedTeamCli.java
    │   │   │
    │   │   ├── core/
    │   │   │   ├── Target.java
    │   │   │   ├── Report.java
    │   │   │   ├── Scanner.java
    │   │   │   └── Module.java
    │   │   │
    │   │   ├── recon/
    │   │   │   ├── PortScanner.java
    │   │   │   ├── ServiceDetector.java
    │   │   │   └── SubdomainEnumerator.java
    │   │   │
    │   │   ├── web/
    │   │   │   ├── PhishingPageGenerator.java
    │   │   │   ├── PhishingServer.java
    │   │   │   ├── CredentialHarvester.java
    │   │   │   └── DirectoryBruteforcer.java
    │   │   │
    │   │   ├── payload/
    │   │   │   ├── PayloadConfig.java
    │   │   │   ├── ApkPayloadBuilder.java
    │   │   │   └── DownloadServer.java
    │   │   │
    │   │   ├── credential/
    │   │   │   ├── PasswordSprayer.java
    │   │   │   └── HashCracker.java
    │   │   │
    │   │   ├── c2/
    │   │   │   ├── Beacon.java
    │   │   │   ├── Listener.java
    │   │   │   └── AgentStubGenerator.java
    │   │   │
    │   │   └── output/
    │   │       ├── Reporter.java
    │   │       └── ConsoleReporter.java
    │   │
    │   └── resources/
    │       ├── templates/
    │       │   └── login-phishing.html
    │       └── wordlists/
    │           ├── directories.txt
    │           └── subdomains.txt
    │
    └── test/
        └── java/fr/redteam/
            └── (tests unitaires)
```

---

## Rôle des packages

| Package    | Rôle |
|-----------|------|
| **core**  | Cible (`Target`), rapport (`Report`), interfaces `Scanner` et `Module`. |
| **recon** | Scan de ports, détection de services, énumération de sous-domaines. |
| **web**   | Génération de pages de phishing, serveur phishing, harvest de credentials, bruteforce de répertoires. |
| **payload** | Construction/injection de payload APK, serveur de téléchargement (APK / binaires). |
| **credential** | Password spraying, crack de hashes (wordlist). |
| **c2**    | Listener (beacons), modèle Beacon, générateur de stubs agent (one-liner, reverse shell). |
| **output** | Interface `Reporter`, implémentation console pour l’affichage des rapports. |

---

## Prérequis

- Java 17+
- Maven 3.x

## Build

```bash
mvn clean package
```

## Lancement

```bash
java -jar target/redteam-tool-1.0-SNAPSHOT.jar [options] [module]
```

---

*Usage strictement réservé à des environnements autorisés et à des fins de démonstration / portfolio.*
