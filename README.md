# pitiupi

Ferramenta **P2P** para práticas de Redes de Computadores: núcleo em Java com arquitetura de **plugins** (chat, compartilhamento de tela, chat multi-protocolo).

**Documentação completa:** [docs/README.md](docs/README.md) · **Quickstart:** [docs/documentation/00_quickstart.md](docs/documentation/00_quickstart.md)

---

## Início rápido (um comando)

Na **raiz do repositório**:

```bash
chmod +x scripts/pitiupi.sh   # só na primeira vez
./scripts/pitiupi.sh run --all
```

Compila o núcleo, os plugins do [catálogo](plugins.registry.json), instala JARs em `pitiupi/plugins/` e abre a aplicação.

1. Login: **nome** e **porta** (ex.: `5000`).
2. Menus dos plugins: barra **Plugin → Social | Jogos | Streaming** (cada plugin registra a categoria no código).
3. P2P no mesmo PC: segunda instância com **porta diferente** (ex.: `5001`).

```bash
./scripts/pitiupi.sh list      # ids, categorias, JAR buildado?
./scripts/pitiupi.sh check     # ferramentas + catálogo
```

---

## Catálogo de plugins (`plugins.registry.json`)

Novos módulos entram no repositório adicionando uma entrada no JSON (não é preciso editar o script):

| Campo | Significado |
| --- | --- |
| `id` | Usado em `--plugins chat,meuJogo` |
| `dir` | Pasta do módulo (ex.: `pitiupi.xadrez`) |
| `build` | `ant` ou `make` |
| `jar` | Caminho do JAR relativo a `dir` |
| `menuCategory` | `Social`, `Jogos` ou `Streaming` — filtro CLI e documentação |

Plugins de referência hoje:

| id | Categoria (menu) | Pasta |
| --- | --- | --- |
| `chat` | Social | `pitiupi.chat` |
| `mpchat` | Social | `pitiupi.mpChat` |
| `screencast` | Streaming | `pitiupi.screenCast` |

A **categoria na interface** vem do `MenuCategory` no Java (`guiManager.createMenu(...)`), não do nome do JAR. O campo `menuCategory` no JSON alinha build/link com a doc.

```bash
./scripts/pitiupi.sh run --all
./scripts/pitiupi.sh run --category Social
./scripts/pitiupi.sh run --plugins chat,screencast
./scripts/pitiupi.sh discover   # pastas pitiupi.* com SPI ainda não no catálogo
```

---

## Novo plugin (resumo)

1. Copiar estrutura de `pitiupi.chat` → `pitiupi.meuPlugin` ([tutorial](docs/documentation/10_tutorial_plugin.md)).
2. Em `createPlugin`, registrar menu: `guiManager.createMenu(MenuCategory.Jogos, ...)` (ou Social / Streaming).
3. Adicionar entrada em [`plugins.registry.json`](plugins.registry.json).
4. `./scripts/pitiupi.sh run --plugins meuId`.

O núcleo carrega **todos os `*.jar`** em `pitiupi/plugins/` (pasta única). Detalhes: [pitiupi/plugins/README.md](pitiupi/plugins/README.md).

---

## Pré-requisitos

| Ferramenta | Uso |
| --- | --- |
| **JDK** com `javac` | Compilação (OpenJDK **25** recomendado) |
| **Apache Ant** | Módulos NetBeans |
| **GNU Make** | `pitiupi.mpChat` |
| **python3** | Script `scripts/pitiupi.sh` (ler o catálogo) |

```bash
sudo apt install openjdk-25-jdk ant make
./scripts/pitiupi.sh check
```

**GUI:** Swing — WSLg / `DISPLAY=:0`.

---

## Comandos do script

| Comando | Ação |
| --- | --- |
| `run` | Build + `plugins/` + app |
| `build` | Build + `plugins/` |
| `link` | Só symlinks (JARs já existem) |
| `list` | Catálogo e status do JAR |
| `discover` | Sugestão de módulos `pitiupi.*` |
| `check` | PATH + JSON válido |

Opções: `--all` (padrão), `--plugins ids`, `--category Social|Jogos|Streaming`.

---

## Teste em rede

Duas instâncias, portas distintas, mesma LAN. Ver [docs/documentation/15_testes_e_gate.md](docs/documentation/15_testes_e_gate.md) e [appendix_portas_e_multicast.md](docs/documentation/appendix_portas_e_multicast.md).

---

## Origem

Projeto base UFSJ (Redes). GitHub: [GabrielFSSantos/pitiupi](https://github.com/GabrielFSSantos/pitiupi).
