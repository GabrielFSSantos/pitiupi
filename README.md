# pitiupi

Ferramenta **P2P** para práticas de Redes de Computadores: núcleo em Java com arquitetura de **plugins** (conversa na rede e compartilhamento de tela).

**Documentação:** [docs/README.md](docs/README.md) · **Quickstart:** [docs/comecar.md](docs/comecar.md)

---

## Como rodar

O comando compila o núcleo, os plugins do [catálogo](plugins.registry.json) e abre a janela. Plugins, cache e logs ficam em **`.pitiupi/`** dentro da pasta do projeto.

Depois que a janela abrir: informe **nome** e **porta** (na sala, em geral `5000`). Os dois colegas usam a mesma porta. Abra os plugins na aba **Plugins**.

### Linux (Ubuntu instalado na máquina)

Abra o terminal, entre na pasta do projeto e rode:

```bash
sudo apt install openjdk-21-jdk ant python3
chmod +x scripts/pitiupi.sh
./scripts/pitiupi.sh run --all
```

A janela é a do Java do Linux. Cabo e Wi-Fi são as placas deste computador.

### Windows

Abra o **Prompt de Comando**: tecla Windows, digite `cmd` e pressione Enter. Entre na pasta em que o projeto foi baixado e rode:

```bat
cd %USERPROFILE%\Downloads\pitiupi
scripts\pitiupi.cmd run --all
```

Se a pasta for outra, troque o `cd`. No PowerShell o comando é o mesmo: `scripts\pitiupi.cmd run --all`.

`java`, `javac` e `ant` precisam estar no PATH. No PowerShell, o JDK:

```powershell
winget install Microsoft.OpenJDK.17
```

O Apache Ant é o zip de <https://ant.apache.org/bindownload.cgi>: extraia e acrescente a pasta `bin` ao PATH. Feche o cmd e abra de novo.

A janela é a do Java do Windows. Cabo e Wi-Fi são as placas deste computador.

### WSL

Abra o **Ubuntu** do WSL (não o cmd). Entre na pasta do projeto e use o comando do Linux:

```bash
sudo apt install openjdk-21-jdk ant python3
chmod +x scripts/pitiupi.sh
./scripts/pitiupi.sh run --all
```

Se o projeto foi baixado no Windows, por exemplo em Downloads:

```bash
cd /mnt/c/Users/SEU_USUARIO/Downloads/pitiupi
chmod +x scripts/pitiupi.sh
./scripts/pitiupi.sh run --all
```

A compilação usa o `javac` e o `ant` do Ubuntu. A janela abre no Windows, com as placas reais. O JDK 17 do Windows também precisa estar instalado (`winget install Microsoft.OpenJDK.17` no PowerShell).

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
| `dir` | Pasta do módulo (ex.: `plugins/xadrez`) |
| `build` | `ant`, `make` ou `jar` (arquivo já pronto, sem compilar) |
| `jar` | Caminho do JAR relativo a `dir` |
| `menuCategory` | `Social`, `Jogos` ou `Streaming` — filtro CLI e documentação |

Plugins de referência hoje:

| id | Uso didático | Pasta |
| --- | --- | --- |
| `chat` | Sala geral, peer por IP, TCP privado | `plugins/chat` |
| `screencast` | Multicast ou unicast de tela | `plugins/screencast` |

Na interface, o texto das abas vem de `getDisplayName()` em cada plugin. O campo `menuCategory` no JSON alinha build/link com a documentação.

```bash
./scripts/pitiupi.sh run --all
./scripts/pitiupi.sh run --category Social
./scripts/pitiupi.sh run --plugins chat,screencast
./scripts/pitiupi.sh discover   # pastas em plugins/ com SPI ainda não no catálogo
```

---

## Novo plugin (resumo)

1. Copiar a estrutura de `plugins/chat` para `plugins/meuPlugin` ([plugins](docs/plugins.md), [funções do núcleo](docs/nucleo-para-plugins.md)).
2. Em `createPlugin`, registrar o painel com `bindShellWorkspaceContent` e, se fizer sentido, peer actions na lista lateral.
3. Adicionar entrada em [`plugins.registry.json`](plugins.registry.json), ou instalar o JAR pela aba **Configurações** (a instalação grava o catálogo).
4. `./scripts/pitiupi.sh link --all` e reiniciar o app.

O núcleo carrega **todos os `*.jar`** em `PITIUPI_HOME/plugins`. Detalhes: [pitiupi/plugins/README.md](pitiupi/plugins/README.md).

---

## Pré-requisitos

| Onde | O que instalar |
| --- | --- |
| Linux e Ubuntu do WSL | `sudo apt install openjdk-21-jdk ant python3` |
| Windows (e o Windows ao lado do WSL) | JDK 17 (`winget install Microsoft.OpenJDK.17`) e Apache Ant no PATH |

Conferir no Linux ou no WSL: `./scripts/pitiupi.sh check`. Testes do núcleo: `cd pitiupi && ant test`.

---

## Comandos do script

| Comando | Ação |
| --- | --- |
| `run` | Build + link + app |
| `build` | Build + link |
| `link` | Só symlinks (JARs já existem) |
| `list` | Catálogo e status do JAR |
| `discover` | Sugestão de módulos com SPI |
| `check` | PATH + JSON válido |

Opções: `--all` (padrão), `--plugins ids`, `--category Social|Jogos|Streaming`.

---

## Teste em rede

- **Mesmo PC:** duas instâncias, portas distintas (ex. 5000 e 5001).
- **Dois PCs na mesma Wi‑Fi:** mesma porta (ex. 5000), nomes diferentes. Cada um segue o caso da própria máquina em [Como rodar](#como-rodar). Linux abre o Java do Linux; Windows e WSL abrem o Java do Windows.

Ver [docs/usar.md](docs/usar.md) e [docs/rede.md](docs/rede.md).

---

## Origem

Projeto base UFSJ (Redes). GitHub: [GabrielFSSantos/pitiupi](https://github.com/GabrielFSSantos/pitiupi).
