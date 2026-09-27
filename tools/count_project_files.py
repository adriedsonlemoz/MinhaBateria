#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EXCLUDE = {".git", ".gradle", "build"}

def files(root: Path) -> set[str]:
    result = set()
    for p in root.rglob("*"):
        if p.is_file() and not any(part in EXCLUDE for part in p.relative_to(root).parts):
            result.add(p.relative_to(root).as_posix())
    return result

def main() -> int:
    current = files(ROOT)
    print(f"Arquivos no projeto: {len(current)}")
    if len(sys.argv) == 2:
        base = Path(sys.argv[1]).resolve()
        previous = files(base)
        added = sorted(current - previous)
        removed = sorted(previous - current)
        print(f"Arquivos na base: {len(previous)}")
        print(f"Adicionados: {len(added)}")
        print(f"Removidos: {len(removed)}")
        if added:
            print("ADICIONADOS:")
            print("\n".join(added))
        if removed:
            print("REMOVIDOS:")
            print("\n".join(removed))
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
