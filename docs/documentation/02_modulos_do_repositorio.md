# Módulos do repositório

```
pitiupi/                   # clone (este repositório)
├── plugins.registry.json
├── scripts/pitiupi.sh
├── pitiupi/               # núcleo P2P (executável)
├── plugins/
│   ├── chat/              # chat unificado (UDP/multicast/TCP + TCP privado)
│   └── screencast/        # compartilhamento de tela
└── docs/
```

## Núcleo (`pitiupi/`)

- **Build:** Apache Ant.
- **Entrada:** `pitiupi.Main` → login → `MainShellFrame`.
- **Messaging:** `pitiupi.application.messaging.MessageDispatcher`.

## Plugins de referência

| Pasta | Build | JAR típico | SPI |
| --- | --- | --- | --- |
| `plugins/chat` | `ant jar` | `dist/pitiupi.chat.jar` | `pitiupi.chat.plugin.ChatPlugin` |
| `plugins/screencast` | `ant jar` | `dist/pitiupi.screenCast.jar` | `pitiupi.screenCast.plugin.ScreenCastPlugin` |

Novo módulo: [10_tutorial_plugin.md](10_tutorial_plugin.md).
