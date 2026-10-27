# Build — Ant e NetBeans

| Módulo | Build | Java |
| --- | --- | --- |
| `pitiupi` | Ant | 17 |
| `plugins/chat` | Ant | 17 |
| `plugins/screencast` | Ant | 17 |

```bash
cd pitiupi && ant jar
cd plugins/chat && ant jar
cd plugins/screencast && ant jar
```

Classpath dos plugins: `../../pitiupi/dist/pitiupi.jar`.

Catálogo: [`plugins.registry.json`](../../plugins.registry.json).
