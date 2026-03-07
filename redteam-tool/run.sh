#!/bin/bash
# Lance l'app SANS Maven (javac + java).
# Usage: ./run.sh        → CLI interactif
#        ./run.sh gui     → Dashboard Bootstrap (http://127.0.0.1:7070)
#        ./run.sh list    → liste les modules

cd "$(dirname "$0")"
SRC=src/main/java
RES=src/main/resources
OUT=target/classes

mkdir -p "$OUT"
echo "Compilation..."
javac -d "$OUT" -sourcepath "$SRC" "$SRC/fr/redteam/Main.java" || exit 1
echo "Lancement..."
exec java -cp "$OUT:$RES" fr.redteam.Main "$@"
