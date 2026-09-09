#!/usr/bin/env python3
"""从目录结构推导 registry.json。

registry 是**被推导的**，不是被手工维护的——手写清单是第二份真实来源，
必然与实际目录漂移。加减 skill 只需增删目录，跑一次本脚本即可。

用法:
    tools/build-registry.py            重新生成 registry.json
    tools/build-registry.py --check    与现状不一致则 exit 1（CI 用）
"""
import json
import pathlib
import re
import sys
from datetime import date

ROOT = pathlib.Path(__file__).resolve().parent.parent
REGISTRY = ROOT / "registry.json"
CATEGORIES_FILE = ROOT / "categories.json"


def read_frontmatter(skill_md: pathlib.Path) -> dict:
    """读 SKILL.md 的 YAML frontmatter。只取 name 与 description 两个字段。

    不引 yaml 依赖：frontmatter 只有这两个键，且 description 可能很长并含冒号，
    所以按「第一个冒号切分 + 缩进续行」手工解析，比引一个依赖更省。
    """
    text = skill_md.read_text(encoding="utf-8")
    m = re.match(r"^---\n(.*?)\n---", text, re.S)
    if not m:
        return {}
    out, key = {}, None
    for line in m.group(1).split("\n"):
        if line.startswith((" ", "\t")) and key:      # 缩进续行
            out[key] += " " + line.strip()
        elif ":" in line:
            key, _, val = line.partition(":")
            key = key.strip()
            out[key] = val.strip()
    return out


def scan() -> dict:
    categories = json.loads(CATEGORIES_FILE.read_text(encoding="utf-8"))
    skills, problems = [], []

    for cat in sorted(categories):
        cat_dir = ROOT / cat
        if not cat_dir.is_dir():
            problems.append(f"categories.json 里有 '{cat}'，但目录不存在")
            continue
        for entry in sorted(cat_dir.iterdir()):
            if not entry.is_dir() or entry.name.startswith("."):
                continue
            skill_md = entry / "SKILL.md"
            if not skill_md.is_file():
                problems.append(f"{cat}/{entry.name} 缺少 SKILL.md")
                continue
            fm = read_frontmatter(skill_md)
            name = fm.get("name") or entry.name
            desc = fm.get("description", "")
            if not desc:
                problems.append(f"{cat}/{entry.name} 的 frontmatter 缺少 description")
            if name != entry.name:
                problems.append(
                    f"{cat}/{entry.name}：目录名与 frontmatter 的 name='{name}' 不一致"
                )
            skills.append(
                {"name": name, "description": desc, "category": cat,
                 "path": f"{cat}/{entry.name}", "origin": "custom"}
            )

    # 目录存在但不在 categories.json 里 —— 会导致该目录下的 skill 永远不被收录
    known = set(categories) | {"tools", ".github", ".claude"}
    for entry in sorted(ROOT.iterdir()):
        if entry.is_dir() and not entry.name.startswith(".") and entry.name not in known:
            problems.append(f"目录 '{entry.name}' 不在 categories.json 里，其下的 skill 不会被收录")

    if problems:
        for p in problems:
            print(f"ERROR: {p}", file=sys.stderr)
        sys.exit(1)

    for cat in categories:
        categories[cat]["count"] = sum(1 for s in skills if s["category"] == cat)

    return {
        "version": "1.0",
        "repo": "dev-junxi/skill-repository",
        "updated": date.today().isoformat(),
        "total": len(skills),
        "categories": categories,
        "skills": skills,
    }


def main() -> None:
    check = "--check" in sys.argv
    new = scan()
    new_text = json.dumps(new, ensure_ascii=False, indent=2) + "\n"

    if check:
        if not REGISTRY.is_file():
            print("ERROR: registry.json 不存在，先运行 tools/build-registry.py", file=sys.stderr)
            sys.exit(1)
        old = REGISTRY.read_text(encoding="utf-8")
        # updated 是日期，天然会变，比对时忽略它
        norm = lambda t: re.sub(r'"updated": "[^"]*"', '"updated": "*"', t)
        if norm(old) != norm(new_text):
            print("ERROR: registry.json 已过期，请运行 tools/build-registry.py", file=sys.stderr)
            sys.exit(1)
        print(f"registry: 与目录结构一致（{new['total']} 个 skill）")
    else:
        REGISTRY.write_text(new_text, encoding="utf-8")
        print(f"registry: 已更新，共 {new['total']} 个 skill")
        for cat, meta in new["categories"].items():
            if meta["count"]:
                print(f"  {meta['emoji']} {cat:20s} {meta['count']}")


if __name__ == "__main__":
    main()
