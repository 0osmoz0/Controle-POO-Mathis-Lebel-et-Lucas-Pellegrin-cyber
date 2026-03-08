# Wordlists

**HashCracker** utilise **rockyou** pour cracker les hashes MD5/SHA-1 (CLI et GUI).

## Fichiers

| Fichier | Usage |
|---------|--------|
| **rockyou-subset.txt** | Inclus dans le projet (~200 mots courants). Utilisé par défaut. |
| **rockyou.txt** | Wordlist complète (~14 Mo). HashCracker la charge en priorité si présente. |

## Ordre de chargement

1. `rockyou.txt` (resources ou `src/main/resources/wordlists/` ou `wordlists/`)
2. `rockyou-subset.txt` (inclus, fallback)

## Utilisation

- **Par défaut** : `rockyou-subset.txt` est utilisé (fourni dans le projet).
- **Pour plus de mots** : placez `rockyou.txt` dans `src/main/resources/wordlists/`. Téléchargez-le depuis [hashes.org](https://hashes.org/leaks.php) ou [GitHub rockyou](https://github.com/brannondorsey/naive-hashcat/releases).
