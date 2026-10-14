# Comunicação UDP


A classe `SocketUDP` é responsável pela comunicação UDP da aplicação.

Ela utiliza um `MulticastSocket` associado à porta configurada pela aplicação e 
ingressa no grupo multicast `224.0.0.3`. Por ser baseada em UDP, a comunicação não 
estabelece uma conexão permanente entre os peers.

A classe estende `Thread`, mantendo um processo contínuo de recepção de datagramas 
enquanto a aplicação está em execução.

O SocketUDP suporta dois modos de envio:

- **multicast**, destinado ao grupo de peers
- **unicast**, destinado diretamente ao endereço IP de um peer específico

### Inicialização

Durante sua inicialização, o `SocketUDP`:

1. obtém o endereço do grupo multicast;
2. cria um `MulticastSocket` utilizando a porta configurada na `MainWindow`;
3. configura os buffers de envio e recepção;
4. habilita o reuso do endereço;
5. ingressa no grupo multicast.

O tamanho dos buffers de comunicação é configurado para `256000` bytes.

### Envio de mensagens UDP

O `SocketUDP` suporta dois modos de envio de mensagens: multicast e unicast, ambos 
baseados no mesmo mecanismo interno de criação e envio de DatagramPacket.

O método `sendMulticast()` envia uma mensagem para o grupo multicast da aplicação. 
Nesse caso, o `DatagramPacket` é direcionado ao endereço multicast configurado, 
permitindo que todas as instâncias participantes do grupo recebam a mensagem 
simultaneamente.

Já o método `sendUnicast()` permite enviar uma mensagem diretamente para um peer 
específico. Nesse caso, o `DatagramPacket` é direcionado ao endereço IP informado pelo 
chamador, mantendo a mesma porta utilizada pela aplicação.

Ambos os métodos são, na prática, abstrações de um único mecanismo interno de envio, 
que utiliza o mesmo processo de criação e envio de `DatagramPacket` por baixo dos 
panos. A diferença entre eles está apenas no endereço de destino: enquanto o multicast 
utiliza o grupo compartilhado, o unicast direciona o pacote a um IP específico.

### Recepção de mensagens

O método `receive()` mantém um loop aguardando novos datagramas.

Ao receber uma mensagem, o `SocketUDP` inicialmente tenta identificar se os dados 
correspondem a uma `HeartbeatMessage`.

O processamento segue o seguinte fluxo:

1. O datagrama é recebido pelo `MulticastSocket`.
2. Os dados recebidos são analisados pelo método `tryParseHeartbeat()`.
3. Caso seja identificada uma `HeartbeatMessage`, ela é encaminhada ao 
   `HeartbeatManager`, juntamente com o endereço IP de origem.
4. Caso não seja um heartbeat, a mensagem é encaminhada para todos os plugins 
   carregados.
5. Cada plugin decide se a mensagem recebida pertence à sua funcionalidade.

Essa separação mantém as mensagens de infraestrutura, como heartbeat, sob 
responsabilidade do núcleo da aplicação, enquanto as mensagens específicas das 
funcionalidades são processadas pelos plugins.

### Identificação de mensagens de heartbeat

O método `tryParseHeartbeat()` tenta desserializar os dados recebidos e verificar se o 
objeto resultante é uma instância de `HeartbeatMessage`.

Caso a mensagem não possa ser desserializada ou não corresponda ao tipo esperado, o 
método retorna `null`. Nesse caso, a mensagem é tratada como uma mensagem destinada aos
plugins.

### Encerramento

O método `close()` interrompe a execução do processo de recepção, remove a aplicação 
do grupo multicast e fecha o `MulticastSocket`.

A variável `running` é utilizada para sinalizar que o processo de recepção deve ser
encerrado.
