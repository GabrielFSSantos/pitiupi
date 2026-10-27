# Plugins instalados (runtime)

O núcleo carrega **todos os arquivos `.jar`** de `PITIUPI_HOME/plugins` ao iniciar (padrão `~/pitiupi/plugins`). Variável de ambiente opcional: `PITIUPI_HOME`.

## Instalação automática (recomendado)

Na raiz do repositório, o catálogo [`plugins.registry.json`](../../plugins.registry.json) lista módulos buildáveis:

```bash
./scripts/pitiupi.sh build --all
./scripts/pitiupi.sh link --all
```

Symlinks: `{PITIUPI_HOME}/plugins/{id}.jar` → JAR buildado em `plugins/{módulo}/dist/` no clone.

## Manual

```bash
ln -sf /caminho/ao/clone/plugins/chat/dist/pitiupi.chat.jar ~/pitiupi/plugins/chat.jar
```

Não commite `.jar` binários no Git — apenas este README.

Novo módulo: registrar no catálogo e ver [docs/documentation/10_tutorial_plugin.md](../../docs/documentation/10_tutorial_plugin.md).
