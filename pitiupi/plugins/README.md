# Plugins instalados

O núcleo carrega **todos os arquivos `.jar`** desta pasta ao iniciar. Execute sempre a partir de `pitiupi/`:

```bash
java -jar dist/pitiupi.jar
```

## Pasta única (sem subpastas por categoria)

Os JARs ficam lado a lado em `plugins/`. A divisão **chat / jogo / streaming** aparece no menu **Plugin → Social | Jogos | Streaming**, definida por cada plugin com `MenuCategory` no código.

## Instalação automática (recomendado)

Na raiz do repositório, o catálogo [`plugins.registry.json`](../../plugins.registry.json) lista módulos buildáveis:

```bash
./scripts/pitiupi.sh link --all
./scripts/pitiupi.sh build --plugins chat
```

Symlinks gerados: `plugins/{id}.jar` → `../../{módulo}/{jar}`.

## Manual

```bash
cd pitiupi/plugins
ln -sf ../../pitiupi.chat/dist/pitiupi.chat.jar chat.jar
```

Não commite `.jar` — apenas este README e symlinks locais.

Novo módulo: registrar no catálogo e ver [docs/documentation/10_tutorial_plugin.md](../../docs/documentation/10_tutorial_plugin.md).
