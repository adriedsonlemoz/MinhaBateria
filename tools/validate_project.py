#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP_MAIN = ROOT / "app" / "src" / "main"
RES = APP_MAIN / "res"


def fail(message: str) -> None:
    print(f"ERRO: {message}", file=sys.stderr)
    raise SystemExit(1)


def read(path: Path) -> str:
    if not path.is_file():
        fail(f"arquivo ausente: {path.relative_to(ROOT)}")
    return path.read_text(encoding="utf-8")


def check_version() -> tuple[str, int]:
    raw = read(ROOT / "VERSION").strip()
    match = re.fullmatch(r"(\d+\.\d+\.\d+)\+(\d+)", raw)
    if not match:
        fail("VERSION deve usar o formato versionName+versionCode")
    name, code_text = match.groups()
    code = int(code_text)

    gradle = read(ROOT / "app" / "build.gradle.kts")
    if f'versionName = "{name}"' not in gradle or f"versionCode = {code}" not in gradle:
        fail("versão do app/build.gradle.kts não corresponde a VERSION")

    identity = json.loads(read(ROOT / "app_identity.json"))
    if identity.get("version_name") != name or identity.get("version_code") != code:
        fail("app_identity.json não corresponde a VERSION")

    workflow = read(ROOT / ".github" / "workflows" / "build-apk.yml")
    expected_apk = f"Minha-Bateria-{name}.apk"
    if expected_apk not in workflow or f'TAG="v{name}"' not in workflow:
        fail("workflow não está sincronizado com a versão")

    readme = read(ROOT / "README.md")
    if expected_apk not in readme:
        fail("README não documenta o APK da versão atual")
    if f"- versionName: {name}" not in readme or f"- versionCode: {code}" not in readme:
        fail("README não está sincronizado com versionName/versionCode")
    if f"- versão completa: {name}+{code}" not in readme:
        fail("README não documenta a versão completa atual")
    return name, code


def check_xml() -> int:
    xml_files = list(APP_MAIN.rglob("*.xml"))
    for path in xml_files:
        try:
            ET.parse(path)
        except ET.ParseError as exc:
            fail(f"XML inválido em {path.relative_to(ROOT)}: {exc}")
    return len(xml_files)


def resource_names(kind: str) -> set[str]:
    if kind in {"layout", "drawable"}:
        folder = RES / kind
        return {p.stem for p in folder.glob("*.xml")} | {p.stem for p in folder.glob("*.png")}
    names: set[str] = set()
    pattern = re.compile(rf'<{kind}\s+name="([^"]+)"')
    for path in (RES / "values").glob("*.xml"):
        names.update(pattern.findall(read(path)))
    return names


def check_resources() -> None:
    kotlin = "\n".join(read(path) for path in APP_MAIN.rglob("*.kt"))
    xml = "\n".join(read(path) for path in RES.rglob("*.xml"))
    declared_ids = set(re.findall(r"@\+id/([A-Za-z0-9_]+)", xml))
    used_ids = set(re.findall(r"R\.id\.([A-Za-z0-9_]+)", kotlin))
    missing_ids = sorted(used_ids - declared_ids)
    if missing_ids:
        fail(f"IDs ausentes: {', '.join(missing_ids)}")

    for kind in ("layout", "drawable", "color"):
        available = resource_names(kind)
        used = set(re.findall(rf"R\.{kind}\.([A-Za-z0-9_]+)", kotlin))
        missing = sorted(used - available)
        if missing:
            fail(f"recursos {kind} ausentes: {', '.join(missing)}")


def check_launcher_label() -> None:
    manifest = read(APP_MAIN / "AndroidManifest.xml")
    if 'android:label="@string/app_name"' not in manifest:
        fail("Manifest não declara @string/app_name")
    if 'android.intent.action.MAIN' not in manifest or 'android.intent.category.LAUNCHER' not in manifest:
        fail("Activity MAIN/LAUNCHER ausente")


def check_line_limits() -> tuple[Path, int, Path, int]:
    code_files = list(ROOT.rglob("*.kt")) + list(ROOT.rglob("*.py"))
    code_counts = [(p, len(read(p).splitlines())) for p in code_files]
    oversized = [(p, n) for p, n in code_counts if n > 500]
    if oversized:
        fail("arquivo de código acima de 500 linhas: " + ", ".join(f"{p.name} ({n})" for p, n in oversized))
    max_code = max(code_counts, key=lambda item: item[1])

    xml_counts = [(p, len(read(p).splitlines())) for p in RES.rglob("*.xml")]
    max_xml = max(xml_counts, key=lambda item: item[1])
    return max_code[0], max_code[1], max_xml[0], max_xml[1]


def check_clean_source() -> None:
    forbidden_suffixes = {".apk", ".aab", ".jks", ".keystore"}
    for path in ROOT.rglob("*"):
        relative = path.relative_to(ROOT)
        if any(part in {"build", ".gradle"} for part in relative.parts):
            fail(f"diretório de build/cache no source: {relative}")
        if path.is_file() and path.suffix.lower() in forbidden_suffixes:
            fail(f"binário/chave dentro do source: {relative}")
        if path.is_file() and "secret" in path.name.lower():
            fail(f"arquivo de secrets dentro do source: {relative}")


def main() -> None:
    name, code = check_version()
    xml_count = check_xml()
    check_resources()
    check_launcher_label()
    check_clean_source()
    max_code, max_code_lines, max_xml, max_xml_lines = check_line_limits()
    print(f"OK Minha Bateria {name}+{code}")
    print(f"OK XMLs: {xml_count}")
    print(f"OK maior código: {max_code.relative_to(ROOT)} ({max_code_lines} linhas)")
    print(f"OK maior XML: {max_xml.relative_to(ROOT)} ({max_xml_lines} linhas)")
    print("OK recursos, launcher, versão e limpeza do source")


if __name__ == "__main__":
    main()
