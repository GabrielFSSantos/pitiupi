# Usar a plataforma

A janela principal é a `MainShellFrame`. No centro ficam as abas dos plugins. Do lado, a lista **Usuários nesta rede**. Em **Plugins** está a galeria. Em **Configurações** estão nome, porta, saída, TTL e os JARs instalados.

## Login e sala

Nome e porta começam no diálogo de login e podem mudar em **Configurações** sem reiniciar o programa. A alteração vale na hora. Nome, porta, TTL e saída ficam gravados nas preferências locais do Java, não num `config.properties` do projeto.

A lista mostra **Todos** e cada pessoa de quem chegou heartbeat. Clicar numa linha abre as ações dos plugins (um submenu por plugin). **Todos** mostra as ações de sala. Uma pessoa mostra as ações daquele IP.

Abrir a ação não envia a mensagem sozinha: no ping, a ação só foca a aba; o botão **Enviar ping** é que manda o datagrama. No chat, a ação abre a conversa.

## Plugins na janela

A galeria lista `getDisplayName()` de cada JAR carregado. Clicar abre a aba central.

Depois de **Instalar JAR…** ou **Remover selecionado**, reinicie. O processo que já está aberto não relê a pasta de plugins. Instalar também registra o arquivo no `plugins.registry.json` do clone, para o próximo `./scripts/pitiupi.sh run` não apagar o JAR. Remover tira o registro. Detalhe em [plugins.md](plugins.md).

## Dois no mesmo computador

Duas janelas, dois nomes, duas portas (5000 e 5001). Elas não entram na mesma sala multicast, porque a sala é a porta. Servem para ver se o plugin abre, se o JAR carregou e se a aba responde. Para ver uma pessoa na lista da outra, use a mesma porta em dois computadores, ou aceite que no mesmo PC a segunda instância precisa de outra porta e portanto de outra sala.

## Dois computadores

Os dois no Java do Windows, ou os dois no Ubuntu instalado na máquina. Mesma porta, nomes diferentes, mesma sub-rede. O caminho de cada sistema está em [comecar.md](comecar.md). Porta, grupo multicast e WSL estão em [rede.md](rede.md).

Na aba **Configurações**, seção **Sua conexão**:

- **IP local** e o nome da interface (por exemplo `eth0 · 192.168.0.15`).
- **Porta** em que esta instância escuta UDP e TCP.
- **Saída:** Automático, Cabo ou Wi-Fi. Os dois precisam usar a rede dessa interface.
- **TTL:** padrão 1. Se o caminho até o colega passar por mais de um roteador, use o número de saltos, até 32.

O colega vê um nome novo no próximo anúncio (até cerca de 5 segundos).

### Conferir no sistema

Troque `5000` e o IP pelos valores da aba.

No Windows: `ipconfig`; `netstat -ano | findstr :5000`; `ping` no IP do colega; `tracert` no IP do colega. No PowerShell, `Test-NetConnection -ComputerName IP -Port 5000`.

No Linux: `ip -4 addr`; `ss -lntu | grep 5000`; `ping -c 3 IP`; `traceroute IP` ou `tracepath IP`; `nc -vz -w 3 IP 5000`.

### Como ler o resultado

- **O ping do sistema falha.** A rede entre os PCs não entrega pacote: sub-rede, cabo, Wi-Fi ou isolamento de clientes. O Pitiupi não contorna isso.
- **O ping funciona e a porta TCP não abre.** Firewall, porta errada ou o outro Pitiupi não está escutando. Libere UDP e TCP nessa porta.
- **O ping responde num salto e a lista só mostra Todos.** A descoberta é multicast. Confira porta igual, a mesma família de interface (os dois no cabo ou os dois no Wi-Fi; `169.254` e `192.168` não se veem), firewall de UDP e isolamento de clientes. Um processo no WSL e outro no Java do Windows não compartilham a placa.
- **O traceroute tem vários saltos e a lista fica vazia.** Suba o TTL até o número de saltos, nos dois.
- **A pessoa está na lista e o TCP avisa que não conectou.** A tentativa espera no máximo cerca de 5 segundos e a janela continua usável. Veja firewall de TCP.

A lista remove quem fica cerca de 15 segundos sem heartbeat.

## Limites visíveis

- Sem criptografia e sem conta de usuário além do nome local.
- Sem atualização automática de plugin: você instala o JAR.
- A lista não mostra um contador de mensagens não lidas.
- UDP pode perder datagrama. TCP é o transporte confiável entre dois IPs.
- O programa não executa o `ping` nem o `traceroute` do sistema; esses comandos só ajudam a separar falha de rede de falha do aplicativo.
