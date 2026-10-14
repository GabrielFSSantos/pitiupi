# Sistema de plugins

O sistema de plugins permite extensão modular sem alterar o núcleo. O contrato
está em `pitiupi.models.Plugin` (classe abstrata).

## Interface Plugin

Métodos obrigatórios:

| Método | Função |
| --- | --- |
| `getName()`, `getAuthor()`, `getVersion()`, `getIdentifier()` | Identificação (o `identifier` distingue plugins em conexões TCP dedicadas) |
| `createPlugin(ConnectionManager, GuiManager)` | Inicialização: menus, janelas, uso da rede |
| `getMessages()` | Lista de classes `Message` que o plugin desserializa |
| `receiveMessage(Message)` | Processamento após o `PluginManager` identificar o tipo |

Métodos opcionais (TCP avançado):

| Método | Função |
| --- | --- |
| `receiveAllTypes()` | Receber todas as mensagens públicas |
| `receivePrivateConnection(TcpConnection)` | Aceitar conexão TCP direcionada ao plugin |
| `receiveBytes(TcpConnection, byte[])` | Ler payload bruto na conexão privada |
| `onSocketClosed(TcpConnection)` | Limpar estado ao fechar socket |

### Descoberta de plugins

Para serem reconhecidos, os plugins devem possuir os arquivos de configuração
do Java Service Provider Interface (SPI):

```
MeusPlugins
|
+---META-INF
    |
    +---services
        |
        +---pitiupi.models.Plugin
```

O arquivo `pitiupi.models.Plugin` deve conter o nome completo das classes que
implementam a interface Plugin:

```
MeusPlugins.plugins.NomeDaClasse
```

Para serem carregados, os plugins devem estar empacotados como arquivos JAR
e colocados no diretório plugins da aplicação.

### Carregamento de plugins

O `PluginManager.loadPlugins()` (chamado pela `MainWindow`):

1. Lista arquivos `.jar` em `pitiupi/plugins/` (diretório de trabalho = pasta do núcleo).
2. Para cada JAR, cria um `URLClassLoader` e usa `ServiceLoader.load(Plugin.class, classLoader)`.
3. Chama `createPlugin(connectionManager, guiManager)` em cada instância.
4. Na recepção UDP/TCP, tenta desserializar com `PluginObjectInputStream` e despacha para plugins que declararam o tipo em `getMessages()`.

Implementação: `pitiupi/src/pitiupi/control/plugins/PluginManager.java`.
