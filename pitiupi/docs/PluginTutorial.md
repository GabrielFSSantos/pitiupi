# Como criar plugins para o Pitiupi

Aqui será discutido os métodos e classes que um desenvolvedor pode ou deve usar para criar um plugin para a plataforma Pitiupi. 

Alguns deles estarão marcados com um <span style="color: red;">[REF]</span>, isso indica que eles estão sendo alterados para a versão 3.0 e deixarão de existir ou vão mudar consideravelmente.

## GUI

O pitiupi é feito usando a biblioteca Swing do Java, por isso é altamente recomendado que os plugins também a utilizem na hora de criar sua interface gráfica, porém não é obrigatório.

## Pitiupi

A classe `Pitiupi` é a interface que o seu plugin vai receber quando for criado, ela fornece os métodos necessários para interagir com a interface gráfica e comunicação para outros peers.

### sendUdpMulticast(Message message)

Envia uma mensagem multicast, retorna `boolean` indicando se teve sucesso.

### sendUdpUnicast(Message message, InetAddress address)

Envia uma mensagem unicast via udp para o endereço fornecido, retorna `boolean` indicando se teve sucesso.

### sendTcp(Message message, InetAddress address)

Envia uma mensagem unicast via tcp para o endereço fornecido, retorna `boolean` indicando se teve sucesso.

### <span style="color: red;">[REF]</span> connectTcp(InetAddress address, String senderId, String receiverId)

Pede uma conexão tcp privada para o seu plugin com outro usuário, para utilizar essa funcionalidade é necessário implementar alguns métodos opcionais em Plugin. Maisinformações serão discutidas na seção TcpConnection.

### getUserName()

Retorna o nome do usuário conectado

### getPort()

Retorna a porta que o usuário está usando o Pitiupi

### createMenu(MenuCategory category, Menu menu)

Adiciona um menu ao pitiupi, category define em qual das categoria de Menus ele será adicionado: Jogos, Social, Streaming.

Os `Menus` serão discutidos em uma seção posterior.

## Classe Plugin

Essa é a classe que o pitiupi vai procurar ao carregar o seu plugin, ela define um contrato de métodos que todo plugin deve possuir.

### getName()

Indica o nome público do plugin.

### getAuthor()

Indica o nome dos desenvolvedores do plugin.

### getVersion()

Indica a versão atual do plugin.

### getIdentifier()

Indica um id que o pitiupi vai utilizar para enontrar seu plugin, por isso é importante que ele seja bem especifico para evitar que dois plugins sejam confundidos durante a comunicação. Procure usar algo como: MeuPlugin.Autor.Números

### createPlugin(Pitiupi pitiupi)

Após o pitiupi identificar e carregar o seu plugin, ele vai chamar esse método para inicializá-lo, por isso pense nele como o construtor do seu plugin, é aqui que você inicializa atributos, adiciona os menus inciais, etc.

### getMessages()

Esse método indica ao pitiupi quais tipos de mensagem seu plugin quer receber, normalmente as que ele mesmo implementar.
Nesse caso, é normal que ele faça:

```
return List.of(
    SuaMensagem1.class, 
    SuaMensagem2.class,
    ...
    );
```

Caso você queira que seu plugin receba todo tipo de mensagem, existe um método `receiveAllTypes()` que ele pode sobreescrever para receber todas mensagens
que não sejam consideradas privadas.

### receiveMessage(Message message)

Esse é o método que o pitiupi vai chamar quando quiser te entregar uma mensagem pelo canal publico de mensagens. Note que ele entrega uma `Message`, então é necessário que
seu plugin confira o tipo real ele mesmo. Como em:

```
if (message instanceOf MinhaMensagem minhaMensagem) {...}
```

O pitiupi garante que é uma mensagem de um tipo que o seu plugin diz aceitar.

### <span style="color: red;">[REF]</span> métodos opcionais

Os métodos a seguir possuem uma implementação padrão da própria classe que retornam false. Os três ultimos são necessários caso seu plugin deseje criar e receber conexões privadas.

### <span style="color: red;">[REF]</span> receiveAllTypes()

Indica ao Pitiupi que seu plugin deseja receber todas as mensagens publicas da plataforma, independente do tipo.

### <span style="color: red;">[REF]</span> receivePrivateConnection(TcpConnection connection)

