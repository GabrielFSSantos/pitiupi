# Arquitetura da aplicação

## Visão geral

O Share My Sheet é uma plataforma P2P baseada em uma arquitetura de plugins,
desenvolvida para permitir a criação de aplicações distribuídas sobre uma
infraestrutura comum de comunicação em rede.

A aplicação fornece mecanismos de descoberta de usuários, comunicação
multicast, troca de mensagens serializadas e integração de extensões por meio
de plugins. Dessa forma, novos comportamentos podem ser adicionados sem
modificar o núcleo da aplicação.

O projeto foi desenvolvido como suporte às práticas da disciplina de Redes de
Computadores, servindo também como base para experimentação de aplicações
distribuídas e desenvolvimento de projetos acadêmicos.

## Arquitetura geral

A aplicação é dividida em três grupos principais:

- **Núcleo da aplicação**: A `MainWindow` atua como ponto central da aplicação, 
  sendo responsável por inicializar e integrar esses componentes durante a 
  execução.
- **Sistema de plugins**: responsável por adicionar funcionalidades sem
  alterar o núcleo.
- **Comunicação de rede**: responsável pela troca de mensagens entre
  instâncias da aplicação.

## Inicialização da aplicação

1. A MainWindow é criada.
2. O Socket é inicializado e inicia a comunicação multicast.
3. O HeartbeatManager é criado e inicia o envio periódico de heartbeats.
4. O painel de usuários online é registrado como listener do HeartbeatManager.
5. O PluginLoader procura e carrega os plugins disponíveis.

## Sistema de comunicação

### Socket

A classe `Socket` é responsável pela comunicação de rede da aplicação.
Ela utiliza comunicação multicast UDP para permitir que diferentes
instâncias da aplicação troquem mensagens sem a necessidade de um servidor
central.

A classe estende `Thread`, pois mantém um processo contínuo de recepção de
mensagens enquanto a aplicação continua executando suas demais atividades.

Durante sua inicialização, o socket:
- obtém o endereço do grupo multicast;
- cria um `MulticastSocket` utilizando a porta configurada;
- configura os buffers de comunicação;
- entra no grupo multicast.

#### Envio de mensagens

O método `send()` recebe uma mensagem já serializada em bytes e cria um
`DatagramPacket`, enviando-o para o grupo multicast.

De forma geral, o socket atua apenas como mecanismo de transporte das mensagens. 
A única exceção é o tratamento de HeartbeatMessage, utilizado pela infraestrutura
da aplicação para manter a lista de usuários ativos.

#### Recepção de mensagens

O método `receive()` mantém um loop aguardando novas mensagens multicast.

Ao receber uma mensagem:
1. Os dados são analisados para verificar se correspondem a um
   `HeartbeatMessage`.
2. Caso seja um heartbeat, a mensagem é encaminhada ao `HeartbeatManager`
   para atualização dos usuários ativos.
3. Caso contrário, a mensagem é enviada para todos os plugins carregados
   através do método `receiveMessage()`.

Esse comportamento permite que a aplicação principal trate mensagens de
infraestrutura (como heartbeat), enquanto mensagens específicas de plugins
são processadas pelas extensões correspondentes.

### Message

A classe `Message` representa a estrutura base das mensagens trocadas pela
aplicação.

Todas as mensagens utilizadas pelo sistema devem herdar dessa classe. Ela
implementa `Serializable`, permitindo que objetos de mensagem sejam
convertidos em vetores de bytes para transmissão pela rede.

A serialização é realizada pelo método `toByteArray()`, que transforma uma
instância da mensagem em uma representação binária enviada pelo `Socket`.

Mensagens específicas devem estender essa classe adicionando os atributos e
comportamentos necessários para cada funcionalidade.

### Heartbeat

O mecanismo de heartbeat é utilizado para identificar quais usuários estão
ativos na rede.

A aplicação envia periodicamente uma `HeartbeatMessage` contendo informações
do usuário e um timestamp. Quando outra instância recebe essa mensagem, o
`HeartbeatManager` atualiza o registro do usuário correspondente.

O fluxo de heartbeat é independente das mensagens dos plugins:

1. `HeartbeatManager` cria uma `HeartbeatMessage`.
2. A mensagem é serializada utilizando `Message.toByteArray()`.
3. O `Socket` transmite a mensagem pela rede multicast.
4. O `Socket` identifica mensagens de heartbeat recebidas e encaminha ao
   `HeartbeatManager`.
5. O `HeartbeatManager` atualiza a lista de usuários ativos e notifica os
   listeners.

## Sistema de plugins

### Interface Plugin

A interface `Plugin` define o contrato que deve ser implementado por qualquer
extensão da aplicação.

Um plugin deve fornecer informações básicas de identificação (`name`,
`author` e `version`), inicializar seu estado através de `createPlugin()` e
participar do sistema de comunicação utilizando mensagens derivadas de
`Message`.

Os principais métodos são:

#### Identificação

Os métodos:
- `getName()`
- `getAuthor()`
- `getVersion()`

fornecem informações utilizadas pela aplicação para identificar o plugin.

#### Inicialização

`createPlugin(MainWindow window)`

É chamado após o carregamento do plugin e permite que ele registre seus
componentes na aplicação, como menus ou elementos de interface.

#### Comunicação

`getMessage()`

Deve retornar uma mensagem utilizada pelo plugin para comunicação com outras
instâncias da aplicação.

`receiveMessage(byte[] message)`

É chamado pela aplicação sempre que uma mensagem é recebida. Cada plugin deve
verificar se a mensagem pertence ao seu tipo e realizar o processamento
correspondente.

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
        +---pitiupi.plugin.Plugin
```

O arquivo `pitiupi.plugin.Plugin` deve conter o nome completo das classes que
implementam a interface Plugin:

```
MeusPlugins.plugins.NomeDaClasse
```

Para serem carregados, os plugins devem estar empacotados como arquivos JAR
e colocados no diretório plugins da aplicação.

### Carregamento

Ao iniciar a aplicação, o `PluginLoader` executa os seguintes passos:

1. Procura arquivos `.jar` dentro do diretório `plugins`.
2. Cria um `URLClassLoader` contendo os JARs encontrados.
3. Utiliza `ServiceLoader` para localizar classes que implementam
   `Plugin`.
4. Instancia cada plugin encontrado.
5. Chama o método `createPlugin(MainWindow)`.
6. Registra o plugin na lista de plugins ativos da aplicação.

## Interface gráfica

A interface gráfica da aplicação é baseada em Swing e é centralizada na
classe `MainWindow`.

Durante a inicialização, cada plugin recebe uma referência para a
`MainWindow` através do método `createPlugin(MainWindow)`.

Essa referência permite integrar componentes gráficos à aplicação, sendo a
principal forma de extensão a adição de menus à barra de menus:

```java
JMenu menu = new JMenu("Meu Plugin");
window.addMenu(menu);