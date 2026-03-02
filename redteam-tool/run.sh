#!/bin/bash
# Lance l'app SANS Maven (javac + java).
# Usage: ./run.sh   ou   ./run.sh list   ou   ./run.sh run agentstubgenerator 192.168.1.1 4444

cd "$(dirname "$0")"
SRC=src/main/java
RES=src/main/resources
OUT=target/classes

mkdir -p "$OUT"
echo "Compilation..."
javac -d "$OUT" -sourcepath "$SRC" "$SRC/fr/redteam/Main.java" || exit 1
echo "Lancement..."
exec java -cp "$OUT:$RES" fr.redteam.Main "$@"
