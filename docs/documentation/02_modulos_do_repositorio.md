# Módulos do repositório

```
pitiupi/                 # clone (este repositório)
├── plugins.registry.json  # catálogo de módulos (build/link via scripts/pitiupi.sh)
├── scripts/pitiupi.sh
├── pitiupi/             # núcleo P2P (executável)
├── pitiupi.chat/        # plugin referência — chat
├── pitiupi.screenCast/  # plugin referência — captura de tela
├── pitiupi.mpChat/      # plugin referência — chat multi-protocolo
└── docs/                # documentação canônica
```

## Núcleo (`pitiupi/`)

- **Build:** Apache Ant (`build.xml`, `nbproject/`).
- **Entrada:** `pitiupi.Main` → login → `MainWindow`.
- **Pacotes principais:** `pitiupi.net`, `pitiupi.control`, `pitiupi.GUI`, `pitiupi.models`.

## Plugins de referência

| Pasta | Build | JAR típico | Classe SPI |
| --- | --- | --- | --- |
| `pitiupi.chat` | `ant jar` | `dist/pitiupi.chat.jar` | `pitiupi.chat.plugin.ChatPlugin` |
| `pitiupi.screenCast` | `ant jar` | `dist/pitiupi.screenCast.jar` | `pitiupi.screenCast.plugin.ScreenCastPlugin` |
| `pitiupi.mpChat` | `make` | `dist/mpChat.jar` | `plugin.MultiProtocolChat` |

Todos dependem do JAR do núcleo no classpath de compilação.

## O que o aluno cria

Novo diretório irmão, por exemplo `pitiupi.xadrez/`, copiando a estrutura de `pitiupi.chat` (Ant + `META-INF/services`). Registre o módulo em `plugins.registry.json` e use `MenuCategory` adequado no código.

Não é obrigatório commitar o plugin do aluno neste repositório base; o professor pode definir fork ou entrega em zip.

Ver [03_build_ant_netbeans.md](03_build_ant_netbeans.md) e [10_tutorial_plugin.md](10_tutorial_plugin.md).
