# Começar

O Pitiupi abre uma janela Java. Nela você informa um nome e uma porta, vê quem está na mesma sala e abre plugins (chat, compartilhamento de tela, ping).

Plugins, cache e logs ficam em **`.pitiupi/`** dentro da pasta do projeto. Essa pasta não entra no Git. O núcleo lê os JARs de `.pitiupi/plugins/`.

Você precisa de JDK 17 ou superior (`java` e `javac`) e do Apache Ant. No Linux o script também usa `python3`.

## Linux (Ubuntu instalado na máquina)

Abra o terminal, entre na pasta do projeto e rode:

```bash
sudo apt install openjdk-21-jdk ant python3
chmod +x scripts/pitiupi.sh
./scripts/pitiupi.sh run --all
```

A janela é a do Java do Linux. Cabo e Wi-Fi são as placas deste computador.

## Windows

Abra o Prompt de Comando, entre na pasta do projeto e rode:

```bat
scripts\pitiupi.cmd run --all
```

`java`, `javac` e `ant` precisam estar no PATH. O JDK pode ser instalado com `winget install Microsoft.OpenJDK.17`. O Apache Ant é o zip de <https://ant.apache.org/bindownload.cgi>: extraia e acrescente a pasta `bin` ao PATH. Feche o cmd e abra de novo.

A janela é a do Java do Windows. Cabo e Wi-Fi são as placas deste computador.

## WSL

Abra o Ubuntu do WSL, entre na pasta do projeto e use o mesmo comando do Linux:

```bash
sudo apt install openjdk-21-jdk ant python3
chmod +x scripts/pitiupi.sh
./scripts/pitiupi.sh run --all
```

A compilação usa o `javac` e o `ant` do Ubuntu. A janela abre no Java do Windows, com as placas reais, então o JDK 17 do Windows também precisa estar instalado. Abrir o `java` de dentro do WSL, sem esse script, não enxerga o Wi-Fi do Windows. O motivo está em [rede.md](rede.md).

## Depois que a janela abrir

1. No login, escolha um **nome** e uma **porta**. Na sala, os dois colegas usam a mesma porta, em geral `5000`.
2. No mesmo computador, a segunda instância precisa de outra porta (por exemplo `5001`) e de outro nome. A mesma porta no mesmo PC não consegue abrir o socket duas vezes.
3. Abra um plugin pela aba **Plugins**, ou clique em uma pessoa na lista lateral e escolha uma ação.

Como usar a lista, as configurações e o que fazer se ninguém aparecer: [usar.md](usar.md).

## O que o comando faz

`./scripts/pitiupi.sh run --all` (ou `scripts\pitiupi.cmd run --all`):

1. Compila o núcleo com Ant (`pitiupi/dist/pitiupi.jar`).
2. Compila cada módulo listado em [`plugins.registry.json`](../plugins.registry.json) que tenha `build.xml` ou `Makefile` nesta máquina.
3. Coloca um atalho de cada JAR em `.pitiupi/plugins/`.
4. Abre a janela.

Outros comandos do script:

| Comando | O que faz |
| --- | --- |
| `build --all` | Compila e atualiza os atalhos, sem abrir a janela |
| `link --all` | Só recria os atalhos, se os JARs já existem |
| `list` | Mostra o id, a categoria e se o JAR foi gerado |
| `check` | Confere `java`, `javac`, `ant`, `python3` e o JSON |

O campo `menuCategory` do JSON (`Social`, `Jogos`, `Streaming`) só filtra o script (`--category Social`). Ele não cria menu na janela.

Para compilar à mão, na pasta do projeto:

```bash
cd pitiupi && ant jar
cd plugins/chat && ant jar
cd plugins/screencast && ant jar
cd plugins/ping && ant jar
```

Testes do núcleo: `cd pitiupi && ant test`.

## Pastas

```
pitiupi/                      clone
├── plugins.registry.json     catálogo que o script compila e vincula
├── scripts/pitiupi.sh        Linux e WSL
├── scripts/pitiupi.cmd       Windows
├── pitiupi/                  núcleo (Main, rede, janela, contrato de plugin)
├── plugins/chat
├── plugins/screencast
├── plugins/ping
└── .pitiupi/plugins          JARs que a janela carrega (gerado, não versionado)
```

O ponto de entrada é `pitiupi.Main`: prepara `.pitiupi/`, pede nome e porta e abre `MainShellFrame`.
