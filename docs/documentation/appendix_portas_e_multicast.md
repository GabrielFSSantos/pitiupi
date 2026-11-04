# Apêndice — portas e multicast

## Configuração

- **Nome** e **porta** começam no diálogo de login e podem ser alterados na aba **Configurações** sem reiniciar. A alteração vale na hora.
- A mesma porta é usada para UDP (multicast socket) e TCP (`ServerSocket`). Trocar a porta sai da sala antiga (goodbye) e passa a procurar colegas na porta nova.
- **Saída:** Automático (cabo antes de Wi-Fi), só Cabo ou só Wi-Fi. O multicast e o IP exibido seguem essa escolha. O TCP de saída também sai pelo IPv4 dessa interface.
- **TTL** do multicast: padrão **1** (só a rede local). Na Configurações, de 1 a 32.
- Preferências persistidas localmente (`java.util.prefs`): nome, porta, TTL e saída. `config.properties` não guarda esses campos.

## Multicast

- Grupo: **`224.0.0.3`** (fixo no código do núcleo).
- Buffer UDP: **256000** bytes (configuração em `SocketUDP`).

Todos os peers na mesma LAN devem usar a **mesma porta** para participar do mesmo grupo lógico de peers (portas diferentes = grupos isolados).

## Mesmo computador

Execute duas instâncias com **portas diferentes** (ex.: 5000 e 5001). Mesmo nome de usuário em duas instâncias pode confundir a lista — use nomes distintos.

## Firewall e Wi‑Fi

- Bloqueio de multicast em redes de visitantes ou isolamento AP→AP impede descoberta.
- Em laboratório, preferir cabo ou Wi‑Fi com multicast habilitado.

## WSL2

Executar o Pitiupi no WSL e peers no Windows (ou vice-versa) pode falhar em multicast. Preferir:

- todos os peers no mesmo ambiente (Linux nativo ou VMs), ou
- dois PCs físicos na mesma sub-rede.

## Escolha de interface (multicast)

No modo **Automático**, o núcleo associa o socket multicast à interface preferida: **cabo (Ethernet) antes de Wi‑Fi**; em cabo com link-local APIPA, prioriza IPv4 **`169.254.*`**. **Cabo** ou **Wi-Fi** na aba Configurações restringe a essa família (nome da interface ou rótulo `Ethernet` / `Wi-Fi`). Isso não substitui a exigência de **mesma sub-rede** entre os dois PCs — `169.254.x` e `192.168.x` ao mesmo tempo em hosts diferentes não compartilham heartbeat.

Roteiro para achar o próprio IP, a porta e o motivo de a lista ficar vazia: [17_teste_com_colega.md](17_teste_com_colega.md).

## IP do peer na UI (Windows)

Ao abrir conversa pela lista de presença, o campo de destino usa **`PeerIpv4`**: IPv4 pontilhado legível, sem forma IPv6-mapeada (`::ffff:…`) nem sufixo de zona (`%7`). Ver [decision-register.md](../decision-register.md) (D22).

## Dois PCs na mesma Wi‑Fi

1. **Mesma porta no login** (ex.: `5000` nos dois). Portas diferentes isolam a sala.
2. **Configurações → Sua conexão:** IPs na mesma sub-rede (ex. ambos `192.168.0.x` ou `192.168.1.x`). Se um for `172.x` (WSL/Docker) e outro `192.168.x`, o multicast entre esses hosts não cruza.
3. **Firewall:** liberar **UDP entrada e saída** na porta do login (Windows Defender / `ufw`).
4. **Roteador:** desativar *isolamento de clientes* / *AP isolation* / Wi‑Fi de visitantes.
5. Aguardar até **~15 s** após abrir os dois apps (heartbeat a cada 5 s).

## Sintomas

| Sintoma | Verificar |
| --- | --- |
| Lista só com **Todos**, sem peers | Porta igual? Mesma sub-rede? Firewall UDP? AP isolation? WSL num dos PCs? |
| Plugin não recebe | Desserialização / SPI / JAR em `PITIUPI_HOME/plugins` |
| TCP falha | Peer visível na lista? IP correto? A tentativa avisa em cerca de 5 s e não trava a janela |
