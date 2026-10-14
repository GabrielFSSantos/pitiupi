# Heartbeat

### Heartbeat

O mecanismo de heartbeat é utilizado para identificar quais usuários estão ativos 
na rede.

A aplicação envia periodicamente uma `HeartbeatMessage` contendo informações do usuário,
um identificador único da instância da aplicação e um timestamp.

Essas mensagens são transmitidas utilizando a comunicação UDP multicast.

Quando outra instância recebe uma mensagem de heartbeat, o `SocketUDP` identifica 
o tipo da mensagem e encaminha o objeto ao `HeartbeatManager`, juntamente com o endereço
IP de origem.

O `HeartbeatManager` registra ou atualiza o peer correspondente, utilizando o 
identificador da instância para diferenciá-lo das demais.

Um peer é considerado inativo quando permanece mais de 15 segundos sem receber um novo 
heartbeat. O `HeartbeatManager` então remove o peer da lista e notifica todos os 
componentes registrados como listeners de mudanças de peers.

Esses listeners podem incluir diferentes partes da aplicação, como componentes 
da interface gráfica, mecanismos de comunicação (como o `SocketTCP`) e também plugins 
que desejem reagir à presença ou ausência de usuários na rede.

Dessa forma, o sistema permite que tanto o núcleo da aplicação quanto extensões 
externas sejam notificados sobre alterações no estado da rede, mantendo a arquitetura 
flexível e extensível.

O fluxo completo é:

```
HeartbeatManager 
| 
| cria HeartbeatMessage 
|
v 
Message.toByteArray() 
| 
v 
SocketUDP.sendMulticast() 
| 
v 
UDP Multicast 
| 
v 
SocketUDP.receive() 
| 
v 
HeartbeatManager.receiveHeartbeat() 
| 
v 
Lista de peers ativos 
| 
+----> Interface de usuários online 
| 
+----> SocketTCP (PeerListener) 
| 
+----> Plugins (PeerListener)
```
