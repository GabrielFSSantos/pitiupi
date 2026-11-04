# Teste com um colega

**Teoria:** dois processos só se descobrem se compartilham a mesma sub-rede, a mesma porta de aplicação e um caminho em que o multicast da LAN não seja filtrado. **No Pitiupi:** a lista **Usuários nesta rede** enche com heartbeats UDP. **Limitação:** o Pitiupi não executa `ping` nem `traceroute` do sistema; esses comandos servem para separar problema de rede de problema do aplicativo.

Os dois Pitiupi precisam usar a **mesma porta** (na sala, em geral **5000**). Portas diferentes são salas diferentes: quem está em 5001 não aparece para quem ficou em 5000.

## O que conferir na aplicação

Abra a aba **Configurações**, seção **Sua conexão**:

- **IP local** e o nome da **interface** (por exemplo `eth0 · 192.168.0.15`).
- **Porta** em que esta instância escuta UDP e TCP.
- **Saída:** Automático, Cabo ou Wi-Fi. Os dois colegas precisam estar na mesma rede dessa interface.
- **TTL:** o padrão é **1**. O número de saltos do `tracert` (ou `traceroute`) até o IP do colega é o mínimo a colocar aqui. Se a linha 1 já é o IP dele, use **1**. Cada roteador no meio soma 1, até o máximo **32**. O teste na mesma sub-rede usa 1.

O nome na rede também se altera aí. O colega passa a ver o nome novo no próximo anúncio (até cerca de 5 s), sem você sair e voltar da lista.

## O que conferir no sistema

Substitua `5000` e o IP do colega pelos valores da aba Configurações.

### Windows

- IP: `ipconfig` e o IPv4 do adaptador Ethernet ou Wi-Fi.
- Porta em escuta: `netstat -ano | findstr :5000`
- Alcance: `ping` no IP do colega.
- Caminho: `tracert` no IP do colega. Conte as linhas numeradas até o IP dele: esse número é o TTL mínimo na aba Configurações.
- Porta TCP do aplicativo: no PowerShell, `Test-NetConnection -ComputerName IP -Port 5000`

### Linux

- IP: `ip -4 addr`
- Porta em escuta: `ss -lntu | grep 5000`
- Alcance: `ping -c 3 IP`
- Caminho: `traceroute IP` ou `tracepath IP`. Conte os saltos até o IP do colega: esse número é o TTL mínimo.
- Porta TCP do aplicativo: `nc -vz -w 3 IP 5000` (ou `ncat`)

## WSL na sala

Cada um abre o programa pelo caso da própria máquina, descrito no [quickstart](00_quickstart.md). No Linux, o terminal e `./scripts/pitiupi.sh run --all` (Java do Linux). No Windows, o cmd e `scripts\pitiupi.cmd run --all` (Java do Windows). No WSL, o terminal do Ubuntu e `./scripts/pitiupi.sh run --all` (a janela abre no Java do Windows). Os dois lados se veem. Abrir o `java` de dentro do WSL, sem esse comando, mostra só `eth0` ou `eth1`. Detalhe: [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md).

## Como ler o resultado

- **O ping falha.** O problema está na rede entre os PCs: sub-rede diferente, cabo, Wi-Fi ou isolamento de clientes no roteador. O Pitiupi não contorna isso.
- **O ping funciona e a porta TCP não abre.** Firewall, porta errada ou o outro Pitiupi não está escutando. Confira a porta na aba Configurações e libere UDP e TCP nessa porta.
- **O ping com TTL 1 responde e a lista só mostra Todos.** A descoberta é multicast, e um TTL baixo não é a causa quando o `tracert` tem um salto só. Confira porta igual, a mesma saída (os dois no cabo ou os dois no Wi-Fi; `169.254` e `192.168` ao mesmo tempo não se veem), firewall de UDP e isolamento de clientes. Um processo aberto dentro do WSL e o outro no Java do Windows não compartilham a mesma placa: os dois precisam estar no Java do Windows ou os dois no Ubuntu instalado na máquina. Ver [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md).
- **O `tracert` tem mais de um salto e a lista fica vazia.** Suba o TTL em Configurações até o número de saltos (máximo 32) e aplique nos dois. Um roteador no meio só entrega o multicast se estiver disposto a encaminhar o grupo.
- **O colega está na lista e o TCP avisa que não conectou.** A tentativa de abrir o socket espera no máximo cerca de **5 s** e a janela continua utilizável. Veja firewall de TCP ou se a porta mudou no meio do caminho.

A lista espera cerca de **5 s** entre anúncios e remove quem fica cerca de **15 s** sem heartbeat. Ao trocar de porta, esta instância envia goodbye na sala antiga e passa a anunciar na porta nova.

Detalhe de portas, interface e TTL: [appendix_portas_e_multicast.md](appendix_portas_e_multicast.md).
