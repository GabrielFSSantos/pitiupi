# Plugins

O núcleo não conhece chat, tela ou ping pelo nome. Na subida, `PluginManager` lista os `.jar` de `.pitiupi/plugins` (ou de `PITIUPI_HOME/plugins`, se você definir essa variável). Para cada arquivo cria um classloader só daquele JAR e pede ao Java as implementações de `pitiupi.models.Plugin`.

## Por que existe o arquivo SPI

SPI (Service Provider Interface) é o mecanismo padrão do Java para achar implementações sem o núcleo importar a sua classe. A classe que faz isso é `java.util.ServiceLoader`.

O `ServiceLoader` abre, dentro do JAR, um arquivo de texto com este caminho exato:

```
META-INF/services/pitiupi.models.Plugin
```

O **nome do arquivo** tem de ser o nome completo da classe que o núcleo procura (`pitiupi.models.Plugin`). O **conteúdo** é uma linha: o nome completo da sua subclasse, por exemplo:

```
pitiupi.ping.plugin.PingPlugin
```

Se o arquivo tiver outro nome, o Java não associa a lista à classe `Plugin` e o JAR sobe vazio. Se a linha não for o pacote mais o nome da classe, o carregador não instancia nada. Uma linha, sem espaço no fim, sem `.java`.

No código-fonte do módulo o arquivo fica em:

```
plugins/ping/src/META-INF/services/pitiupi.models.Plugin
```

O Ant copia `src/META-INF` para dentro do JAR (`meta.inf.dir` no `project.properties`). Depois de `ant jar`, confira:

```bash
jar tf plugins/ping/dist/pitiupi.ping.jar | grep META-INF/services
```

A saída precisa mostrar `META-INF/services/pitiupi.models.Plugin`.

## O que acontece ao abrir o programa

1. `Main` garante a pasta `.pitiupi/` e o login (nome e porta).
2. `MainShellFrame` chama `PluginManager.loadPlugins()`.
3. Cada classe listada no arquivo SPI é instanciada.
4. O núcleo chama `createPlugin(connectionManager, workspace)`.
5. O painel registrado com `bindShellWorkspaceContent` fica guardado. A aba só abre na galeria ou numa peer action.
6. Quando um UDP não é heartbeat, ou quando um frame TCP é uma `Message`, o núcleo desserializa com o classloader do JAR e chama `receiveMessage` se o tipo estiver em `getMessages()`.

Fechar a aba do plugin no topo chama `onShellTabClosed` antes de tirar o painel. Instalar ou remover um JAR não recarrega a lista em memória: a aba **Configurações** pede para reiniciar.

Cada JAR tem o próprio classloader. A desserialização usa `PluginObjectInputStream` com esse classloader, para achar `PingMessage` dentro do plugin e não dentro do núcleo. Instale só JARs em que você confia: não há sandbox. Um JAR carregado roda com o mesmo poder do programa.

## Catálogo e a aba Configurações

São duas listas diferentes.

**`plugins.registry.json`**, na raiz do clone, diz ao `./scripts/pitiupi.sh` o que compilar e o que colocar em `.pitiupi/plugins` no próximo `run`, `build` ou `link`. Cada entrada tem:

| Campo | Significado |
| --- | --- |
| `id` | Nome do atalho (`chat` vira `.pitiupi/plugins/chat.jar`) e o filtro `--plugins` |
| `dir` | Pasta do módulo, relativa à raiz |
| `build` | `ant`, `make` ou `jar` |
| `jar` | Caminho do JAR relativo a `dir` |
| `menuCategory` | `Social`, `Jogos` ou `Streaming` — só filtro do script |

`build: ant` ou `make` compila a pasta se existir `build.xml` ou `Makefile`. `build: jar` não compila: o script só vincula o arquivo que já está em `dir`. A aba **Configurações → Instalar JAR…** grava uma cópia em `.pitiupi/jar-catalog/` e acrescenta uma entrada `build: jar`. **Remover selecionado** apaga o JAR e tira essa entrada do JSON. O próximo `run` recria `.pitiupi/plugins` só com o que ainda está no catálogo: um JAR instalado e depois apagado do JSON não volta.

Se o programa rodar fora de um clone (sem `plugins.registry.json` ao lado), instalar e remover ainda copiam ou apagam o arquivo em `.pitiupi/plugins`, e o catálogo simplesmente não é alterado.

## O que cada módulo de referência faz

Os três usam a mesma casca de tela quando a atividade fica na aba central: `PluginSessionWorkspace` no corpo, `PluginRoutingPanel` no topo (destino e transporte) e `SessionTabBar` nas abas. A sala usa a chave `room`. A aba que você abriu para um IP começa com `peer:`. A aba que nasceu porque chegou tráfego, nos plugins de referência, começa com `view:` e deixa o envio somente leitura (`bindReceiveSession`).

### Chat — `plugins/chat`

`ChatPlugin` declara `ChatMessage` e `LegacyMpChatMessage`. `ChatMessage` leva o texto, o nome, o rótulo do transporte e o IP de quem enviou. Sala e um IP saem por `MessageDispatcher`: multicast, unicast ou TCP público, conforme o combo.

O texto privado é outro caminho. `connectPluginTcpAsync` abre TCP cujo destinatário é o `getIdentifier()` do chat. `receivePrivateConnection` aceita, `receiveBytes` lê o frame e `PrivateChatController` guarda a conversa. Essa conversa abre `PrivateChatWindow`, um `JFrame` separado. O painel da aba central continua dentro da shell. Um plugin novo não precisa dessa janela extra: o privado do chat é o que o código faz hoje, não o modelo da aba.

`LegacyMpChatMessage` existe para ler um formato antigo de mensagem. Não é o tipo que você cria num plugin novo.

### Screencast — `plugins/screencast`

`ScreenCastMessage` leva o nome e o `byte[]` da imagem. O combo da sala oferece UDP multicast e UDP unicast. Para um IP, oferece unicast e TCP. A captura fica em `control` (`ScreenShot`, temporizador). O painel é `ScreenCastPanel`.

Ao parar, o painel envia `SessionNoticeMessage` com fase `END` e canal `SCREEN_CAST`. O `ScreenCastPlugin` também lista essa classe em `getMessages()` e, ao receber `END`, fecha a aba de quem estava assistindo.

### Ping — `plugins/ping`

Mede RTT com `PingMessage`, `PongMessage` e `PingStopMessage`, só em UDP. A montagem arquivo por arquivo está no material de aula. Aqui basta saber que o envio passa por `MessageDispatcher` e que a aba não abre sozinha no login.

A lista lateral não desenha um selo numérico de mensagens não lidas. O recebimento aparece na aba.

Como chamar cada método: [nucleo-para-plugins.md](nucleo-para-plugins.md). Onde cada pacote mora: [nucleo.md](nucleo.md). Como a rede trata o byte: [rede.md](rede.md).
