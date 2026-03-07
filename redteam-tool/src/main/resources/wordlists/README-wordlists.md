# Wordlists

**HashCracker** utilise les wordlists de ce dossier pour cracker les hashes MD5/SHA-1.

## Fichiers

| Fichier | Usage |
|---------|--------|
| **passwords.txt** | Wordlist par défaut (~10k mots courants). |
| **rockyou.txt** | Wordlist complète (~14 Mo). HashCracker la charge en priorité si présente. |

## Utilisation

HashCracker charge d'abord `rockyou.txt`, puis `passwords.txt` si rockyou est vide ou absent. Place les fichiers dans `src/main/resources/wordlists/`.

**Note** : `rockyou.txt` est souvent dans `.gitignore` (trop lourd). À télécharger séparément si besoin.
