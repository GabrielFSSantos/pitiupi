# Quickstart

O `run --all` compila o núcleo, os plugins do catálogo e abre a janela. Plugins, cache e logs ficam em **`.pitiupi/`** na pasta do projeto. JDK **17 ou superior** (`java` e `javac`) e Apache Ant. O script do Linux também usa `python3`.

## Linux (Ubuntu instalado na máquina)

Abra o terminal, entre na pasta do projeto e rode:

```bash
sudo apt install openjdk-21-jdk ant python3
chmod +x scripts/pitiupi.sh
./scripts/pitiupi.sh run --all
```

A janela é a do Java do Linux. Cabo e Wi-Fi são as placas deste computador.

## Windows

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

## WSL

Abra o **Ubuntu** do WSL, não o cmd. Entre na pasta do projeto e use o comando do Linux:

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

A compilação usa o `javac` e o `ant` do Ubuntu. A janela abre no Windows, com as placas reais, então o JDK 17 do Windows também precisa estar instalado (`winget install Microsoft.OpenJDK.17` no PowerShell). Abrir o `java` de dentro do WSL, sem esse comando, não mostra o Wi-Fi do Windows. O motivo está em [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md).

## Depois que a janela abrir

Login com nome e porta (na sala, em geral `5000` nos dois). Abra plugins pela aba **Plugins**. Clique em **Todos** ou em um peer na lista lateral para ver as ações de cada plugin.

Validação rápida do núcleo: `cd pitiupi && ant test`.

## Build sem abrir a janela

No Linux ou no WSL, na pasta do projeto:

```bash
./scripts/pitiupi.sh build --all
./scripts/pitiupi.sh link --all
```

Ou manualmente:

```bash
cd pitiupi && ant jar
cd plugins/chat && ant jar
cd plugins/screencast && ant jar
```
