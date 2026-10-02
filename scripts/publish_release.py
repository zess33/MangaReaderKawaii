#!/usr/bin/env python3
import sys
import os
import subprocess
import json
import urllib.request
import urllib.error

REPO = "zess33/MangaReaderKawaii"
PROJECT_DIR = "/Users/pedrourielhuertaplantillas/Desktop/apps/manga/MangaReaderKawaii"
APK_PATH = "/Users/pedrourielhuertaplantillas/Desktop/MangaReader_Jasubileem.apk"

def get_github_token():
    token = os.environ.get("GITHUB_TOKEN", "") or os.environ.get("GH_TOKEN", "")
    if not token:
        secret_file = os.path.expanduser("~/.github_manga_token")
        if os.path.exists(secret_file):
            with open(secret_file, "r") as f:
                token = f.read().strip()
    return token

def publish_release(version_tag="v1.0.0", release_title=None, release_notes=None, skip_build=False):
    if release_title is None:
        release_title = f"MangaReader Kawaii {version_tag} (Jasubileem Edition)"
    if release_notes is None:
        release_notes = "🌸 MangaReader Kawaii Release Inicial:\n- Lectura fluida de Manga, Manhwa y Manhua en español.\n- Visor integrado sin publicidad.\n- Selector de iconos de Tomoe.\n- Modo oscuro confortable y temas pastel.\n- Auto-actualizaciones automáticas."

    token = get_github_token()
    if not token:
        print("Error: GITHUB_TOKEN no encontrado en variables de entorno ni en ~/.github_manga_token.")
        return False

    if not skip_build or not os.path.exists(APK_PATH):
        print(f"--> [1/4] Compilando APK para la versión {version_tag}...")
        subprocess.run(["./gradlew", "assembleDebug"], cwd=PROJECT_DIR, check=True)

        source_apk = os.path.join(PROJECT_DIR, "app/build/outputs/apk/debug/app-debug.apk")
        subprocess.run(["cp", "-f", source_apk, APK_PATH], check=True)
    
    print(f"--> [2/4] APK listo en: {APK_PATH} ({os.path.getsize(APK_PATH) / 1024 / 1024:.2f} MB)")

    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "MangaReaderKawaii-Publisher",
        "Content-Type": "application/json"
    }

    # 1. Crear Release en GitHub
    create_url = f"https://api.github.com/repos/{REPO}/releases"
    payload = json.dumps({
        "tag_name": version_tag,
        "target_commitish": "main",
        "name": release_title,
        "body": release_notes,
        "draft": False,
        "prerelease": False
    }).encode("utf-8")

    print(f"--> [3/4] Creando Release '{version_tag}' en GitHub ({REPO})...")
    req = urllib.request.Request(create_url, data=payload, headers=headers, method="POST")

    try:
        with urllib.request.urlopen(req) as resp:
            release_data = json.loads(resp.read().decode("utf-8"))
            upload_url = release_data["upload_url"].split("{")[0]
            html_url = release_data.get("html_url", "")
    except urllib.error.HTTPError as e:
        error_body = e.read().decode("utf-8")
        print(f"Error creando release en GitHub: HTTP {e.code} - {error_body}")
        return False

    # 2. Subir APK como Asset
    print(f"--> [4/4] Subiendo APK ({APK_PATH}) al Release...")
    with open(APK_PATH, "rb") as f:
        apk_bytes = f.read()

    upload_headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/vnd.android.package-archive",
        "User-Agent": "MangaReaderKawaii-Publisher"
    }

    asset_url = f"{upload_url}?name=MangaReader_Jasubileem.apk"
    upload_req = urllib.request.Request(asset_url, data=apk_bytes, headers=upload_headers, method="POST")

    try:
        with urllib.request.urlopen(upload_req) as upload_resp:
            print("==================================================")
            print(f"¡ÉXITO TOTAL! Release {version_tag} publicado automáticamente.")
            print(f"Enlace de descarga directa: {html_url}")
            print("==================================================")
            return True
    except urllib.error.HTTPError as e:
        error_body = e.read().decode("utf-8")
        print(f"Error subiendo APK: HTTP {e.code} - {error_body}")
        return False

if __name__ == "__main__":
    tag = sys.argv[1] if len(sys.argv) > 1 else "v1.0.0"
    title = sys.argv[2] if len(sys.argv) > 2 else f"MangaReader Kawaii {tag}"
    notes = sys.argv[3] if len(sys.argv) > 3 else "Mejoras de rendimiento, temas visuales y auto-actualizador integrado."
    skip = "--skip-build" in sys.argv
    publish_release(tag, title, notes, skip_build=skip)
