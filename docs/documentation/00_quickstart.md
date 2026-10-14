# Quickstart

## Pré-requisitos

- **JDK completo** (`java` e `javac`) — OpenJDK 25 recomendado; o núcleo usa `javac.source/target=17`, plugins Ant usam 25.
- Apache Ant (`pitiupi`, `pitiupi.chat`, `pitiupi.screenCast`)
- GNU Make (`pitiupi.mpChat`)

Ubuntu / WSL:

```bash
sudo apt install openjdk-25-jdk ant make
which javac ant make
```

Se `ant jar` falhar com *Cannot run program "javac"*, você instalou apenas JRE — instale `openjdk-*-jdk`.

## Build

Na raiz do clone:

```bash
cd pitiupi && ant jar
cd ../pitiupi.chat && ant jar
cd ../pitiupi.screenCast && ant jar
cd ../pitiupi.mpChat && make
```

O núcleo gera `pitiupi/dist/pitiupi.jar`. Cada plugin gera JAR em seu `dist/`.

## Instalar plugins

A aplicação lê JARs de `pitiupi/plugins/` (relativo ao diretório de trabalho ao executar o núcleo). Detalhes: [pitiupi/plugins/README.md](../../pitiupi/plugins/README.md).

Symlinks em desenvolvimento (Linux):

```bash
cd pitiupi/plugins
ln -sf ../../pitiupi.chat/dist/pitiupi.chat.jar pitiupi.chat.jar
ln -sf ../../pitiupi.screenCast/dist/pitiupi.screenCast.jar pitiupi.screenCast.jar
ln -sf ../../pitiupi.mpChat/dist/mpChat.jar mpChat.jar
```

## Executar

```bash
cd pitiupi
java -jar dist/pitiupi.jar
```

No diálogo inicial informe **nome** e **porta** (UDP/TCP). Para duas instâncias no mesmo computador, use **portas diferentes** (ex.: 5000 e 5001).

## Verificar P2P

1. Abra duas instâncias (mesma rede ou mesmo host com portas distintas).
2. Confirme que os nomes aparecem no painel de usuários online.
3. Teste um plugin de referência (ex.: chat em `MenuCategory.Social`).

Problemas de multicast: [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md).

Gate de build (quatro módulos): [15_testes_e_gate.md](15_testes_e_gate.md).

Próximo passo para o TP: [10_tutorial_plugin.md](10_tutorial_plugin.md).
