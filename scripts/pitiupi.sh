#!/usr/bin/env bash
# Build, instala plugins em pitiupi/plugins/ e executa o núcleo (catálogo: plugins.registry.json).
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CORE="${REPO_ROOT}/pitiupi"
PLUGINS_DIR="${CORE}/plugins"
REGISTRY="${REPO_ROOT}/plugins.registry.json"

usage() {
  cat <<'EOF'
Uso: scripts/pitiupi.sh <comando> [opções]

Comandos:
  run       Compila, atualiza plugins/ e abre a aplicação (padrão: --all)
  build     Compila e atualiza plugins/
  link      Só symlinks em pitiupi/plugins/ (JARs já buildados)
  list      Lista plugins do catálogo (id, categoria, JAR presente)
  discover  Sugere entradas para o catálogo (pastas pitiupi.* com SPI)
  check     Ferramentas no PATH + valida plugins.registry.json

Opções (exclusivas; padrão em run/build/link: --all):
  --all              Todos os plugins do catálogo
  --plugins ids      IDs separados por vírgula (ex.: chat,screencast)
  --category Nome    Social | Jogos | Streaming

Exemplos:
  ./scripts/pitiupi.sh list
  ./scripts/pitiupi.sh run --all
  ./scripts/pitiupi.sh run --category Social
  ./scripts/pitiupi.sh run --plugins chat
EOF
}

registry_python() {
  python3 - "$REGISTRY" "$REPO_ROOT" "$@" <<'PY'
import json
import sys
from pathlib import Path

registry_path = Path(sys.argv[1])
repo = Path(sys.argv[2])
mode = sys.argv[3]

data = json.loads(registry_path.read_text(encoding="utf-8"))
plugins = data.get("plugins", [])

def by_id(pid):
    for p in plugins:
        if p["id"] == pid:
            return p
    return None

def jar_path(entry):
    return repo / entry["dir"] / entry["jar"]

if mode == "validate":
    if not plugins:
        print("ERRO: nenhum plugin no catálogo", file=sys.stderr)
        sys.exit(1)
    for p in plugins:
        for key in ("id", "dir", "build", "jar", "menuCategory"):
            if key not in p:
                print(f"ERRO: campo ausente {key} em {p}", file=sys.stderr)
                sys.exit(1)
    sys.exit(0)

if mode == "list":
    for p in plugins:
        jp = jar_path(p)
        built = "sim" if jp.is_file() else "não"
        print(f"{p['id']}\t{p['menuCategory']}\t{p['dir']}\tjar:{built}")
    sys.exit(0)

if mode == "discover":
    known_dirs = {p["dir"] for p in plugins}
    for child in sorted(repo.glob("pitiupi.*")):
        if not child.is_dir():
            continue
        spi = list(child.rglob("META-INF/services/pitiupi.models.Plugin"))
        if not spi:
            continue
        status = "registrado" if child.name in known_dirs else "sugerido"
        build = "make" if (child / "Makefile").is_file() else "ant"
        print(f"{status}\t{child.name}\tbuild:{build}")
    sys.exit(0)

if mode == "resolve":
    filter_mode = sys.argv[4]
    arg = sys.argv[5] if len(sys.argv) > 5 else ""

    selected = []
    if filter_mode == "all":
        selected = plugins
    elif filter_mode == "category":
        cat = arg.strip()
        selected = [p for p in plugins if p["menuCategory"] == cat]
        if not selected:
            print(f"ERRO: categoria vazia ou inválida: {cat}", file=sys.stderr)
            sys.exit(1)
    elif filter_mode == "plugins":
        ids = [x.strip() for x in arg.lower().split(",") if x.strip()]
        for i in ids:
            p = by_id(i)
            if not p:
                print(f"ERRO: id desconhecido no catálogo: {i}", file=sys.stderr)
                sys.exit(1)
            selected.append(p)
    else:
        print(f"ERRO: modo resolve {filter_mode}", file=sys.stderr)
        sys.exit(1)

    for p in selected:
        print(p["id"])
    sys.exit(0)

if mode == "field":
    pid = sys.argv[4]
    field = sys.argv[5]
    p = by_id(pid)
    if not p:
        sys.exit(1)
    print(p[field])
    sys.exit(0)

print(f"ERRO: modo desconhecido {mode}", file=sys.stderr)
sys.exit(1)
PY
}

resolve_filter_mode() {
  if [[ -n "${FILTER_PLUGINS:-}" ]]; then
    echo "plugins"
  elif [[ -n "${FILTER_CATEGORY:-}" ]]; then
    echo "category"
  else
    echo "all"
  fi
}

