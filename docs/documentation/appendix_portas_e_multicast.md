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

## Onde o processo Java roda

A aba **Configurações** só lista interfaces que **este** processo enxerga. O nome decide a família: `eth`, `eno`, `enp`, `ens` ou “Ethernet” é **Cabo**; `wlan`, `wlp` ou “Wi-Fi” é **Wi-Fi**. O clone Git pode ficar no disco do WSL ou numa pasta do Windows. O que muda a lista é qual `java` abre o JAR.

| Processo | O que a saída mostra |
| --- | --- |
| `java.exe` do Windows | Placas reais. Sem cabo com IPv4, **Cabo** não é oferecido. **Wi-Fi** aparece quando a placa sem fio está ligada. |
| Ubuntu instalado no PC (não é WSL) | `wlan*` e `eth*`/`enp*` reais. O mesmo `./scripts/pitiupi.sh run --all` abre o Java do Linux. |
| Java de dentro do WSL | Em geral uma interface virtual `eth0` ou `eth1`. O programa classifica isso como **Cabo**, mesmo com o cabo físico desconectado e o Windows no Wi-Fi. Não existe `wlan` nesse Linux, então a saída **Wi-Fi** não aparece. |

Dois arranjos de WSL, e nenhum dos dois substitui o Java do Windows na sala:

- **NAT** (IP `172.x` no `ip -4 addr`): o Linux não está na rede dos colegas. Quem está em `192.168.x` ou `169.254.x` não recebe o multicast. Aumentar o TTL não coloca esse processo na LAN.
- **Rede espelhada** (o `eth*` mostra o mesmo IPv4 do Wi-Fi do Windows, por exemplo `192.168.3.x`): o rótulo **Cabo** segue o nome Linux, não o cabo da tomada. O socket ainda é o da interface virtual. A descoberta não é a mesma de um colega que abriu o Java no Windows.

Não dá para o código “pegar a placa Wi-Fi do Windows” a partir do WSL: essa placa não está no espaço de rede do Linux. Tratar todo `eth*` como Wi-Fi quebraria o Ubuntu nativo, em que `eth` é cabo de verdade. Por isso a sala usa o mesmo tipo de processo nos dois lados: os dois no Java do Windows, ou os dois no Ubuntu instalado na máquina. Não misture um Pitiupi aberto no WSL com outro aberto no Windows.

O passo a passo de cada máquina está no [quickstart](00_quickstart.md).

| Onde a pessoa está | O que abrir | Comando, na pasta do projeto |
| --- | --- | --- |
| Linux (Ubuntu na máquina) | Terminal | `./scripts/pitiupi.sh run --all` — Java do Linux |
| Windows | Prompt de Comando (`cmd`) | `scripts\pitiupi.cmd run --all` — Java do Windows |
| WSL | Terminal do Ubuntu | `./scripts/pitiupi.sh run --all` — a janela abre no Java do Windows |

No PowerShell o comando é o mesmo do cmd. Plugins, cache e logs ficam em `.pitiupi/` na pasta do projeto. JDK 17 do Windows, se faltar: `winget install Microsoft.OpenJDK.17`. No Windows o Ant também precisa estar no PATH.

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