Esse método é chamado quando o Pitiupi precisa entregar uma `TcpConnection` ao seu plugin, seja um que você pediu ou recebeu de um peer.

### <span style="color: red;">[REF]</span> receiveBytes(TcpConnection connection, byte[] bytes)

Esse método é chamado para entregar as mensagens recebidas por um `TcpConnection`, o tcpConnection no parametro indica qual a fonte da mensagem.

### <span style="color: red;">[REF]</span> onSocketClosed(TcpConnection connection)

Esse é método é chamado quando o socket de um `TcpConnection` pertencente ao plugin é fechado.

## Message

Essa é a classe abstrata que o pitiupi usa para comunicação nos canais publicos.

<span style="color: red;">[REF]</span> Ela possui um atributo isPublic que por padrão é true, mas é possível defini-lo no construtor como false caso a mensagem deva ser privada.

Essa classe implementa `Serializable` e possui um método `toByteArray` que transforma a mensagem em `byte[]`. Porém é importante notar que caso ela possua um objeto de outra classe como atributo aquela classe também deve implementar `Serializable`.

## <span style="color: red;">[REF]</span> TcpConnection

Representa uma conexão Tcp ativa para algum outro peer, mensagens enviadas por essa conexão não chegam ao canal publico e em vez disso vão direto para destinatário.

Caso fique inativa (Sem enviar ou receber mensagens) por 5 minutos a conexão é fechada automaticamente e o dono é notificado.

### <span style="color: red;">[REF]</span> void send(byte[] message)

Envia um conjunto de bytes[] para o destinatario.

Caso queira enviar uma `Message` chame o método toByteArray para transforma-la em bytes primeiro.

### <span style="color: red;">[REF]</span> boolean isClosed()

Indica se a conexão esta fechada

### <span style="color: red;">[REF]</span> InetAddress getAddress()

Retorna o endereço do peer que ela esta conectada.

### <span style="color: red;">[REF]</span> String getReceiver()

Retorna o identificador do plugin que ela esta conectada.

### <span style="color: red;">[REF]</span> getReceiver()

Retorna o identifador do plugin que pediu pela conexão.

## Menu

Essa é a classe que indica ao pitiupi que um menu deve ser adicionado a janela principal da aplicação.

Menus podem ser de dois tipos `UnicastMenu` e `MulticastMenu` com a principal diferença entre eles sendo onde eles são adicionados na janela principal. Outras mudanças serão discutidas quando for relevante nas classes a seguir.

Atualmente Menus não podem ser alterados uma vez que são adicionados, então é importante que eles sejam feitos com isso em mente.

Tem um método `String getText()` que retorna o que deve ser escrito no Menu`.

### SubMenu

Permite que o `Menu` expanda em uma nova seção de menus. 

`UnicastSubMenu` o método `getMenu()` retorna `List<UnicastMenu>`

`MulticastSubMenu` o método `getMenu()` retorna `List<MulticastMenu>`

Exemplo:

```

ChatCasualMulticastMenu chatCasualMulticastMenu;
ChatEstudoMutlticastMenu chatEstudoMutlticastMenu;

...

@Override
    public List<MulticastMenu> getMenus() {
        return List.of(chatCasualMulticastMenu, chatEstudoMutlticastMenu);
    }
```

### ActionMenu

É um `Menu` que que realiza alguma ação quando selecionado pelo usuário. 

`UnicastActionMenu` o método `onClick(User user)` é chamado quando selecionado pelo usuário, recebendo como parametro o peer escolhido pelo usuário.

`MulticastActionMenu` o método `onClick()` é chamado quando selecionado pelo usuário.

Exemplo:

```

@Override
    public void onClick(User user) {
        callManager.openCallWindow(user);
    }
```

## Exemplo de plugin no pitiupi:

### Plugin

Implementa o contrato de plugin.

Inicializa a janela principal e cria os menus.

Também define quais tipos de mensagem ele quer receber.

```
public class ChatPlugin extends Plugin {

    private ChatWindow chatWindow;