resolve_filter_arg() {
  local mode="$1"
  case "$mode" in
    plugins) echo "${FILTER_PLUGINS}" ;;
    category) echo "${FILTER_CATEGORY}" ;;
    all) echo "" ;;
  esac
}

selected_ids() {
  local mode arg
  mode="$(resolve_filter_mode)"
  arg="$(resolve_filter_arg "$mode")"
  registry_python resolve "$mode" "$arg"
}

build_core() {
  (cd "${REPO_ROOT}/pitiupi" && ant jar)
}

build_one_plugin() {
  local id="$1"
  local dir build_cmd
  dir="$(registry_python field "$id" dir)"
  build_cmd="$(registry_python field "$id" build)"
  case "$build_cmd" in
    ant) (cd "${REPO_ROOT}/${dir}" && ant jar) ;;
    make) (cd "${REPO_ROOT}/${dir}" && make) ;;
    *)
      printf 'ERRO: build desconhecido para %s: %s\n' "$id" "$build_cmd" >&2
      exit 1
      ;;
  esac
}

build_plugins_from_selection() {
  local id
  while IFS= read -r id; do
    [[ -z "$id" ]] && continue
    build_one_plugin "$id"
  done < <(selected_ids)
}

link_plugins_from_selection() {
  local id dir jar_rel link_name
  mkdir -p "${PLUGINS_DIR}"
  rm -f "${PLUGINS_DIR}/"*.jar

  while IFS= read -r id; do
    [[ -z "$id" ]] && continue
    dir="$(registry_python field "$id" dir)"
    jar_rel="$(registry_python field "$id" jar)"
    link_name="${id}.jar"
    ln -sf "../../${dir}/${jar_rel}" "${PLUGINS_DIR}/${link_name}"
  done < <(selected_ids)

  local count
  count="$(find "${PLUGINS_DIR}" -maxdepth 1 -name '*.jar' 2>/dev/null | wc -l)"
  if [[ "$count" -eq 0 ]]; then
    printf 'AVISO: nenhum plugin em %s.\n' "${PLUGINS_DIR}" >&2
  else
    printf 'Plugins ativos em %s:\n' "${PLUGINS_DIR}"
    ls -1 "${PLUGINS_DIR}"/*.jar
  fi
}

check_tools() {
  local ok=0
  for cmd in java javac ant make python3; do
    if command -v "${cmd}" >/dev/null 2>&1; then
      printf 'OK %s\n' "${cmd}"
    else
      printf 'ERRO: %s não está no PATH\n' "${cmd}" >&2
      ok=1
    fi
  done
  if [[ ! -f "${REGISTRY}" ]]; then
    printf 'ERRO: catálogo ausente: %s\n' "${REGISTRY}" >&2
    ok=1
  else
    registry_python validate
    printf 'OK plugins.registry.json\n'
  fi
  return "${ok}"
}

CMD="run"
FILTER_PLUGINS=""
FILTER_CATEGORY=""
USE_ALL=1

while [[ $# -gt 0 ]]; do
  case "$1" in
    run | build | link | check | list | discover)
      CMD="$1"
      shift
      ;;
    --all)
      USE_ALL=1
      FILTER_PLUGINS=""
      FILTER_CATEGORY=""
      shift
      ;;
    --plugins)
      USE_ALL=0
      FILTER_PLUGINS="${2:-}"
      FILTER_CATEGORY=""
      shift 2
      ;;
    --category)
      USE_ALL=0
      FILTER_CATEGORY="${2:-}"
      FILTER_PLUGINS=""
      shift 2
      ;;
    -h | --help)
      usage
      exit 0
      ;;
    *)
      printf 'Argumento desconhecido: %s\n' "$1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if [[ "$USE_ALL" -eq 1 && -z "$FILTER_PLUGINS" && -z "$FILTER_CATEGORY" ]]; then
  : # default --all
elif [[ -n "$FILTER_PLUGINS" && -n "$FILTER_CATEGORY" ]]; then
  printf 'ERRO: use apenas um de --plugins, --category ou --all\n' >&2
  exit 1
fi

case "${CMD}" in
  check)
    check_tools
    ;;
  list)
    registry_python list
    ;;
  discover)
    registry_python discover
    ;;
  link)
    link_plugins_from_selection
    ;;
  build)
    check_tools || exit 1
    build_core
    build_plugins_from_selection
    link_plugins_from_selection
    ;;
  run)
    check_tools || exit 1
    build_core
    build_plugins_from_selection
    link_plugins_from_selection
    cd "${CORE}"
    exec java -jar dist/pitiupi.jar
    ;;
esac
