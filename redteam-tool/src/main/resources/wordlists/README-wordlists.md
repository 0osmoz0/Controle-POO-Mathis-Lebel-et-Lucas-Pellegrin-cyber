# Wordlists (téléchargées depuis GitHub)

## Fichiers présents

| Fichier | Source | Usage |
|---------|--------|--------|
| **passwords.txt** | SecLists (10k-most-common) | HashCracker : crack MD5/SHA-1. Par défaut = 10k mots. |
| **rockyou.txt** | [naive-hashcat/releases](https://github.com/brannondorsey/naive-hashcat/releases/download/data/rockyou.txt) | Wordlist complète (~14 Mo). Pour un crack plus lourd, remplacer le contenu de `passwords.txt` par `rockyou.txt`. |
| **10k-most-common.txt** | [SecLists Passwords](https://github.com/danielmiessler/SecLists/blob/master/Passwords/Common-Credentials/10k-most-common.txt) | Copie utilisée pour `passwords.txt`. |
| **users.txt** | [SecLists Usernames/Names](https://github.com/danielmiessler/SecLists/blob/master/Usernames/Names/names.txt) | PasswordSprayer : liste d’utilisateurs (un par ligne). |
| **passwords_spray.txt** | Créé à la main | PasswordSprayer : quelques mots de passe pour le spray (éviter le lockout). |

## Utilisation

- **HashCracker** lit `passwords.txt` (classpath). Pour utiliser rockyou : remplacer `passwords.txt` par le fichier `rockyou.txt` (ou le renommer).
- **PasswordSprayer** lit `users.txt` et `passwords_spray.txt`. Tu peux modifier `passwords_spray.txt` (quelques lignes suffisent).