   @Override
    public void createPlugin(Pitiupi pitiupi) {
        this.chatWindow = new ChatWindow(pitiupi);
        pitiupi.createMenu(MenuCategory.Social, new ChatMenu(chatWindow));
    }


    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(ChatMessage.class);
    }


    @Override
    public void receiveMessage(Message message) {
        if (message instanceof ChatMessage msg) {
            this.chatWindow.receiveChatMessage(msg);
        }
    }

    @Override
    public String getName() {
        return "Chat";
    }

    @Override
    public String getAuthor() {
        return "Flávio Schiavoni";
    }

    @Override
    public String getVersion() {
        return "0";
    }

    @Override
    public String getIdentifier() {
        return "Pitiupi.Chat.Example.123456";
    }
}
```

### Message

Implementa uma mensagem customizada que o plugin vai utilizar para comunicação.

```
public class ChatMessage extends Message {

    private String text;
    private String name;


    public ChatMessage(){
        
    }

    public ChatMessage(String name, String text){
        this.name = name;
        this.text = text;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }
}
```

### Menus

Implementa os dois menus de Multicast utilizados pela aplicação.

#### ActionMenu

Esse menu apenas abre a janela principal do plugin.

```
public class ChatMulticastMenu extends MulticastActionMenu {
    ChatWindow chatWindow;

    public ChatMulticastMenu(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }

    @Override
    public String getText() {
        return "Show Chat Window";
    }

    @Override
    public void onClick() {
        chatWindow.setVisible(true);
    }
}
```

Esse menu abre a seção de menus do plugin Chat.

#### SubMenu

```
public class ChatMenu extends MulticastSubMenu {
    ChatMulticastMenu chatMulticastMenu;

    public ChatMenu(ChatWindow chatWindow) {
        this.chatMulticastMenu = new ChatMulticastMenu(chatWindow);
    }

    @Override
    public String getText() {
        return "Chat";
    }

    @Override
    public List<MulticastMenu> getMenus() {
        return List.of(chatMulticastMenu);
    }
}
```

### GUI

Essa é a janela principal do plugin de chat.

```
public class ChatWindow extends JDialog {

    private JButton jButton1;
    private JEditorPane jEditorPane1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JSplitPane jSplitPane1;
    private JTextArea jTextArea1;

    private final Pitiupi pitiupi;

	public ChatWindow(Pitiupi pitiupi) {
          this.pitiupi = pitiupi;
          initComponents();
	}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setTitle("Chat Window");
        this.setBounds(new java.awt.Rectangle(0, 0, 800, 600));
        this.getContentPane().setLayout(new java.awt.BorderLayout(5, 5));
        this.setPreferredSize(new java.awt.Dimension(800, 600));

        jSplitPane1 = new JSplitPane();
        jScrollPane1 = new JScrollPane();
        jEditorPane1 = new JEditorPane();
        jPanel1 = new JPanel();
        jScrollPane2 = new JScrollPane();
        jTextArea1 = new JTextArea();
        jPanel2 = new JPanel();
        jButton1 = new JButton();

        jSplitPane1.setDividerLocation(500);
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jEditorPane1.setContentType("text/html");
        jPanel1.setLayout(new java.awt.BorderLayout());
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (evt.getKeyChar() == '\n') {
                    sendMessage();
                }
            }
        });
        jButton1.setText("Send");

        jScrollPane1.setViewportView(jEditorPane1);
        jScrollPane2.setViewportView(jTextArea1);
        jSplitPane1.setTopComponent(jScrollPane1);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        jButton1.addActionListener((java.awt.event.ActionEvent evt) -> {
            sendMessage();
        });
        jPanel2.add(jButton1);
        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_END);
        jSplitPane1.setRightComponent(jPanel1);
        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);
        pack();
    }

    private void sendMessage() {
        if (jTextArea1.getText().trim().isEmpty()) {
            return;
        }
        ChatMessage message = new ChatMessage(pitiupi.getUserName(), jTextArea1.getText());
        this.sendChatMessage(message);
        jTextArea1.setText("");
    }

    public void sendChatMessage(Message message) {
        pitiupi.sendUdpMulticast(message);
    }

    public void receiveChatMessage(ChatMessage message) {
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyyy hh:mm:ss");
        Date date = new Date();
        this.receiveChatMessage(dt.format(date) + " "
                + "<b>" + message.getName() + "</b>: "
                + message.getText() + "<br>");
    }

    public void receiveChatMessage(String message) {
        jEditorPane1.setText(message + jEditorPane1.getText());
    }
}
```