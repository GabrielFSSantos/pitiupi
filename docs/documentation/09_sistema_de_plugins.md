# Sistema de plugins

O sistema de plugins permite extensão modular sem alterar o núcleo. O contrato
está em `pitiupi.models.Plugin` (classe abstrata).

## Interface Plugin

Métodos obrigatórios:

| Método | Função |
| --- | --- |
| `getName()`, `getAuthor()`, `getVersion()`, `getIdentifier()` | Identificação (o `identifier` distingue plugins em conexões TCP dedicadas) |
| `getDisplayName()` | Texto na galeria e título de aba (padrão: `getName()`) |
| `createPlugin(ConnectionManager, PluginWorkspace)` | Inicialização na shell: painel na aba, peer actions, rede |
| `createPlugin(ConnectionManager, GuiManager)` | Legado — adapter na carga; não usar em plugins novos |
| `getMessages()` | Lista de classes `Message` que o plugin desserializa |
| `receiveMessage(Message)` | Processamento após o `PluginManager` identificar o tipo |

Métodos opcionais (ciclo de vida e TCP avançado):

| Método | Função |
| --- | --- |
| `onShellTabClosed()` | Usuário fechou a aba do plugin no topo; liberar timers, captura, envio contínuo |
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

O catálogo de build (`plugins.registry.json`) cobre só **chat** e **screencast** no CI. Exercícios (ex. [ping](17_tutorial_ping_aula.md)) ficam fora do repositório — o aluno instala o JAR localmente após `ant jar`.

### Carregamento de plugins

O `PluginManager.loadPlugins()` (chamado pela `MainShellFrame`):

1. Lista arquivos `.jar` em `PITIUPI_HOME/plugins` (ver [00_quickstart.md](00_quickstart.md); symlinks de dev via `./scripts/pitiupi.sh link`).
2. Para cada JAR, cria um `URLClassLoader` e usa `ServiceLoader.load(Plugin.class, classLoader)`.
3. Chama `createPlugin(connectionManager, pluginWorkspace)` em cada instância (`ShellPluginWorkspace`).
4. Na recepção UDP/TCP, tenta desserializar com `PluginObjectInputStream` e despacha para plugins que declararam o tipo em `getMessages()`.

Ao fechar uma aba dinâmica de plugin, `TabWorkspacePanel` notifica `PluginManager.notifyShellTabClosed(pluginId)` **antes** de remover o conteúdo, para o plugin encerrar trabalho em segundo plano.

Implementação: `pitiupi/src/pitiupi/control/plugins/PluginManager.java`.

## Shell e abas (PluginWorkspace)

Na `MainShellFrame`, `createPlugin(ConnectionManager, PluginWorkspace)` registra o painel com `bindShellWorkspaceContent(JPanel)` **sem** abrir aba no boot. A galeria **Plugins** e as peer actions abrem a aba sob demanda.

## Peer actions na lista lateral

`PeerActionBroker.getAllActionsFor(target)` agrupa ações de **todos** os plugins (submenu por `getDisplayName()`). Destinos: `SINGLE_PEER` (um usuário) ou `ALL_PEERS` (linha **Todos**).

## Envio didático (núcleo)

`PluginRoutingPanel` (núcleo) padroniza destino, forma de envio e dica embaixo para chat, screencast e futuros plugins.

`MessageDispatcher` envia com `TransportMode` e `SendTarget`. `PluginTransportCatalog` define quais transportes cada plugin mostra na UI (sala geral vs um IP). Os combos usam textos didáticos (`describeForUi`) com o termo técnico entre parênteses (multicast UDP, UDP unicast, TCP). Na sala geral o campo IP fica desativado; um resumo **“Como esta mensagem vai”** (chat) deixa explícito o transporte escolhido — na sala pode ser multicast ou cópias unicast/TCP para cada pessoa, conforme o plugin. Para um peer use o IP manualmente ou a peer action na lista lateral.

Em abas de **recebimento** (ex. screencast `view:`), `PluginRoutingPanel.bindReceiveSession` deixa IP e forma de envio somente leitura, refletindo o que o remetente usou.

### Avisos de sessão (D24)

`SessionNoticeMessage` (núcleo) sinaliza fases `START` / `END` por `pluginId` e canal (`SessionNoticeChannel`, ex. `screen_cast`). Plugins registram o tipo em `getMessages()` e reagem na UI (ex. fechar aba de recebimento ao `END` de compartilhamento). Campos incluem remetente, escopo (broadcast/direto) e `TransportMode` observado.
