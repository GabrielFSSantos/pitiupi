# Apêndice — portas e multicast

## Configuração

- **Nome** e **porta** definidos no diálogo de login (`Login`).
- A mesma porta é usada para UDP (multicast socket) e TCP (`ServerSocket`).
- Preferências persistidas localmente (`java.util.prefs`).

## Multicast

- Grupo: **`224.0.0.3`** (fixo no código do núcleo).
- Buffer UDP: **256000** bytes (configuração em `SocketUDP`).

Todos os peers na mesma LAN devem usar a **mesma porta** para participar do mesmo “grupo lógico” da aula (portas diferentes = grupos isolados).

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

O núcleo associa o socket multicast à interface de rede preferida: **cabo (Ethernet) antes de Wi‑Fi**; em cabo com link-local APIPA, prioriza IPv4 **`169.254.*`**. Isso não substitui a exigência de **mesma sub-rede** entre os dois PCs — `169.254.x` e `192.168.x` ao mesmo tempo em hosts diferentes não compartilham heartbeat.

## Checklist — dois PCs na mesma Wi‑Fi

1. **Mesma porta no login** (ex.: `5000` nos dois). Portas diferentes isolam a sala.
2. **Configurações → Sua conexão:** IPs na mesma sub-rede (ex. ambos `192.168.0.x` ou `192.168.1.x`). Se um for `172.x` (WSL/Docker) e outro `192.168.x`, o multicast da aula não cruza.
3. **Firewall:** liberar **UDP entrada e saída** na porta do login (Windows Defender / `ufw`).
4. **Roteador:** desativar *isolamento de clientes* / *AP isolation* / Wi‑Fi de visitantes.
5. Aguardar até **~15 s** após abrir os dois apps (heartbeat a cada 5 s).

## Sintomas

| Sintoma | Verificar |
| --- | --- |
| Lista só com **Todos**, sem peers | Porta igual? Mesma sub-rede? Firewall UDP? AP isolation? WSL num dos PCs? |
| Plugin não recebe | Desserialização / SPI / JAR em `PITIUPI_HOME/plugins` |
| TCP falha | Peer visível na lista? IP correto? |
