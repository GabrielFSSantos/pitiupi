# Plugins instalados

O núcleo carrega arquivos `.jar` desta pasta ao iniciar (relativo ao diretório de trabalho — execute `java -jar dist/pitiupi.jar` a partir de `pitiupi/`).

## Desenvolvimento

Após compilar os módulos de referência, use symlinks (Linux):

```bash
cd pitiupi/plugins
ln -sf ../../pitiupi.chat/dist/pitiupi.chat.jar pitiupi.chat.jar
ln -sf ../../pitiupi.screenCast/dist/pitiupi.screenCast.jar pitiupi.screenCast.jar
ln -sf ../../pitiupi.mpChat/dist/mpChat.jar mpChat.jar
```

Ordem de build: núcleo (`pitiupi`) primeiro, depois cada plugin. Ver [docs/documentation/00_quickstart.md](../../docs/documentation/00_quickstart.md).

Os JARs gerados não devem ser commitados — apenas este README e symlinks locais.
