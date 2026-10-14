# pitiupi

Ferramenta **P2P** para práticas de Redes de Computadores: núcleo em Java com arquitetura de **plugins** (chat, compartilhamento de tela, exemplos multi-protocolo).

**Documentação completa:** [docs/README.md](docs/README.md)

## Pré-requisitos

| Ferramenta | Uso |
| --- | --- |
| **JDK** com `javac` | Compilação (OpenJDK **25** recomendado; núcleo compila com target **17**) |
| **Apache Ant** 1.10+ | `pitiupi/`, `pitiupi.chat/`, `pitiupi.screenCast/` |
| **GNU Make** | `pitiupi.mpChat/` |

Exemplo Ubuntu / WSL:

```bash
sudo apt install openjdk-25-jdk ant make
javac -version   # deve existir — JRE sozinho não basta
```

## Módulos

| Pasta | Descrição | Build |
| --- | --- | --- |
| `pitiupi/` | Núcleo (GUI, UDP multicast, TCP, heartbeat) | `ant jar` |
| `pitiupi.chat/` | Plugin referência — chat | `ant jar` |
| `pitiupi.screenCast/` | Plugin referência — screen cast | `ant jar` |
| `pitiupi.mpChat/` | Plugin referência — chat multicast + TCP | `make` |

Compile cada módulo no seu diretório (núcleo primeiro). Instale os JARs dos plugins em `pitiupi/plugins/` — ver [pitiupi/plugins/README.md](pitiupi/plugins/README.md).

Exemplo de symlinks (Linux):

```bash
cd pitiupi/plugins
ln -sf ../../pitiupi.chat/dist/pitiupi.chat.jar pitiupi.chat.jar
ln -sf ../../pitiupi.screenCast/dist/pitiupi.screenCast.jar pitiupi.screenCast.jar
ln -sf ../../pitiupi.mpChat/dist/mpChat.jar mpChat.jar
```

Execute o núcleo (a partir de `pitiupi/`):

```bash
cd pitiupi
java -jar dist/pitiupi.jar
```

Quickstart: [docs/documentation/00_quickstart.md](docs/documentation/00_quickstart.md).  
Trabalho prático (plugin próprio): [docs/documentation/10_tutorial_plugin.md](docs/documentation/10_tutorial_plugin.md).
