# Quickstart

## Pré-requisitos

- **JDK completo** (`java` e `javac`) — OpenJDK 25 recomendado; o núcleo usa `javac.source/target=17`, plugins Ant usam 25.
- Apache Ant (`pitiupi`, `plugins/chat`, `plugins/screencast`)

Ubuntu / WSL:

```bash
sudo apt install openjdk-25-jdk ant
which javac ant
```

## Build

Na raiz do clone:

```bash
./scripts/pitiupi.sh build --all
```

Ou manualmente:

```bash
cd pitiupi && ant jar
cd plugins/chat && ant jar
cd plugins/screencast && ant jar
```

## Instalar plugins

Runtime: JARs em **`PITIUPI_HOME/plugins`** (padrão `~/pitiupi/plugins`).

```bash
./scripts/pitiupi.sh link --all
```

## Executar

```bash
./scripts/pitiupi.sh run --all
```

Login com nome e porta; abra plugins pela aba **Plugins**. Clique em **Todos** ou em um peer na lista lateral para ver ações de cada plugin.

Validação rápida do núcleo: `cd pitiupi && ant test`.
