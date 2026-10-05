# Como criar plugins para o Pitiupi

## Pitiupi

A classe `Pitiupi` é a interface que o seu plugin
vai receber quando for criado, ela fornece os métodos
necessários para interagir com a interface gráfica e
comunicação para outros peers.

### sendUdpMulticast(Message message)

Envia uma mensagem multicast, retorna `boolean` indicando 
se teve sucesso.

### sendUdpUnicast(Message message, InetAddress address)

Envia uma mensagem unicast via udp para o endereço fornecido,
retorna `boolean` indicando se teve sucesso.

### sendTcp(Message message, InetAddress address)

Envia uma mensagem unicast via tcp para o endereço fornecido,
retorna `boolean` indicando se teve sucesso.

### connectTcp(InetAddress address, String senderId, String receiverId)

Pede uma conexão tcp privada para o seu plugin com outro usuário,
para utilizar essa funcionalidade é necessário implementar alguns
métodos opcionais em Plugin. Mais informações serão discutidas na
seção TcpConnection.

### getUserName()

Retorna o nome do usuário conectado

### getPort()

Retorna a porta que o usuário está usando o Pitiupi

### createMenu(MenuCategory category, Menu menu)

Adiciona um menu ao pitiupi, mais detalhes são encontrados
na seção sobre Menus.

## Classe Plugin

Essa é a classe que o pitiupi vai procurar ao
carregar o seu plugin, ela define um contrato
de métodos que todo plugin deve possuir.

### getName()

Indica o nome público do plugin.

### getAuthor()

Indica o nome dos desenvolvedores do plugin.

### getVersion()

Indica a versão atual do plugin.

### getIdentifier()

Indica um id que o pitiupi vai utilizar para
enontrar seu plugin, por isso é importante que
ele seja bem especifico para evitar que dois
plugins sejam confundidos durante a comunicação.
Procure usar algo como: MeuPlugin.Autor.Números

### createPlugin(Pitiupi pitiupi)

Após o pitiupi identificar e carregar o seu plugin,
ele vai chamar esse método para inicializá-lo, por 
isso pense nele como o construtor do seu plugin, 
é aqui que você inicializa atributos, adiciona os 
menus inciais, etc.

### getMessages()

Esse método indica ao pitiupi quais tipos de mensagem
seu plugin quer receber, normalmente as que ele mesmo
implementar.
Nesse caso, é normal que ele faça:
```
return List.of(
    SuaMensagem1.class, 
    SuaMensagem2.class,
    ...
    );
```

Caso você queira que seu plugin receba todo tipo de
mensagem, existe um método `receiveAllTypes()` que
ele pode sobreescrever para receber todas mensagens
que não sejam consideradas privadas.

### receiveMessage(Message message)

Esse é o método que o pitiupi vai chamar quando quiser
te entregar uma mensagem pelo canal publico de mensagens.
Note que ele entrega uma `Message`, então é necessário que
seu plugin confira o tipo real ele mesmo. Como em:

```
if (message instanceOf MinhaMensagem minhaMensagem) {...}
```
O pitiupi garante que é uma mensagem de um tipo
que o seu plugin diz aceitar.


