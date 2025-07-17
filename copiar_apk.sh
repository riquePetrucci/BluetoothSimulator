#!/data/data/com.termux/files/usr/bin/bash

APK_PATH="./app/build/outputs/apk/debug/app-debug.apk"
DEST_PATH="/sdcard/BluetoothSimulator.apk"

if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$DEST_PATH"
    echo "APK copiado para: $DEST_PATH"
else
    echo "APK não encontrado em: $APK_PATH"
    exit 1
fi
