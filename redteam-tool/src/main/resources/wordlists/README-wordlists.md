# Wordlists

## rockyou.txt (HashCracker)

**HashCracker** utilise **uniquement rockyou.txt** (~14 millions de mots) pour cracker les hashes MD5/SHA-1 (CLI et GUI).

### rockyou.txt

Le fichier est inclus dans `src/main/resources/wordlists/rockyou.txt` (décompressé).

### Emplacements détectés automatiquement

1. `src/main/resources/wordlists/rockyou.txt` (resources / classpath)
2. `wordlists/rockyou.txt`
3. `/usr/share/wordlists/rockyou.txt` (Kali Linux)

### Si rockyou.txt est absent

Téléchargez-le depuis [kali-wordlists](https://github.com/00xBAD/kali-wordlists) ou [SecLists](https://github.com/danielmiessler/SecLists), décompressez `rockyou.txt.gz` avec `gunzip -k rockyou.txt.gz`, puis placez-le dans `src/main/resources/wordlists/`.

---

## subdomains.txt (Subdomain Takeover)

**SubdomainTakeoverChecker** utilise **subdomains.txt** (SecLists, 5000 sous-domaines courants) pour l’énumération DNS et la détection de takeover.

Le fichier est inclus dans le projet (source : [SecLists Discovery/DNS](https://github.com/danielmiessler/SecLists/tree/master/Discovery/DNS)).
