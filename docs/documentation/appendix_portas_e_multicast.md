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

## Sintomas

| Sintoma | Verificar |
| --- | --- |
| Lista de usuários vazia | Porta igual entre peers? Mesma sub-rede? Firewall? |
| Plugin não recebe | Desserialização / SPI / JAR em `plugins/` |
| TCP falha | Peer ativo no heartbeat? IP correto? |
