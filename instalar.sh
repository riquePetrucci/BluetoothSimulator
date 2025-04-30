#!/data/data/com.termux/files/usr/bin/bash

# Caminhos
PROJETO=~/BluetoothSimulator
APK_LOCAL="$PROJETO/app/build/outputs/apk/debug/app-debug.apk"
APK_SD="/sdcard/app-debug.apk"

echo ">> Compilando o projeto..."
cd "$PROJETO" || exit 1
./gradlew assembleDebug || exit 1

echo ">> Copiando APK para o armazenamento externo..."
cp "$APK_LOCAL" "$APK_SD" || exit 1

echo ">> Iniciando instalação do APK..."
am start -a android.intent.action.VIEW -d "file://$APK_SD" -t "application/vnd.android.package-archive"
