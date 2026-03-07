# RedTeam Tool

**Outil Red Team / offensive security** — Projet Java POO (Programmation Orientée Objet).

Projet réalisé par **Mathis Lebel** et **Lucas Pellegrin** dans le cadre du contrôle POO.

---

## Installation

### Prérequis

- **Java 17** ou supérieur
- *(Optionnel)* **Maven 3.x** pour le build JAR

### Étapes

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/0osmoz0/Controle-POO-Mathis-Lebel-et-Lucas-Pellegrin-cyber.git
   cd Controle-POO-Mathis-Lebel-et-Lucas-Pellegrin-cyber/redteam-tool
   ```

2. **Vérifier Java**
   ```bash
   java -version   # doit afficher 17+
   ```

3. *(Optionnel)* **Wordlists pour HashCracker**  
   Placez `rockyou.txt` ou `passwords.txt` dans `src/main/resources/wordlists/` pour le crack de hashes. Voir `redteam-tool/src/main/resources/wordlists/README-wordlists.md`.

---

## Lancement

### Mode CLI (ligne de commande)

```bash
cd redteam-tool
./run.sh
```

Menu interactif avec :
- **[1] Phishing** — Assistant (Netflix/Instagram, QR, shortener, homograph, ngrok)
- **[2] HashCracker** — Crack MD5/SHA1 par wordlist
- **[3] Subdomain Takeover** — Détection de vulnérabilités
- **[4] Password Strength** — Analyse de la force d’un mot de passe

### Mode GUI (interface web Bootstrap)

```bash
cd redteam-tool
./run.sh gui
```

Ouvre le dashboard sur **http://127.0.0.1:7070** — interface Bootstrap avec tous les modules.

### Autres commandes

```bash
./run.sh list                    # Liste les modules disponibles
./run.sh run hashcracker <hash>   # Exécute un module en ligne de commande
```

### Build JAR (Maven)

```bash
cd redteam-tool
mvn clean package
java -jar target/redteam-tool-1.0-SNAPSHOT.jar
```

---

## Techniques utilisées

| Module | Technique | Description |
|--------|-----------|-------------|
| **Phishing** | Ingénierie sociale | Serveur HTTP avec templates Netflix/Instagram, harvest de credentials, redirection vers le vrai site |
| **HashCracker** | Force brute / dictionnaire | Crack MD5 et SHA-1 par wordlist (rockyou, passwords.txt) |
| **Subdomain Takeover** | Reconnaissance | Vérification des enregistrements CNAME pour détecter des sous-domaines vulnérables |
| **Password Strength** | Analyse heuristique | Score de force, détection de patterns faibles (dates, mots courants, etc.) |
| **QR Code** | Génération | QR code pointant vers l’URL phishing (API qrserver.com) |
| **URL Shortener** | Obfuscation | URLs courtes avec tracking des clics (IP, User-Agent) |
| **Homograph** | IDN homograph attack | Génération de domaines lookalike (Unicode) + Punycode pour enregistrement |
| **ngrok** | Tunneling | Exposition du serveur local sur Internet via ngrok |

---

## Architecture POO

### Interfaces et polymorphisme

- **`Module`** — Interface commune à tous les modules : `run(Target, Report)`
- **`Report`** — Interface pour les rapports : `addFinding()`, `getFindings()`
- **`Reporter`** — Interface d’affichage : `output(Report)` → implémentation `ConsoleReporter`

### Packages et responsabilités

| Package | Rôle |
|---------|------|
| **core** | `Target` (cible), `Report`, `Module`, `DefaultReport` |
| **credential** | `HashCracker`, `PasswordStrengthAnalyzer` |
| **phishing** | `QrCodeGenerator`, `HomographGenerator` |
| **recon** | `SubdomainTakeoverChecker` |
| **web** | `PhishingHttpServer`, `PhishingPageGenerator`, `CredentialHarvester`, `UrlShortener`, `DashboardServer` |
| **output** | `Reporter`, `ConsoleReporter` |
| **util** | `Ansi` (couleurs terminal), `NgrokHelper` |

### Principes appliqués

- **Encapsulation** : chaque module encapsule sa logique
- **Polymorphisme** : tous les modules implémentent `Module`
- **Injection de dépendances** : `PhishingHttpServer` reçoit `PhishingPageGenerator`, `CredentialHarvester`
- **Séparation des responsabilités** : génération de pages, harvest, affichage séparés

---

## Arborescence

```
redteam-tool/
├── pom.xml
├── run.sh
├── src/main/
│   ├── java/fr/redteam/
│   │   ├── Main.java              # Point d'entrée (CLI ou GUI)
│   │   ├── RedTeamCli.java        # Menu interactif
│   │   ├── core/                  # Target, Report, Module
│   │   ├── credential/            # HashCracker, PasswordStrength
│   │   ├── phishing/              # QrCode, Homograph
│   │   ├── recon/                 # SubdomainTakeover
│   │   ├── web/                   # Phishing, UrlShortener, Dashboard
│   │   ├── output/                # Reporter
│   │   └── util/                  # Ansi, NgrokHelper
│   └── resources/
│       ├── templates/             # netflix.html, instagram.html
│       ├── web/                    # index.html (dashboard Bootstrap)
│       └── wordlists/              # passwords.txt, rockyou.txt
└── target/                         # Classes compilées
```

---

## Avertissement

**Usage strictement réservé** à des environnements autorisés et à des fins de démonstration / portfolio. Ne pas utiliser à des fins malveillantes.
